#ifndef SOUNDLIB_UTIL_H 
#define SOUNDLIB_UTIL_H

#include <math.h>

int min_int(int a, int b) {
    return (a < b) ? a : b;
}

int doubleToDecibel(double input) {
    return 20*log10(input);
}

float envelopeFollower(double input, double attack, double release) {
    static double prevEnvelope = 0;
    double attackAlpha = 1 - expf(-1.0 / (attack * 100));
    double releaseAlpha = 1 - expf(-1.0 / (release * 100));

    double envelope = 0;
    if (input > prevEnvelope)
        envelope = attackAlpha * input + (1 - attackAlpha) * prevEnvelope;
    else
        envelope = releaseAlpha * input + (1 - releaseAlpha) * prevEnvelope;

    prevEnvelope = envelope; 

    return envelope;
}

void memadd(void *dest, void *src, size_t n) {
    /* same as memcpy except add to data location instead of overwrite */
    char *csrc = (char *)src;
    char *cdest = (char *)dest;

    // Copy contents of src[] to dest[]
    for (int i=0; i<n; i++) {
        cdest[i] += csrc[i];
    }
}

#endif