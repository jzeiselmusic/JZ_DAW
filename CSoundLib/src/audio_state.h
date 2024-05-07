#ifndef AUDIO_STATE_H
#define AUDIO_STATE_H

#include <stdbool.h>
#include "soundio_inc.h"
#include "tracks.h"

typedef struct _audioState {
    struct SoundIo* soundio;

    /* playback */
    bool playback_started;
    int current_cursor_offset; // in samples

    /* initialization */
    bool input_memory_allocated;
    bool output_memory_allocated;
    bool environment_initialized;
    bool backend_connected;

    /* input */
    struct SoundIoDevice** input_devices;
    struct SoundIoRingBuffer** input_buffers;
    struct SoundIoInStream** input_streams;
    bool* input_streams_started;
    bool* input_streams_written;
    int num_input_streams; // defaults to 0
    int input_stream_read_write_counter;

    /* output */
    struct SoundIoDevice** output_devices;
    struct SoundIoOutStream** output_streams;
    int output_stream_started; // should intialize to -1
    bool output_stream_initialized;

    /* mixed inputs */
    char* mixed_input_buffer; // allocate to max buffer size in bytes

    /* volume */
    double* list_of_rms_volume_decibel;

    /* tracks */
    trackObject* list_of_track_objects;
    int num_tracks;

} audio_state;

extern audio_state* csoundlib_state;

#endif