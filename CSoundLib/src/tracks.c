
#include "tracks.h"
#include <stdio.h>
#include <stdint.h>
#include <stdlib.h>
#include "errors.h"
#include "csl_types.h"
#include "devices.h"
#include "string.h"
#include "csl_util.h"
#include "state.h"
#include "wav.h"
#include "callbacks.h"
#include <fcntl.h>

int lib_addNewTrack(int trackId) {
    audioFile* files = malloc(MAX_FILES * sizeof(audioFile));
    if (!files) {
        return SoundIoErrorNoMem;
    }
    trackObject* tp = malloc(sizeof(trackObject));
    trackObject track =
        {
            .track_id = trackId,
            .volume = 1.0,
            .files = files,
            .num_files = 0,
            .record_enabled = false,
            .solo_enabled = false,
            .mute_enabled = false,
            .is_recording = false,
            .is_playing_back = false,
            .input_device_index = lib_getDefaultInputDeviceIndex(),
            .input_channel_index = 0,
            .current_rms_levels = {0.0, 0.0},
            .input_buffer.buffer = {0},
            .input_buffer.write_bytes = 0
        };
    *tp = track;

    /* csoundlib_state->list_of_track_objects[csoundlib_state->num_tracks] = track;
    csoundlib_state->num_tracks += 1; */
    const char key[50];
    ht_getkey(trackId, key);
    ht_set(csoundlib_state->track_hash_table, key, (void*)(tp));
    return SoundIoErrorNone;
}

int lib_deleteTrack(int trackId) {
    const char key[50];
    ht_getkey(trackId, key);
    trackObject* track_p = (trackObject*)ht_get(csoundlib_state->track_hash_table, key);
    if (track_p == NULL) return SoundIoErrorTrackNotFound;
    /* close any open files to free the memory */
    for (int jdx = 0; jdx < track_p->num_files; jdx++) {
        if (track_p->files[jdx].is_file_open) {
            close_wav_for_playback(&(track_p->files[jdx]));
            track_p->files[jdx].is_file_open = false;
            track_p->is_recording = false;
            track_p->is_playing_back = false;
        }
    }
    /* free memory that was waiting for future audio files */
    free(track_p->files);
    ht_remove(csoundlib_state->track_hash_table, key);
    csoundlib_state->num_tracks -= 1;
    return SoundIoErrorNone;
}

static int _deleteTrack(const char* key) {
    trackObject* track_p = (trackObject*)ht_get(csoundlib_state->track_hash_table, key);
    if (track_p == NULL) return SoundIoErrorTrackNotFound;
    /* close any open files to free the memory */
    for (int jdx = 0; jdx < track_p->num_files; jdx++) {
        if (track_p->files[jdx].is_file_open) {
            close_wav_for_playback(&(track_p->files[jdx]));
            track_p->files[jdx].is_file_open = false;
            track_p->is_recording = false;
            track_p->is_playing_back = false;
        }
    }
    /* free memory that was waiting for future audio files */
    free(track_p->files);
    csoundlib_state->num_tracks -= 1;
    return SoundIoErrorNone;
}

int lib_deleteFile(int trackId, int fileId) {
    const char key[50];
    ht_getkey(trackId, key);
    trackObject* track_p = (trackObject*)ht_get(csoundlib_state->track_hash_table, key);
    if (track_p == NULL) return SoundIoErrorTrackNotFound;
    audioFile* files = track_p->files;
    for (int jdx = 0; jdx < track_p->num_files; jdx++) {
        if (files[jdx].file_id == fileId) {
            int fd = fileno(files[jdx].fp);
            flock(fd, LOCK_EX);
            fclose(files[jdx].fp);
            for (int kdx = jdx + 1; kdx < track_p->num_files; kdx++) {
                memcpy(&(files[kdx-1]), &(files[kdx]), sizeof(audioFile));
                if (kdx == track_p->num_files-1) {
                    memset(&(files[kdx]), 0, sizeof(audioFile));
                }
            }
            track_p->num_files -= 1;
            flock(fd, LOCK_UN);
            return SoundIoErrorNone;
        }
    }
    return SoundIoErrorFileNotFound;
}

int lib_moveFileBetweenTracks(int destTrackId, int sourceTrackId, int sourceFileId) {
    /* find source file in source track. copy it to destination track file list. delete from source track file list */
    /* first find destination track */
    const char destTrackKey[50];
    ht_getkey(destTrackId, destTrackKey);
    trackObject* destTrack = ht_get(csoundlib_state->track_hash_table, destTrackKey);
    if (destTrack == NULL) return SoundIoErrorTrackNotFound;

    /* then find source track, move it to destination, and delete from source */
    const char srcTrackKey[50];
    ht_getkey(sourceTrackId, srcTrackKey);
    trackObject* srcTrack = ht_get(csoundlib_state->track_hash_table, srcTrackKey);
    if (srcTrack == NULL) return SoundIoErrorTrackNotFound;
    audioFile* files = srcTrack->files;
    for (int jdx = 0; jdx < srcTrack->num_files; jdx++) {
        if (files[jdx].file_id == sourceFileId) {
            memcpy(&(destTrack->files[destTrack->num_files]), &(files[jdx]), sizeof(audioFile));
            destTrack->num_files += 1;
            lib_deleteFile(sourceTrackId, sourceFileId);
            return SoundIoErrorNone;
        }
    }
    return SoundIoErrorFileNotFound;
}

int lib_trackChooseInputDevice(int trackId, int device_index) {
    const char key[50];
    ht_getkey(trackId, key);
    trackObject* track_p = (trackObject*)ht_get(csoundlib_state->track_hash_table, key);
    if (track_p == NULL) return SoundIoErrorTrackNotFound;
    track_p->input_device_index = device_index;
    return SoundIoErrorNone;
}

int lib_trackChooseInputChannel(int trackId, int channel_index) {
    const char key[50];
    ht_getkey(trackId, key);
    trackObject* track_p = (trackObject*)ht_get(csoundlib_state->track_hash_table, key);
    if (track_p == NULL) return SoundIoErrorTrackNotFound;
    track_p->input_channel_index = channel_index;
    return SoundIoErrorNone;
}

int lib_armTrackForRecording(int trackId) {
    const char key[50];
    ht_getkey(trackId, key);
    logCallback("searching for track");
    trackObject* track_p = (trackObject*)ht_get(csoundlib_state->track_hash_table, key);
    if (track_p == NULL) return SoundIoErrorTrackNotFound;
    track_p->record_enabled = true;
    return SoundIoErrorNone;
}

int lib_disarmTrackForRecording(int trackId) {
    const char key[50];
    ht_getkey(trackId, key);
    trackObject* track_p = (trackObject*)ht_get(csoundlib_state->track_hash_table, key);
    if (track_p == NULL) return SoundIoErrorTrackNotFound;
    track_p->record_enabled = false;
    return SoundIoErrorNone;
}

int lib_inputEnable(int trackId, bool enable) {
    const char key[50];
    ht_getkey(trackId, key);
    trackObject* track_p = (trackObject*)ht_get(csoundlib_state->track_hash_table, key);
    if (track_p == NULL) return SoundIoErrorTrackNotFound;
    track_p->input_enabled = enable;
    return SoundIoErrorNone;
}

float lib_getRmsVolumeTrackInput(int trackId) {
    const char key[50];
    ht_getkey(trackId, key);
    trackObject* track_p = (trackObject*)ht_get(csoundlib_state->track_hash_table, key);
    if (track_p == NULL) return 0.0;
    return track_p->current_rms_levels.input_rms_level;
}

float lib_getRmsVolumeTrackOutput(int trackId) {
    const char key[50];
    ht_getkey(trackId, key);
    trackObject* track_p = (trackObject*)ht_get(csoundlib_state->track_hash_table, key);
    if (track_p == NULL) return 0.0;
    return track_p->current_rms_levels.output_rms_level;
}

int lib_updateTrackOffset(int trackId, int fileId, int newOffset) {
    const char key[50];
    ht_getkey(trackId, key);
    trackObject* track_p = (trackObject*)ht_get(csoundlib_state->track_hash_table, key);
    if (track_p == NULL) return SoundIoErrorTrackNotFound;
    for (int jdx = 0; jdx < track_p->num_files; jdx++) {
        if (fileId == track_p->files[jdx].file_id) {
            track_p->files[jdx].file_sample_offset = newOffset;
            return SoundIoErrorNone;
        }
    }
    return SoundIoErrorFileNotFound;
}

int lib_soloEnable(int trackId) {
    const char key[50];
    ht_getkey(trackId, key);
    trackObject* track_p = (trackObject*)ht_get(csoundlib_state->track_hash_table, key);
    if (track_p == NULL) return SoundIoErrorTrackNotFound;
    track_p->solo_enabled = true;
    csoundlib_state->tracks_solod += 1;
    csoundlib_state->solo_engaged = true;
    return SoundIoErrorNone;
}

int lib_soloDisable(int trackId) {
    const char key[50];
    ht_getkey(trackId, key);
    trackObject* track_p = (trackObject*)ht_get(csoundlib_state->track_hash_table, key);
    if (track_p == NULL) return SoundIoErrorTrackNotFound;
    track_p->solo_enabled = false;
    csoundlib_state->tracks_solod -= 1;
    if (csoundlib_state->tracks_solod > 0) csoundlib_state->solo_engaged = true;
    else csoundlib_state->solo_engaged = false;
    return SoundIoErrorNone;
}

int lib_muteEnable(int trackId) {
    const char key[50];
    ht_getkey(trackId, key);
    trackObject* track_p = (trackObject*)ht_get(csoundlib_state->track_hash_table, key);
    if (track_p == NULL) return SoundIoErrorTrackNotFound;
    track_p->mute_enabled = true;
    return SoundIoErrorNone;
}

int lib_muteDisable(int trackId) {
    const char key[50];
    ht_getkey(trackId, key);
    trackObject* track_p = (trackObject*)ht_get(csoundlib_state->track_hash_table, key);
    if (track_p == NULL) return SoundIoErrorTrackNotFound;
    track_p->mute_enabled = false;
    return SoundIoErrorNone;
}

int lib_setTrackVolume(int trackId, float logVolume) {
    const char key[50];
    ht_getkey(trackId, key);
    trackObject* track_p = (trackObject*)ht_get(csoundlib_state->track_hash_table, key);
    if (track_p == NULL) return SoundIoErrorTrackNotFound;
    /* turn db volume into magnitude volume */
    float mag = log_to_mag(logVolume);
    track_p->volume = mag;
    return SoundIoErrorNone;
}

void lib_setMasterVolume(float logVolume) {
    csoundlib_state->master_volume = log_to_mag(logVolume);
}

void deleteAllTracks() {
    hti it = ht_iterator(csoundlib_state->track_hash_table);
    while( ht_next(&it) ) {
        _deleteTrack(it.key);
    }
}