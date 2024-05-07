#ifndef TRACKS_H
#define TRACKS_H

#include <stdbool.h>
#include <stdio.h>
#include <stdint.h>

#define MAX_TRACKS                                500

typedef struct _trackObj {
    uint32_t track_id; // unique identifier
    FILE** files; // pointer to list of file pointers representing audio data to be read
    uint32_t* file_sample_offsets; // sample offsets where these files begin
    uint32_t* file_num_bytes; // number of bytes to read for each audio file
    uint32_t num_files;

    bool record_enabled;
    uint8_t input_device_index; // input device currently attached to this track
    double current_rms_volume;
} trackObject;

int lib_addNewTrack(int track_id);

int lib_deleteTrack(int track_id);

int lib_trackChooseInputDevice(int trackId, int device_index);

int lib_armTrackForRecording(int trackId);

int lib_disarmTrackForRecording(int trackId);

int open_rec_file(int trackId);

#endif