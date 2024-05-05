#include "audio_playback.h"
#include <stdbool.h>
#include "audio_state.h"

void lib_startPlayback() {
    csoundlib_state->playback_started = true;
}

void lib_stopPlayback() {
    csoundlib_state->playback_started = false;
}