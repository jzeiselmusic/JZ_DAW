#ifndef TRACKS_H
#define TRACKS_H

#include <stdbool.h>
#include <stdio.h>
#include <stdint.h>

#define MAX_TRACKS              500
#define MAX_FILES               500
#define MAX_NAME_SIZE           128

typedef struct _audioFile {
    FILE* fp;
    bool is_file_open;
    int file_sample_offset; // where the file starts 
    int bytes_written;
    char file_name[MAX_NAME_SIZE];
    int file_name_len;
} audioFile;

typedef struct _trackObj {
    int track_id; // unique identifier
    audioFile* files; // pointer to list of files representing audio data to be read
    int num_files;

    bool record_enabled;
    int input_device_index; // input device currently attached to this track
    double current_rms_volume;
} trackObject;

int lib_addNewTrack(int track_id);

int lib_deleteTrack(int track_id);

int lib_trackChooseInputDevice(int trackId, int device_index);

int lib_armTrackForRecording(int trackId);

int lib_disarmTrackForRecording(int trackId);

#endif