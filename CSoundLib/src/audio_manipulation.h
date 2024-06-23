#ifndef AUDIO_MANIPULATION_H
#define AUDIO_MANIPULATION_H

#include <stdlib.h>

void stereo_to_mono(char* out_buffer, const char* in_buffer, size_t out_bytes, size_t in_bytes);

void mono_to_stereo(char* out_buffer, const char* in_buffer, size_t out_bytes, size_t in_bytes);

void resample(char* out_buffer, const char* in_buffer, size_t out_bytes, size_t in_bytes, int out_sample_rate, int in_sample_rate);

#endif