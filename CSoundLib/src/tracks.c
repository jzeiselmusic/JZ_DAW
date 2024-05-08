
#include "tracks.h"
#include <stdio.h>
#include <stdint.h>
#include <stdlib.h>
#include "audio_errors.h"
#include "soundio_inc.h"
#include "audio_devices.h"
#include "string.h"

#include "audio_state.h"

int lib_addNewTrack(int trackId) {
    audioFile* files = malloc(MAX_FILES * sizeof(audioFile));
    if (!files) {
        return SoundIoErrorNoMem;
    }

    trackObject track =
        {
            .track_id = trackId,
            .files = files,
            .num_files = 0,

            .record_enabled = false,
            .input_device_index = lib_getDefaultInputDeviceIndex(),
            .current_rms_volume = 0.0
        };

    csoundlib_state->list_of_track_objects[csoundlib_state->num_tracks] = track;
    csoundlib_state->num_tracks += 1;
    return SoundIoErrorNone;
}

int lib_deleteTrack(int trackId) {
    for (int idx = 0; idx < csoundlib_state->num_tracks; idx++) {
        if (csoundlib_state->list_of_track_objects[idx].track_id == trackId) {
            trackObject track = csoundlib_state->list_of_track_objects[idx];
            /* close any open files to free the memory */
            for (int jdx = 0; jdx < track.num_files; jdx++) {
                if (track.files[jdx].is_file_open) {
                    fclose(track.files[jdx].fp);
                }
            }
            /* free memory that was waiting for future audio files */
            free(track.files);
            for (int jdx = idx+1; jdx < csoundlib_state->num_tracks; jdx++) {
                memcpy(&(csoundlib_state->list_of_track_objects[jdx-1]), &(csoundlib_state->list_of_track_objects[jdx]), sizeof(trackObject));
            }
            csoundlib_state->num_tracks -= 1;
            return SoundIoErrorNone;
        }
    }
    return SoundIoErrorTrackNotFound;
}

int lib_trackChooseInputDevice(int trackId, int device_index) {
    for (int idx = 0; idx < csoundlib_state->num_tracks; idx ++) {
        if (csoundlib_state->list_of_track_objects[idx].track_id == trackId) {
            csoundlib_state->list_of_track_objects[idx].input_device_index = device_index;
            return SoundIoErrorNone;
        }
    }
    return SoundIoErrorTrackNotFound;
}

int lib_armTrackForRecording(int trackId) {
    for (int idx = 0; idx < csoundlib_state->num_tracks; idx++) {
        if (csoundlib_state->list_of_track_objects[idx].track_id == trackId) {
            csoundlib_state->list_of_track_objects[idx].record_enabled = true;
            return SoundIoErrorNone;
        }
    }
    return SoundIoErrorTrackNotFound;
}

int lib_disarmTrackForRecording(int trackId) {
    for (int idx = 0; idx < csoundlib_state->num_tracks; idx++) {
        if (csoundlib_state->list_of_track_objects[idx].track_id == trackId) {
            csoundlib_state->list_of_track_objects[idx].record_enabled = false;
            return SoundIoErrorNone;
        }
    }
    return SoundIoErrorTrackNotFound;
}