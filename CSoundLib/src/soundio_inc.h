#ifndef SOUNDIO_INC_H
#define SOUNDIO_INC_H

#include <soundio/soundio.h>

#define ATTACK                                    0.001
#define RELEASE                                   0.15

#define MAX_24_BIT_SIGNED                         8388607.0
#define MAX_24_BIT_UNSIGNED                       16777215.0

extern struct SoundIo* soundio;

extern bool input_memory_allocated;
extern bool output_memory_allocated;
extern bool environment_initialized;

extern bool backend_connected;

extern char emptyString[];

#endif