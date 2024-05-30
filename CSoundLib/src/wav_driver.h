#ifndef WAV_DRIVER_H
#define WAV_DRIVER_H

#include "tracks.h"

typedef struct _wavHeader {
    char chunkId[4];            // "RIFF"
    uint32_t chunkSize;         // File size - 8
    char format[4];             // "WAVE"
    char subchunk1Id[4];        // "fmt "
    uint32_t subchunk1Size;     // 16 for PCM
    uint16_t audioFormat;       // 1 for PCM
    uint16_t numChannels;       // 1 for mono
    uint32_t sampleRate;        // 44100
    uint32_t byteRate;          // sampleRate * numChannels * bitsPerSample/8
    uint16_t blockAlign;        // numChannels * bitsPerSample/8
    uint16_t bitsPerSample;     // 24
    char subchunk2Id[4];        // "data"
    uint32_t subchunk2Size;     // Number of bytes in the data chunk
} wavHeader;

int create_recording_wav_file(trackObject* track, int sample_rate, int fileId);

void stop_recording_wav_file(audioFile* file);

void thr_write_to_wav_file(trackObject* track, const char* bytes, int num_bytes);

int open_wav_for_playback(trackObject* track, audioFile* file);

void close_wav_for_playback(audioFile* file);

int read_wav_file_for_playback(trackObject* track, char* mixed_buffer, int max_bytes);

#endif