#include "soundlib_util.h"

#include <math.h>
#include <stdlib.h>
#include <string.h>
#include "soundio_inc.h"
#include "callbacks.h"

/*

24 bit signed int - little endian format - stored in 32 bit buffer
< byte 1  -    byte 2     -  byte 3 -   byte 4>
<smallest - next smallest - largest -     0   >

*/

int min_int(int a, int b) {
    return (a < b) ? a : b;
}

int floatToDecibel(float input) {
    return 20*log10(input);
}

float envelopeFollower(float input, float attack, float release, float prev_envelope) {
    float attackAlpha = 1 - expf(-1.0 / (attack * 100.0));
    float releaseAlpha = 1 - expf(-1.0 / (release * 100.0));

    float envelope = 0.0;
    if (input > prev_envelope)
        envelope = attackAlpha * input + (1.0 - attackAlpha) * prev_envelope;
    else
        envelope = releaseAlpha * input + (1.0 - releaseAlpha) * prev_envelope;

    return envelope;
}

void memadd(void *dest, void *src, size_t n) {
    /* same as memcpy except add to data location instead of overwrite */
    char *csrc = (char *)src;
    char *cdest = (char *)dest;

    // Copy contents of src[] to dest[]
    for (int i = 0; i < n; i++) {
        cdest[i] += csrc[i];
    }
}

void add_and_scale_audio(const uint8_t *source, uint8_t *destination, float volume, int num_samples) {
    for (int i = 0; i < num_samples; i++) {
        int32_t src_sample = 0;
        int32_t dst_sample = 0;

        // Read 24-bit little endian samples
        for (int j = 0; j < 3; j++) {
            src_sample |= ((int32_t)source[i * 4 + j]) << (j * 8);
            dst_sample |= ((int32_t)destination[i * 4 + j]) << (j * 8);
        }

        // Sign-extend to 32 bits
        src_sample = (src_sample << 8) >> 8;
        dst_sample = (dst_sample << 8) >> 8;

        // Add samples and apply volume scaling
        int32_t result = (int32_t)((src_sample + dst_sample) * volume);

        // Clip the result to 24-bit range
        result = (result > 8388607) ? 8388607 : (result < -8388608) ? -8388608 : result;

        // Write the result back to the destination buffer
        for (int j = 0; j < 3; j++) {
            destination[i * 4 + j] = (uint8_t)(result >> (j * 8));
        }
        // The fourth byte remains unused (zero)
        destination[i * 4 + 3] = 0;
    }
}

float calculate_rms_level(const char* source, int num_bytes) {
    float rms = 0.0;
    for (int idx = 0; idx < num_bytes; idx += BYTES_PER_SAMPLE) {
        float sample = four_bytes_to_sample(source + idx);
        rms += sample * sample;
    }
    return sqrt(rms / (float)num_bytes);
}

float four_bytes_to_sample(const char* bytes) {
    int sample_value = (int32_t)
            (
                (bytes[3] << 24) | 
                (bytes[2] << 16) |
                (bytes[1] << 8)  |
                (bytes[0])
            );
    float sample_val_float = (float)sample_value;
    return sample_val_float / MAX_24_BIT_SIGNED;
}

void sample_to_four_bytes(unsigned char* bytes, float sample) { 
    int32_t sample_value = (int32_t)((sample * MAX_24_BIT_SIGNED));
    bytes[0] = (sample_value) & 0xFF;
    bytes[1] = (sample_value >> 8) & 0xFF;
    bytes[2] = (sample_value >> 16) & 0xFF;
    bytes[3] = 0x00;
}

float log_to_mag(float log) {
    return pow(10, (log / 20.0));
}

float mag_to_log(float mag) {
    return 20.0 * log10(mag);
}