#include "audio_playback.h"
#include <stdbool.h>
#include "audio_state.h"
#include "callbacks.h"

void lib_startPlayback() {
    csoundlib_state->playback_started = true;
}

void lib_stopPlayback() {
    csoundlib_state->playback_started = false;
}

int get_cursor_offset_samples() {
    return csoundlib_state->current_cursor_offset;
}

void lib_updateCursorOffsetSamples(int new_offset) {
    csoundlib_state->current_cursor_offset = new_offset;
}