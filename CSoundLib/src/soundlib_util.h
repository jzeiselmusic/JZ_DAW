#ifndef SOUNDLIB_UTIL_H 
#define SOUNDLIB_UTIL_H

#include <math.h>
#include <stdlib.h>
#include <string.h>

int min_int(int a, int b);

int floatToDecibel(float input);

float envelopeFollower(float input, float attack, float release, float prev_envelope);

void memadd(void *dest, void *src, size_t n);

void add_and_scale_audio(const uint8_t *source, uint8_t *destination, float volume, int num_samples);

float calculate_rms_level(const char* source, int num_bytes);

float four_bytes_to_sample(const char* bytes);

void sample_to_four_bytes(unsigned char* bytes, float sample);

float log_to_mag(float log);

float mag_to_log(float mag);

#endif