#include "init.h"
#include "audio_errors.h"
#include "soundio_inc.h"
#include "buffers_streams.h"
#include "tracks.h"
#include "audio_devices.h"
#include "callbacks.h"
#include <stdlib.h>
#include "audio_state.h"

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
        free(csoundlib_state->input_buffers);
        free(csoundlib_state->input_streams);
        free(csoundlib_state->input_streams_started);
        free(csoundlib_state->input_streams_started);
        free(csoundlib_state->input_devices);
        free(csoundlib_state->list_of_rms_volume_decibel);
    }
    if (csoundlib_state->output_memory_allocated) {
        free(csoundlib_state->output_streams);
    }
    free(csoundlib_state);
}

int lib_startSession() {
    // called by host, which in turn calls initialize environment 
    logCallback("creating session");
    int err = lib_initializeEnvironment();
    if (err != SoundIoErrorNone) 
        return err;
    return  SoundIoErrorNone;
}

int lib_initializeEnvironment() {
    logCallback("creating environment");

    csoundlib_state = malloc( 1 * sizeof(audio_state) );
    struct SoundIo* soundio = soundio_create();
    char* mixed_input_buffer = calloc(MAX_BUFFER_SIZE_BYTES, sizeof(char));
    trackObject* list_of_track_objects = malloc(MAX_TRACKS * sizeof(trackObject));

    if (soundio && mixed_input_buffer && list_of_track_objects && csoundlib_state) {
        csoundlib_state->soundio = soundio;
        csoundlib_state->mixed_input_buffer = mixed_input_buffer;
        csoundlib_state->list_of_track_objects = list_of_track_objects;
        csoundlib_state->environment_initialized = true;
        return _connectToBackend();
    }
    else {
        return SoundIoErrorNoMem;
    }
}

int lib_destroySession() {
    // called by host, which in turn calls deinitialize environment 
    logCallback("destroying session");

    for (int idx = 0; idx < lib_getNumInputDevices(); idx++) {
        if (csoundlib_state->input_streams_started[idx] == true) lib_stopInputStream(idx);
    }

    for (int idx = 0; idx < lib_getNumOutputDevices(); idx++) {
        if (csoundlib_state->output_stream_started == idx) lib_stopOutputStream(idx);
    }

    int ret = lib_deinitializeEnvironment();
    _deallocateAllMemory();
    return ret;
}

int lib_deinitializeEnvironment() {
    logCallback("deinitializing environment");
    if (csoundlib_state->environment_initialized) {
        soundio_destroy(csoundlib_state->soundio);
        free(csoundlib_state->mixed_input_buffer);
        /* delete all tracks */
        for (int idx = csoundlib_state->num_tracks ; idx-- > 0 ; ) {
            lib_deleteTrack(csoundlib_state->list_of_track_objects[idx].track_id);
        }

        free(csoundlib_state->list_of_track_objects);
        csoundlib_state->environment_initialized = false;
        csoundlib_state->backend_connected = false;
        return 0;
    }
    else {
        return SoundIoErrorEnvironmentNotInitialized;
    }
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

int lib_checkEnvironmentAndBackendConnected() {
    if (!csoundlib_state->environment_initialized) {
        return SoundIoErrorEnvironmentNotInitialized;
    }
    if (!csoundlib_state->backend_connected) {
        return SoundIoErrorBackendDisconnected;
    }
    if (!csoundlib_state->input_memory_allocated) {
        return SoundIoInputMemoryNotAllocated;
    }
    if (!csoundlib_state->output_memory_allocated) {
        return SoundIoOutputMemoryNotAllocated;
    }
    return SoundIoErrorNone;
}