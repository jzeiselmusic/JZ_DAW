#include "wav.h"
#include "tracks.h"
#include "errors.h"
#include "state.h"
#include <string.h>
#include <pthread.h>
#include <stdlib.h>
#include "callbacks.h"
#include "csl_util.h"
#include "streams.h"
#include <fcntl.h>

typedef struct _writeArgs {
    FILE* fp;
    trackObject* track;
    const char* bytes;
    int num_bytes;
} writeArgs;

static wavHeader _createWavHeader(int numSamples, int sampleRate, int bitDepth, int numChannels) {
    wavHeader header;
    header.riff_header[0] = 'R'; header.riff_header[1] = 'I'; header.riff_header[2] = 'F'; header.riff_header[3] = 'F';
    header.wav_size = 36 + numSamples * numChannels * (bitDepth / 8);
    header.wave_header[0] = 'W'; header.wave_header[1] = 'A'; header.wave_header[2] = 'V'; header.wave_header[3] = 'E';
    header.fmt_header[0] = 'f'; header.fmt_header[1] = 'm'; header.fmt_header[2] = 't'; header.fmt_header[3] = ' ';
    header.fmt_chunk_size = 16; // PCM
    header.audio_format = 1; // PCM
    header.num_channels = numChannels;
    header.sample_rate = sampleRate;
    header.byte_rate = sampleRate * numChannels * (bitDepth / 8);
    header.sample_alignment = numChannels * (bitDepth / 8);
    header.bit_depth = bitDepth;
    header.data_header[0] = 'd'; header.data_header[1] = 'a'; header.data_header[2] = 't'; header.data_header[3] = 'a';
    header.data_bytes = numSamples * numChannels * (bitDepth / 8);
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
    wavHeader header = _createWavHeader(
                            csoundlib_state->sample_rate * 60 * 10, 
                            csoundlib_state->sample_rate, 
                            csoundlib_state->input_dtype.bit_depth, 
                            1
                        );
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
    /* user shall not be able to change sample rate and bit depth while recording wav file */

    file->is_file_open = false; // set to false to stop data thread writes
    int fd = fileno(file->fp);
    /* update .wav file for how many samples have been written */
    wavHeader header = _createWavHeader(
                            file->samples_written, 
                            csoundlib_state->sample_rate, 
                            csoundlib_state->input_dtype.bit_depth, 
                            1
                        );

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
    trackObject* track_p = vals->track;
    /* copy bytes into a new buffer */
    const char* new_buffer = malloc(num_bytes);
    memcpy(new_buffer, byte_buffer, num_bytes);
    FILE* fp = vals->fp;
    int fd = fileno(fp);
    flock(fd, LOCK_EX);
    /* write bytes to file */
    uint8_t bytes_in_buffer = csoundlib_state->input_dtype.bytes_in_buffer;
    uint8_t bytes_in_sample = csoundlib_state->input_dtype.bytes_in_sample;
    for (int idx = 0; idx < num_bytes; idx += bytes_in_buffer) {
        fwrite(new_buffer + idx, sizeof(char), bytes_in_sample, fp);
        track_p->files[track_p->num_files - 1].samples_written += 1;
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
    }
    return SoundIoErrorNone;
}

void close_wav_for_playback(audioFile* file) {
    fclose(file->fp);
    file->is_file_open = false;
}

int read_wav_file_for_playback(trackObject* track, char* mixed_buffer, int max_bytes) {
    /* this should get changed to be the current location before cursor gets moved */
    /* current implementation is that the UI moves the cursor which moves this value */
    int current_offset = csoundlib_state->current_cursor_offset;
    uint8_t bytes_in_buffer = csoundlib_state->input_dtype.bytes_in_buffer;
    uint8_t bytes_in_sample = csoundlib_state->input_dtype.bytes_in_sample;
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
                char final_buffer[MAX_BUFFER_SIZE_BYTES] = {0x00};
                flock(fd, LOCK_EX);
                fseek(fp, sizeof(wavHeader) + num_samples_into_file * bytes_in_sample, SEEK_SET);
                int bytes_read = 0;
                bytes_read = fread(temp_buffer, sizeof(uint8_t), max_bytes, fp);
                char* dest = final_buffer;
                for (int i = 0; i < bytes_read; i += bytes_in_sample) {
                    memcpy(dest, temp_buffer + i, bytes_in_sample);
                    dest += bytes_in_buffer;  
                }
                add_and_scale_audio(
                    (uint8_t*)final_buffer, 
                    (uint8_t*)mixed_buffer, 
                    1.0, 
                    bytes_read / bytes_in_buffer
                );
                flock(fd, LOCK_UN);
                return bytes_read;
            }
        }
    }
    return 0;
}

int read_wav_file_for_bounce(audioFile* file, char* mixed_buffer, int sample_offset) {
    uint8_t bytes_in_buffer = csoundlib_state->input_dtype.bytes_in_buffer;
    uint8_t bytes_in_sample = csoundlib_state->input_dtype.bytes_in_sample;
    if (file->is_file_open) {
        if (sample_offset >= file->file_sample_offset && 
            sample_offset < (file->file_sample_offset + file->samples_written)) {
            int num_samples_into_file = sample_offset - file->file_sample_offset;
            FILE* fp = file->fp;
            int fd = fileno(fp);
            char temp_buffer[4] = {0x00};
            flock(fd, LOCK_EX);
            fseek(fp, sizeof(wavHeader) + num_samples_into_file * bytes_in_sample, SEEK_SET);
            int ret = fread(temp_buffer, sizeof(char), bytes_in_sample, fp);
            add_and_scale_audio(
                (uint8_t*)temp_buffer, 
                (uint8_t*)mixed_buffer, 
                1.0, 
                1
            );
            flock(fd, LOCK_UN);
            return ret;
        }
    }
    return 0;
}

int lib_bounceMasterToWav(int start_sample_offset, int end_sample_offset) {
    /* create new wav file */
    FILE* fp = fopen("/Users/jacobzeisel/Desktop/master_bounce.wav", "wb");
    if (!fp) {
        return SoundIoErrorOpeningFile;
    }
    /* write wavheader */
    int fd = fileno(fp);
    flock(fd, LOCK_EX);
    wavHeader header = _createWavHeader(
                            end_sample_offset - start_sample_offset, 
                            csoundlib_state->sample_rate,
                            csoundlib_state->input_dtype.bit_depth, 
                            1
                        );
    fwrite(&header, sizeof(wavHeader), 1, fp);

    /* open all files for reading */
    hti it = ht_iterator(csoundlib_state->track_hash_table);
    while (ht_next(&it)) {
        trackObject* track_p = (trackObject*)it.value;
        for (int fileIdx = 0; fileIdx < track_p->num_files; fileIdx++) {
            open_wav_for_playback(
                track_p, 
                &(track_p->files[fileIdx])
            );
        }
    }

    for (int sample_offset = start_sample_offset; sample_offset < end_sample_offset; sample_offset++) {
        /* read from any files available at current sample */
        char mixed_buffer[4] = {0x00};
        hti it = ht_iterator(csoundlib_state->track_hash_table);
        while (ht_next(&it)) {
            trackObject* track_p = (trackObject*)it.value;
            for (int fileIdx = 0; fileIdx < track_p->num_files; fileIdx++) {
                read_wav_file_for_bounce(
                    &(track_p->files[fileIdx]), 
                    mixed_buffer, 
                    sample_offset
                );
            }
        }
        fwrite(mixed_buffer, sizeof(char), csoundlib_state->input_dtype.bytes_in_sample, fp);
    }
    fclose(fp);
    flock(fd, LOCK_UN);

    /* close all files for reading */
    it = ht_iterator(csoundlib_state->track_hash_table);
    while (ht_next(&it)) {
        trackObject* track_p = (trackObject*)it.value;
        for (int fileIdx = 0; fileIdx < track_p->num_files; fileIdx++) {
            close_wav_for_playback(&(track_p->files[fileIdx]));
        }
    }

    return SoundIoErrorNone;
}

int _read_wav_header(FILE* fp, wavHeader* header) {
    // Read RIFF chunk descriptor
    fread(header->riff_header, sizeof(header->riff_header), 1, fp);
    fread(&header->wav_size, sizeof(header->wav_size), 1, fp);
    fread(header->wave_header, sizeof(header->wave_header), 1, fp);

    // Read "fmt " sub-chunk
    fread(header->fmt_header, sizeof(header->fmt_header), 1, fp);
    fread(&header->fmt_chunk_size, sizeof(header->fmt_chunk_size), 1, fp);
    fread(&header->audio_format, sizeof(header->audio_format), 1, fp);
    fread(&header->num_channels, sizeof(header->num_channels), 1, fp);
    fread(&header->sample_rate, sizeof(header->sample_rate), 1, fp);
    fread(&header->byte_rate, sizeof(header->byte_rate), 1, fp);
    fread(&header->sample_alignment, sizeof(header->sample_alignment), 1, fp);
    fread(&header->bit_depth, sizeof(header->bit_depth), 1, fp);

    // Find "data" sub-chunk
    char chunk_id[4];
    uint32_t chunk_size;
    while (1) {
        fread(chunk_id, sizeof(chunk_id), 1, fp);
        fread(&chunk_size, sizeof(chunk_size), 1, fp);

        if (memcmp(chunk_id, "data", 4) == 0) {
            memcpy(header->data_header, chunk_id, 4);
            header->data_bytes = chunk_size;
            break;
        }

        // Skip this chunk
        fseek(fp, chunk_size, SEEK_CUR);
    }

    return 0;
}

void lib_enableMetronome(bool enabled) {
    csoundlib_state->metronome.enabled = enabled;
}

int lib_readWavFileForMetronome() {
    FILE* fp = fopen("/Users/jacobzeisel/git/App_Test/CSoundLib/res/synthsine.wav", "rb");

    wavHeader fileHeader;
    _read_wav_header(fp, &fileHeader);

    /* solution for now. will make more general later */
    if (fileHeader.num_channels != 1) {
        return SoundIoErrorReadingWavForMetronome;
    }

    if (fileHeader.bit_depth != csoundlib_state->input_dtype.bit_depth) {
        return SoundIoErrorReadingWavForMetronome;
    }

    if (fileHeader.sample_rate != csoundlib_state->sample_rate) {
        return SoundIoErrorReadingWavForMetronome;
    }

    char temp_buffer[CSL_MAX_METRONOME_BUF_SIZE] = {0x00};

    int jdx = 0;
    char sample[4];
    uint8_t bytes_in_buffer = csoundlib_state->input_dtype.bytes_in_buffer;
    uint8_t bytes_in_sample = csoundlib_state->input_dtype.bytes_in_sample;
    while (fread(sample, bytes_in_sample, sizeof(char), fp) > 0) {
        memcpy(csoundlib_state->metronome.audio + jdx, sample, bytes_in_buffer);
        jdx += bytes_in_buffer;
        memset(sample, 0x00, 4);
    }
    csoundlib_state->metronome.num_bytes = jdx;

    fclose(fp);
    return SoundIoErrorNone;
} 

int read_metronome_into_buffer(char* mixed_buffer, int offset_bytes, int max_fill_bytes) {
    /* return num bytes read */

    /* currently we know the metronome buffer is always 24 bit words in 32 bit samples */
    int read_bytes = min_int(csoundlib_state->metronome.num_bytes - offset_bytes, max_fill_bytes);
    add_and_scale_audio(
        (uint8_t*)(csoundlib_state->metronome.audio + offset_bytes),
        (uint8_t*)mixed_buffer, 
        1.0, 
        read_bytes / csoundlib_state->input_dtype.bytes_in_buffer
    );
    return read_bytes;
}
