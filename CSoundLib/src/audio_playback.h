#ifndef AUDIO_PLAYBACK_H
#define AUDIO_PLAYBACK_H

#include <stdbool.h>
#include "tracks.h"

#define MAX_METRONOME_BUFFER   48000*2

typedef struct _metronomeStruct {
    bool enabled;
    char audio_bytes[MAX_METRONOME_BUFFER];
    size_t num_bytes;
} metronomeStruct;

int lib_startPlayback(int fileId);

void lib_stopPlayback();

int get_cursor_offset_samples();

void lib_updateCursorOffsetSamples(int new_offset);

void lib_setMetronome(bool enabled);

#endif