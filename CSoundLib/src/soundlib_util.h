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

double log_to_mag(double log);

double mag_to_log(double mag);

#endif