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
            /* record mode */
            /* create, open, and save new files for each track armed for recording */
            int ret = create_recording_wav_file(&(csoundlib_state->list_of_track_objects[idx]), csoundlib_state->sample_rate);
            if (ret != SoundIoErrorNone) {
                return ret;
            }
            csoundlib_state->list_of_track_objects[idx].is_recording = true;
        }
        else {
            /* playback mode */
            /* open all previously recorded files for reading */
            int num_files = csoundlib_state->list_of_track_objects[idx].num_files;
            for (int jdx = 0; jdx < num_files; jdx++) {
                audioFile* file = &(csoundlib_state->list_of_track_objects[idx].files[jdx]);
                int ret = open_wav_for_playback(&(csoundlib_state->list_of_track_objects[idx]), file);
                if (ret != SoundIoErrorNone) {
                    return ret;
                }
            }
            csoundlib_state->list_of_track_objects[idx].is_playing_back = true;
        }
    }
    csoundlib_state->playback_started = true;
    return SoundIoErrorNone;
}

void lib_stopPlayback() {
    csoundlib_state->playback_started = false;
    for (int idx = 0; idx < csoundlib_state->num_tracks; idx++) {
        trackObject track = csoundlib_state->list_of_track_objects[idx];
        audioFile* files = track.files;
        if (track.is_recording) {
            /* stop and close file being written */
            stop_recording_wav_file(&(track.files[track.num_files - 1]));
            track.is_recording = false;
        }
        else if (track.is_playing_back) {
            /* close all files open for reading */
            for (int jdx = 0; jdx < track.num_files; jdx++) {
                close_wav_for_playback(&(files[jdx]));
            }
            track.is_playing_back = false;
        }
    }
}

int get_cursor_offset_samples() {
    return csoundlib_state->current_cursor_offset;
}

void lib_updateCursorOffsetSamples(int new_offset) {
    csoundlib_state->current_cursor_offset = new_offset;
}