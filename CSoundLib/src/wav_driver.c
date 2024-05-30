#include "wav_driver.h"

#include "tracks.h"
#include "audio_errors.h"
#include "audio_state.h"

#include <string.h>
#include <pthread.h>
#include <stdlib.h>

#include "callbacks.h"
#include "soundlib_util.h"
#include "buffers_streams.h"

#include <fcntl.h>

typedef struct _writeArgs {
    FILE* fp;
    trackObject* track;
    const char* bytes;
    int num_bytes;
} writeArgs;

static wavHeader _createWavHeader(int numSamples, int sampleRate, int bitDepth, int numChannels) {
    wavHeader header;
    header.chunkId[0] = 'R'; header.chunkId[1] = 'I'; header.chunkId[2] = 'F'; header.chunkId[3] = 'F';
    header.chunkSize = 36 + numSamples * numChannels * (bitDepth / 8);
    header.format[0] = 'W'; header.format[1] = 'A'; header.format[2] = 'V'; header.format[3] = 'E';
    header.subchunk1Id[0] = 'f'; header.subchunk1Id[1] = 'm'; header.subchunk1Id[2] = 't'; header.subchunk1Id[3] = ' ';
    header.subchunk1Size = 16; // PCM
    header.audioFormat = 1; // PCM
    header.numChannels = numChannels;
    header.sampleRate = sampleRate;
    header.byteRate = sampleRate * numChannels * (bitDepth / 8);
    header.blockAlign = numChannels * (bitDepth / 8);
    header.bitsPerSample = bitDepth;
    header.subchunk2Id[0] = 'd'; header.subchunk2Id[1] = 'a'; header.subchunk2Id[2] = 't'; header.subchunk2Id[3] = 'a';
    header.subchunk2Size = numSamples * numChannels * (bitDepth / 8);
    return header;
}

int create_recording_wav_file(trackObject* track, int sample_rate, int fileId) {
    /* open a new audio file for writing for track with id == trackid */
    int current_offset = csoundlib_state->current_cursor_offset;

    const int bufferSize = MAX_NAME_SIZE;
    char buffer[bufferSize];
    const char *file_name;
    /* create file name from track id and sample offset */
    int charsWritten = 
                sprintf(
                    buffer, 
                    "/Users/jacobzeisel/Desktop/track-%d-offset-%d.wav", 
                    track->track_id, 
                    current_offset
                );

    if (charsWritten == 0 || charsWritten >= bufferSize) {
        return SoundIoErrorOpeningFile;
    }
    file_name = buffer;

    FILE* fp = fopen(file_name, "wb");
    if (!fp) {
        return SoundIoErrorOpeningFile;
    }
    int fd = fileno(fp);
    wavHeader header = _createWavHeader(44100 * 60 * 10, 44100, 24, 1);
    flock(fd, LOCK_EX);
    fwrite(&header, sizeof(wavHeader), 1, fp);
    flock(fd, LOCK_UN);

    audioFile file_obj = {
        .fp = fp,
        .file_id = fileId,
        .is_file_open = true,
        .file_sample_offset = current_offset,
        .samples_written = 0
    };

    strncpy(file_obj.file_name, buffer, charsWritten);
    file_obj.file_name_len = charsWritten;

    track->files[track->num_files] = file_obj;
    track->num_files += 1;
    return SoundIoErrorNone;
}

void stop_recording_wav_file(audioFile* file) {
    file->is_file_open = false; // set to false to stop data thread writes
    int fd = fileno(file->fp);
    /* update .wav file for how many samples have been written */
    wavHeader header = _createWavHeader(file->samples_written, 44100, 24, 1);

    flock(fd, LOCK_EX);

    fseek(file->fp, 0, SEEK_SET);
    fwrite(&header, sizeof(wavHeader), 1, file->fp);
    fclose(file->fp);

    flock(fd, LOCK_UN);
}

void* write_to_wav_file(void* args) {
    writeArgs* vals = (writeArgs*)args;
    const char* byte_buffer = vals->bytes;
    int num_bytes = vals->num_bytes;
    trackObject* track = vals->track;
    /* copy bytes into a new buffer */
    const char* new_buffer = malloc(num_bytes);
    memcpy(new_buffer, byte_buffer, num_bytes);
    FILE* fp = vals->fp;
    int fd = fileno(fp);
    flock(fd, LOCK_EX);
    /* write bytes to file */
    for (int idx = 0; idx < num_bytes; idx += 4) {
        fwrite(new_buffer + idx, sizeof(char), 3, fp);
        track->files[track->num_files - 1].samples_written += 1;
    }
    flock(fd, LOCK_UN);

    free(args);
    free(new_buffer);
    return NULL;
}

void thr_write_to_wav_file(trackObject* track, const char* bytes, int num_bytes) {
    pthread_t thread_id;
    /* later-- find a way to not use malloc on the audio thread!! */
    writeArgs* args = malloc(sizeof(writeArgs));
    args->fp = track->files[track->num_files - 1].fp;
    args->bytes = bytes;
    args->num_bytes = num_bytes;
    args->track = track;
    if (track->files[track->num_files - 1].is_file_open) {
        pthread_create(&thread_id, NULL, write_to_wav_file, (void*)args);
    }
}

int open_wav_for_playback(trackObject* track, audioFile* file) {
    if (file->is_file_open == false) {
        FILE* fp = fopen(file->file_name, "rb");
        if (!fp) {
            panicCallback("error opening file for playback");
            return SoundIoErrorOpeningFile;
        }
        file->fp = fp;
        file->is_file_open = true;
        logCallback("opened file for playback");
    }
    return SoundIoErrorNone;
}

void close_wav_for_playback(audioFile* file) {
    logCallback("closing file after playback");
    fclose(file->fp);
    file->is_file_open = false;
}

int read_wav_file_for_playback(trackObject* track, char* mixed_buffer, int max_bytes) {
    /* this should get changed to be the current location before cursor gets moved */
    /* current implementation is that the UI moves the cursor which moves this value */
    int current_offset = csoundlib_state->current_cursor_offset;
    for (int fileidx = 0; fileidx < track->num_files; fileidx++) {
        audioFile* file = &(track->files[fileidx]);
        if (file->is_file_open) {
            if (current_offset >= file->file_sample_offset && 
                current_offset < (file->file_sample_offset + file->samples_written)) {
                /* go to correct location in file and read fill_bytes number of bytes if available */
                /* if not available, read amount available and pad the rest with 0x00 */
                int num_samples_into_file = current_offset - file->file_sample_offset;
                FILE* fp = file->fp;

                int fd = fileno(fp);
                char temp_buffer[MAX_BUFFER_SIZE_BYTES] = {0x00};
                flock(fd, LOCK_EX);
                fseek(fp, sizeof(wavHeader) + num_samples_into_file * 3, SEEK_SET);
                int bytes_copied = 0;
                while (bytes_copied < max_bytes) {
                    int ret = fread(temp_buffer + bytes_copied, sizeof(char), 3, fp);
                    if (ret < 3) {
                        break;
                    }
                    bytes_copied += 4;
                }
                add_audio_buffers_24bitNE(csoundlib_state->mixed_output_buffer, temp_buffer, bytes_copied);
                flock(fd, LOCK_UN);
                return bytes_copied;
            }
        }
    }
    return 0;
}

