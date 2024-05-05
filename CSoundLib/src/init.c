#include "init.h"
#include "audio_errors.h"
#include "soundio_inc.h"
#include "buffers_streams.h"
#include "tracks.h"
#include "audio_devices.h"
#include "callbacks.h"
#include <stdlib.h>

static int _connectToBackend() {
    int ret = soundio_connect(soundio);
    if (ret == 0) {
        backend_connected = true;
        soundio_flush_events(soundio);
    }
    return ret;
}

static void _deallocateAllMemory() {
    logCallback("deallocating memory");
    if (input_memory_allocated) {
        free(input_buffers);
        free(input_streams);
        free(input_streams_started);
        free(input_streams_started);
        free(input_devices);
        free(list_of_rms_volume_decibel);
    }
    if (output_memory_allocated) {
        free(output_streams);
    }
}

int lib_startSession() {
    logCallback("creating session");
    int err = lib_initializeEnvironment();
    if (err != SoundIoErrorNone) return err;
    return  _connectToBackend();
}

int lib_initializeEnvironment() {
    logCallback("creating environment");
    soundio = soundio_create();
    mixed_input_buffer = calloc(MAX_BUFFER_SIZE_BYTES, sizeof(char));
    list_of_track_objects = malloc(MAX_TRACKS * sizeof(trackObject));

    if (soundio && mixed_input_buffer && list_of_track_objects) {
        environment_initialized = true;
        return 0;
    }
    else {
        return SoundIoErrorNoMem;
    }
}

int lib_destroySession() {
    logCallback("destroying session");
    for (int idx = 0; idx < lib_getNumInputDevices(); idx++) {
        if (input_streams_started[idx] == true) lib_stopInputStream(idx);
    }
    for (int idx = 0; idx < lib_getNumOutputDevices(); idx++) {
        if (output_stream_started != -1) lib_stopOutputStream(idx);
    }
    int ret = lib_deinitializeEnvironment();
    _deallocateAllMemory();
    return ret;
}

int lib_deinitializeEnvironment() {
    logCallback("deinitializing environment");
    if (environment_initialized) {
        soundio_destroy(soundio);
        free(mixed_input_buffer);
        /* delete all tracks */
        for (int idx = num_tracks ; idx-- > 0 ; ) {
            lib_deleteTrack(list_of_track_objects[idx].track_id);
        }
        free(list_of_track_objects);
        environment_initialized = false;
        backend_connected = false;
        return 0;
    }
    else {
        return SoundIoErrorEnvironmentNotInitialized;
    }
}

int lib_getCurrentBackend() {
    if (backend_connected) {
        soundio_flush_events(soundio);
        return soundio->current_backend;
    }
    else {
        return -1;
    }
}

int lib_checkEnvironmentAndBackendConnected() {
    if (!environment_initialized) {
        return SoundIoErrorEnvironmentNotInitialized;
    }
    if (!backend_connected) {
        return SoundIoErrorBackendDisconnected;
    }
    if (!input_memory_allocated) {
        return SoundIoInputMemoryNotAllocated;
    }
    if (!output_memory_allocated) {
        return SoundIoOutputMemoryNotAllocated;
    }
    return SoundIoErrorNone;
}