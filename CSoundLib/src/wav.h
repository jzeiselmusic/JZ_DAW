#ifndef WAV_DRIVER_H
#define WAV_DRIVER_H

#include "tracks.h"

typedef struct _wavHeader {
    // RIFF Header
    char riff_header[4];  // Contains "RIFF"
    uint32_t wav_size;    // Size of the wav portion of the file, which follows the first 8 bytes. File size - 8
    char wave_header[4];  // Contains "WAVE"
    
    // Format Header
    char fmt_header[4];   // Contains "fmt " (includes trailing space)
    uint32_t fmt_chunk_size;  // Should be 16 for PCM
    uint16_t audio_format;    // Should be 1 for PCM. 3 for IEEE Float
    uint16_t num_channels;
    uint32_t sample_rate;
    uint32_t byte_rate;       // Number of bytes per second. sample_rate * num_channels * Bytes Per Sample
    uint16_t sample_alignment; // num_channels * Bytes Per Sample
    uint16_t bit_depth;       // Number of bits per sample
    
    // Data
    char data_header[4];  // Contains "data"
    uint32_t data_bytes;  // Number of bytes in data. Number of samples * num_channels * sample byte size
    // uint8_t bytes[];    // Remainder of wave file is bytes
} wavHeader;

int create_recording_wav_file(trackObject* track, int sample_rate, int fileId);

void stop_recording_wav_file(audioFile* file);

void thr_write_to_wav_file(trackObject* track, const char* bytes, int num_bytes);

int open_wav_for_playback(trackObject* track, audioFile* file);

void close_wav_for_playback(audioFile* file);

int read_wav_file_for_playback(trackObject* track, char* mixed_buffer, int max_bytes);

int lib_bounceMasterToWav(int start_sample_offset, int end_sample_offset);

int lib_readWavFileForMetronome();

void lib_enableMetronome(bool enabled);

int read_metronome_into_buffer(char* mixed_buffer, int offset_bytes, int max_fill);

#endif