#ifndef AUDIO_STATE_H
#define AUDIO_STATE_H

#include <stdbool.h>
#include "soundio_inc.h"
#include "tracks.h"
#include "audio_playback.h"

#define MAX_METRONOME_BUF_SIZE   44100 * 3 * 4 // 4 seconds at 44.1k 24 bit

typedef struct _metronome {
    bool enabled;
    char audio[MAX_METRONOME_BUF_SIZE];
    size_t num_bytes;
} Metronome;

typedef struct _audioState {
    struct SoundIo* soundio;
    int sample_rate; 

    /* playback */
    bool playback_started;
    int current_cursor_offset; // in samples
    int samples_in_a_beat;
    Metronome metronome;

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
    double current_rms_ouput;

    /* tracks */
    trackObject* list_of_track_objects;
    int num_tracks;

    /* solo and mute */
    bool solo_engaged;

} audio_state;

extern audio_state* csoundlib_state;

double lib_getCurrentRmsOutput(void);

#endif