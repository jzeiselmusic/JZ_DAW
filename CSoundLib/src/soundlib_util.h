#ifndef SOUNDLIB_UTIL_H 
#define SOUNDLIB_UTIL_H

#include <math.h>
#include <stdlib.h>
#include <string.h>

int min_int(int a, int b);

int doubleToDecibel(double input);

float envelopeFollower(double input, double attack, double release, double prev_envelope);

void memadd(void *dest, void *src, size_t n);

void add_audio_buffers_24bitNE(char* dest, const char* source, int num_bytes);

double calculate_rms_level(const char* source, int num_bytes);

double four_bytes_to_sample(const char* bytes);

void stereo_to_mono(size_t sample_width, char* mono_buffer, const char* stereo_buffer, size_t* mono_bytes, size_t stereo_bytes);

void mono_to_stereo(size_t sample_width, char* stereo_buffer, const char* mono_buffer, size_t* stereo_bytes, size_t mono_bytes);

int mono_resample(size_t sample_width, char* out_buffer, const char* in_buffer, size_t* out_bytes, size_t in_bytes, int out_sample_rate, int in_sample_rate);

void c16bit_to_24bit(const int16_t* input, int32_t* output, size_t sample_count);

#endif