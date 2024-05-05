#ifndef AUDIO_PLAYBACK_H
#define AUDIO_PLAYBACK_H

#include <stdbool.h>

extern bool playback_started;

void lib_startPlayback();

void lib_stopPlayback();

#endif