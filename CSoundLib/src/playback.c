#include "playback.h"
#include "state.h"
#include "callbacks.h"
#include "errors.h"
#include "wav.h"

#include <stdbool.h>
#include <string.h>

int lib_startPlayback(int fileId) {
    /* open new audio file for each record enabled track */
    hti it = ht_iterator(csoundlib_state->track_hash_table);
    while (ht_next(&it)) {
        trackObject* track_p = (trackObject*)it.value;
        if (track_p->record_enabled) {
            /* record mode */
            /* create, open, and save new files for each track armed for recording */
            int trackId = track_p->track_id;
            int ret = create_recording_wav_file(
                    track_p, csoundlib_state->sample_rate, trackId ^ fileId);
            if (ret != SoundIoErrorNone) {
                return ret;
            }
            track_p->is_recording = true;
            track_p->is_playing_back = false;
        }
        else {
            /* playback mode */
            /* open all previously recorded files for reading */
            for (int jdx = 0; jdx < track_p->num_files; jdx++) {
                audioFile* file = track_p->files + jdx;
                int ret = open_wav_for_playback(track_p, file);
                if (ret != SoundIoErrorNone) {
                    return ret;
                }
            }
            track_p->is_playing_back = true;
            track_p->is_recording = false;
        }
    }
    csoundlib_state->playback_started = true;
    return SoundIoErrorNone;
}

void lib_stopPlayback() {
    csoundlib_state->playback_started = false;
    hti it = ht_iterator(csoundlib_state->track_hash_table);
    while (ht_next(&it)) {
        trackObject* track_p = (trackObject*)it.value;
        audioFile* files = track_p->files;
        if (track_p->is_recording) {
            /* stop and close file being written */
            stop_recording_wav_file(files + track_p->num_files - 1);
        }
        else if (track_p->is_playing_back) {
            /* close all files open for reading */
            for (int jdx = 0; jdx < track_p->num_files; jdx++) {
                close_wav_for_playback(files + jdx);
            }
        }
        track_p->current_rms_levels.input_rms_level = 0.0;
        track_p->current_rms_levels.output_rms_level = 0.0;
        track_p->is_recording = false;
        track_p->is_playing_back = false;
    }
}

int get_cursor_offset_samples() {
    return csoundlib_state->current_cursor_offset;
}

void lib_updateCursorOffsetSamples(int new_offset) {
    csoundlib_state->current_cursor_offset = new_offset;
}