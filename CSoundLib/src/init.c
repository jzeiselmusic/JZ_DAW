#include "init.h"
#include "audio_errors.h"
#include "soundio_inc.h"
#include "buffers_streams.h"
#include "tracks.h"
#include "audio_devices.h"
#include "callbacks.h"
#include <stdlib.h>
#include "state.h"
#include <string.h>
#include "wav_driver.h"

static int _connectToBackend();
static void _deallocateAllMemory();

static int _connectToBackend() {
    int ret = soundio_connect(csoundlib_state->soundio);
    if (ret == 0) {
        csoundlib_state->backend_connected = true;
        soundio_flush_events(csoundlib_state->soundio);
    }
    return ret;
}

static void _deallocateAllMemory() {
    logCallback("deallocating memory");
    if (csoundlib_state->input_memory_allocated) {
        free(csoundlib_state->input_channel_buffers);
        free(csoundlib_state->input_devices);
    }
    if (csoundlib_state->output_memory_allocated) {
        free(csoundlib_state->output_devices);
    }
    free(csoundlib_state);
}

int lib_startSession() {
    csoundlib_state = malloc( 1 * sizeof(audio_state) );
    csoundlib_state->sample_rate = 44100;

    struct SoundIo* soundio = soundio_create();
    char* mixed_output_buffer = calloc(MAX_BUFFER_SIZE_BYTES, sizeof(char));
    trackObject* list_of_track_objects = malloc(MAX_TRACKS * sizeof(trackObject));

    if (soundio && mixed_output_buffer && list_of_track_objects && csoundlib_state) {
        csoundlib_state->soundio = soundio;
        csoundlib_state->mixed_output_buffer = mixed_output_buffer;
        csoundlib_state->list_of_track_objects = list_of_track_objects;
        csoundlib_state->environment_initialized = true;
        csoundlib_state->solo_engaged = false;
        csoundlib_state->master_volume = 1.0;
        lib_setBeatsPerMinute(120.0);
        memset(&(csoundlib_state->metronome.audio), 0x00, MAX_METRONOME_BUF_SIZE);
        csoundlib_state->metronome.enabled = false;
        csoundlib_state->metronome.num_bytes = MAX_METRONOME_BUF_SIZE;
        int err = lib_readWavFileForMetronome();
        if (err != SoundIoErrorNone) {
            return err;
        }
        return _connectToBackend();
    }
    else {
        return SoundIoErrorNoMem;
    }
}

int lib_destroySession() {
    int ret = lib_stopOutputStream();
    ret = lib_stopInputStream();
    soundio_flush_events(csoundlib_state->soundio);
    soundio_destroy(csoundlib_state->soundio);

    for (int idx = csoundlib_state->num_tracks ; idx-- > 0 ; ) {
            lib_deleteTrack(csoundlib_state->list_of_track_objects[idx].track_id);
    }

    free(csoundlib_state->list_of_track_objects);
    free(csoundlib_state->mixed_output_buffer);

    if (csoundlib_state->input_memory_allocated) {
        free(csoundlib_state->input_channel_buffers);
        free(csoundlib_state->input_devices);
    }
    if (csoundlib_state->output_memory_allocated) {
        free(csoundlib_state->output_devices);
    }

    free(csoundlib_state);
    return SoundIoErrorNone;
}

int lib_getCurrentBackend() {
    if (csoundlib_state->backend_connected) {
        soundio_flush_events(csoundlib_state->soundio);
        return csoundlib_state->soundio->current_backend;
    }
    else {
        return -1;
    }
}

int _checkEnvironmentAndBackendConnected() {
    if (!csoundlib_state->environment_initialized) {
        logCallback("uh oh. environment not initialized");
        return SoundIoErrorEnvironmentNotInitialized;
    }
    if (!csoundlib_state->backend_connected) {
        logCallback("uh oh. backend not connected");
        return SoundIoErrorBackendDisconnected;
    }
    return SoundIoErrorNone;
}

void lib_setSamplesInABeat(int samples) {
    csoundlib_state->metronome.samples_in_a_beat = samples;
}

void lib_setBeatsPerMinute(float bpm) {
    csoundlib_state->metronome.tempo = bpm;
    float beats_per_second = bpm / 60.0;
    lib_setSamplesInABeat((int)(csoundlib_state->sample_rate / beats_per_second));
}