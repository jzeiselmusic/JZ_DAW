#ifndef WAV_DRIVER_H
#define WAV_DRIVER_H

#include "tracks.h"

int create_recording_wav_file(trackObject* track, int sample_rate);

void stop_recording_wav_file(audioFile* file);

void thr_write_to_wav_file(trackObject* track, const char* bytes, int num_bytes);

int open_wav_for_playback(trackObject* track, audioFile* file);

void close_wav_for_playback(audioFile* file);

#endif