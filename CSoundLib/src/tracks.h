#ifndef TRACKS_H
#define TRACKS_H

#include <stdbool.h>
#include <stdio.h>
#include <stdint.h>
#include "buffers_streams.h"

#define MAX_TRACKS              500
#define MAX_FILES               500
#define MAX_NAME_SIZE           128

typedef struct _inputBuffer {
    char buffer[MAX_BUFFER_SIZE_BYTES];
    size_t write_bytes;
} inputBuffer;

typedef struct _audioFile {
    FILE* fp;
    int file_id;
    bool is_file_open;
    int file_sample_offset; // where the file starts 
    int samples_written;
    char file_name[MAX_NAME_SIZE];
    int file_name_len;
} audioFile;

typedef struct _trackObj {
    int track_id; // unique identifier
    audioFile* files; // pointer to list of files representing audio data to be read
    int num_files;
    bool input_enabled;
    bool record_enabled;
    bool solo_enabled;
    bool mute_enabled;
    bool is_recording;
    bool is_playing_back;
    int input_device_index; // input device currently attached to this track
    int input_channel_index;
    double current_rms_volume_input_stream;
    double current_rms_volume_track_playback;
    inputBuffer input_buffer;
} trackObject;

int lib_addNewTrack(int track_id);

int lib_deleteTrack(int track_id);

int lib_deleteFile(int trackId, int fileId);

int lib_moveFileBetweenTracks(int destTrackId, int sourceTrackId, int sourceFileId);

int lib_trackChooseInputDevice(int trackId, int device_index);

int lib_trackChooseInputChannel(int trackId, int channel_index);

int lib_armTrackForRecording(int trackId);

int lib_disarmTrackForRecording(int trackId);

int lib_inputEnable(int trackId, bool enable);

double lib_getRmsVolumeInputStream(int trackId);

double lib_getRmsVolumeTrackPlayback(int trackId);

int lib_updateTrackOffset(int trackId, int fileId, int newOffset);

int lib_soloEnable(int trackId);

int lib_soloDisable(int trackId);

int lib_muteEnable(int trackId);

int lib_muteDisable(int trackId);

#endif