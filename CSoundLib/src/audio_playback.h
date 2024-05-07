#ifndef AUDIO_PLAYBACK_H
#define AUDIO_PLAYBACK_H

#include <stdbool.h>

void lib_startPlayback();

void lib_stopPlayback();

int get_cursor_offset_samples();

void lib_updateCursorOffsetSamples(int new_offset);

#endif