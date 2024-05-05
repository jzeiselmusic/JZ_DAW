#include "audio_playback.h"
#include <stdbool.h>

bool playback_started = false;

void lib_startPlayback() {
    playback_started = true;
}

void lib_stopPlayback() {
    playback_started = false;
}