#ifndef AUDIO_PLAYBACK_H
#define AUDIO_PLAYBACK_H

#include <stdbool.h>
#include "tracks.h"

int lib_startPlayback();

void lib_stopPlayback();

int get_cursor_offset_samples();

void lib_updateCursorOffsetSamples(int new_offset);

int open_rec_file(trackObject* track);

#endif