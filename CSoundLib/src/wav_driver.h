#ifndef WAV_DRIVER_H
#define WAV_DRIVER_H

#include "tracks.h"

int open_wav_file(trackObject* track, int sample_rate);

void close_wav_file(audioFile* file);

void thr_write_to_wav_file(trackObject* track, const char* bytes, int num_bytes);

#endif