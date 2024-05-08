#include "audio_playback.h"
#include "audio_state.h"
#include "callbacks.h"
#include "audio_errors.h"

#include <stdbool.h>
#include <string.h>

int lib_startPlayback() {
    /* open new audio file for each record enabled track */
    for (int idx = 0; idx < csoundlib_state->num_tracks; idx++) {
        if (csoundlib_state->list_of_track_objects[idx].record_enabled) {
            int ret = open_rec_file(&(csoundlib_state->list_of_track_objects[idx]));
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
}

int get_cursor_offset_samples() {
    return csoundlib_state->current_cursor_offset;
}

void lib_updateCursorOffsetSamples(int new_offset) {
    csoundlib_state->current_cursor_offset = new_offset;
}

int open_rec_file(trackObject* track) {
    /* open a new audio file for writing for track with id == trackid */
    const int bufferSize = MAX_NAME_SIZE;
    char buffer[bufferSize];
    const char *file_name;

    int charsWritten = sprintf(
                    buffer, 
                    "/tmp/JZDAW/track-%d-offset-%d.jz", 
                    track->track_id, 
                    csoundlib_state->current_cursor_offset);
    if (charsWritten == 0 || charsWritten >= bufferSize) {
        return SoundIoErrorOpeningFile;
    }

    file_name = buffer;
    FILE* fp = fopen(file_name, "wb");
    if (!fp) {
        return SoundIoErrorOpeningFile;
    }

    audioFile file_obj = {
        .fp = fp,
        .is_file_open = true,
        .file_sample_offset = csoundlib_state->current_cursor_offset,
        .bytes_written = 0
    };

    strncpy(file_obj.file_name, buffer, charsWritten);
    file_obj.file_name_len = charsWritten;

    track->files[track->num_files] = file_obj;
    track->num_files += 1;
    return SoundIoErrorNone;
}