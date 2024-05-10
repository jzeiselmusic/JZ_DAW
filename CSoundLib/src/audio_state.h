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
    struct SoundIoRingBuffer** input_channel_buffers; // keep a ring buffer for every channel audio
    int num_channels_available;
    struct SoundIoInStream* input_stream;
    bool input_stream_started;
    bool input_stream_written;

    /* output */
    struct SoundIoDevice** output_devices;
    struct SoundIoOutStream* output_stream;
    bool output_stream_started; // should intialize to -1
    bool output_stream_initialized;

    /* mixed inputs */
    char* mixed_output_buffer; // every channel of data that is enabled gets mixed into output buffer

    /* tracks */
    trackObject* list_of_track_objects;
    int num_tracks;

} audio_state;

extern audio_state* csoundlib_state;

#endif