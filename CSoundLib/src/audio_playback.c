#include "audio_playback.h"
#include "audio_state.h"
#include "callbacks.h"
#include "audio_errors.h"
#include "wav_driver.h"

#include <stdbool.h>
#include <string.h>

int lib_startPlayback() {
    /* open new audio file for each record enabled track */
    for (int idx = 0; idx < csoundlib_state->num_tracks; idx++) {
        if (csoundlib_state->list_of_track_objects[idx].record_enabled) {
            int ret = open_wav_file(&(csoundlib_state->list_of_track_objects[idx]), csoundlib_state->sample_rate);
            if (ret != SoundIoErrorNone) {
                return ret;
            }
        }
    }
    csoundlib_state->playback_started = true;
    return SoundIoErrorNone;
}

void lib_stopPlayback() {
    csoundlib_state->playback_started = false;
    for (int idx = 0; idx < csoundlib_state->num_tracks; idx++) {
        trackObject track = csoundlib_state->list_of_track_objects[idx];
        if (track.files[track.num_files].is_file_open) {
            close_wav_file(&(track.files[track.num_files]));
        }
    }
}

int get_cursor_offset_samples() {
    return csoundlib_state->current_cursor_offset;
}

void lib_updateCursorOffsetSamples(int new_offset) {
    csoundlib_state->current_cursor_offset = new_offset;
}