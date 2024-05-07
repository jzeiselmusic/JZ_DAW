
#include "audio_devices.h"
#include <stdlib.h>
#include <stdio.h>
#include <stdint.h>
#include <stdarg.h>
#include <string.h>

#include "soundio_inc.h"
#include "soundlib_util.h"
#include "buffers_streams.h"
#include "callbacks.h"
#include "init.h"

#include "audio_state.h"

int lib_loadInputDevices() {
    soundio_flush_events(csoundlib_state->soundio);
    logCallback("loading all input devices");
    int num_input_devices = lib_getNumInputDevices();
    if (num_input_devices > 0) {
        struct SoundIoDevice** input_devices = malloc(num_input_devices * sizeof( struct SoundIoDevice*) );
        struct SoundIoRingBuffer** input_buffers = malloc(num_input_devices * sizeof( struct SoundIoRingBuffer*) );
        struct SoundIoInStream** input_streams = malloc(num_input_devices * sizeof( struct SoundIoInStream*) );
        bool* input_streams_started = malloc(num_input_devices * sizeof(bool));
        bool* input_streams_written = malloc(num_input_devices * sizeof(bool));
        double* list_of_rms_volume_decibel = calloc(num_input_devices, sizeof(double));
        if (!input_devices || !input_buffers || !input_streams || !input_streams_started || !list_of_rms_volume_decibel) {
            csoundlib_state->input_memory_allocated = false;
            return SoundIoErrorNoMem;
        }
        else {
            csoundlib_state->input_devices = input_devices;
            csoundlib_state->input_buffers = input_buffers;
            csoundlib_state->input_streams = input_streams;
            csoundlib_state->input_streams_started = input_streams_started;
            csoundlib_state->input_streams_written = input_streams_written;
            csoundlib_state->list_of_rms_volume_decibel = list_of_rms_volume_decibel;
            csoundlib_state->input_memory_allocated = true;
            for (int i = 0; i < num_input_devices; i++) {
                struct SoundIoDevice* device = soundio_get_input_device(csoundlib_state->soundio, i);
                if (!device) {
                    return SoundIoErrorInvalid;
                }
                else {
                    csoundlib_state->input_devices[i] = soundio_get_input_device(csoundlib_state->soundio, i);
                }
            }
        }
    }
    return SoundIoErrorNone;
}


int lib_getDefaultInputDeviceIndex() {
    /* returns -1 on error */
    soundio_flush_events(csoundlib_state->soundio);
    return soundio_default_input_device_index(csoundlib_state->soundio);
}

int lib_getNumInputDevices() {
    /* returns -1 on error */
    soundio_flush_events(csoundlib_state->soundio);
    return soundio_input_device_count(csoundlib_state->soundio);
}

char* lib_getDefaultInputDeviceName() {
    /* returns "" on error */
    int device_index = lib_getDefaultInputDeviceIndex();
    if (device_index == -1) return emptyString;
    return (csoundlib_state->input_devices)[device_index]->name;
}

char* lib_getInputDeviceName(int index) {
    /* returns "" on error */
    if (lib_checkEnvironmentAndBackendConnected() != SoundIoErrorNone) return emptyString;
    return (csoundlib_state->input_devices)[index]->name;
}

char* lib_getInputDeviceId(int index) {
    /* returns "" on error */
    if (lib_checkEnvironmentAndBackendConnected() != SoundIoErrorNone) return emptyString;
    return (csoundlib_state->input_devices)[index]->id;
}

int lib_getNumChannelsOfInputDevice(int index) {
    /* returns -1 on error */
    if (lib_checkEnvironmentAndBackendConnected() != SoundIoErrorNone) return -1;
    return (csoundlib_state->input_devices)[index]->current_layout.channel_count;
}

char* lib_getNameOfChannelOfInputDevice(int deviceIndex, int channelIndex) {
    /* returns "" on error */
    if (lib_checkEnvironmentAndBackendConnected() != SoundIoErrorNone) return emptyString;
    return 
    soundio_get_channel_name(
        (csoundlib_state->input_devices)[deviceIndex]->current_layout.channels[channelIndex]
    );
}

/********************/

/* functions for output devices */

/********************/

int lib_loadOutputDevices() {
    soundio_flush_events(csoundlib_state->soundio);
    int num_output_devices = lib_getNumOutputDevices();
    if (num_output_devices > 0) {
        struct SoundIoDevice** output_devices = malloc(num_output_devices * sizeof( struct SoundIoDevice*) );
        struct SoundIoOutStream** output_streams = malloc(num_output_devices * sizeof( struct SoundIoOutStreams*) );
        if (!output_devices || !output_streams) {
            csoundlib_state->output_memory_allocated = false;
            return SoundIoErrorNoMem;
        }
        else {
            csoundlib_state->output_memory_allocated = true;
            csoundlib_state->output_devices = output_devices;
            csoundlib_state->output_streams = output_streams;
            csoundlib_state->output_stream_started = -1;
            for (int i = 0; i < num_output_devices; i++) {
                struct SoundIoDevice* device = soundio_get_output_device(csoundlib_state->soundio, i);
                if (!device) {
                    return SoundIoErrorInvalid;
                }
                else {
                    csoundlib_state->output_devices[i] = soundio_get_output_device(csoundlib_state->soundio, i);
                }
            }
        }
    }
    return SoundIoErrorNone;
}

int lib_getDefaultOutputDeviceIndex() {
    /* returns -1 on error */
    soundio_flush_events(csoundlib_state->soundio);
    return soundio_default_output_device_index(csoundlib_state->soundio);
}

int lib_getNumOutputDevices() {
    /* returns -1 on error */
    soundio_flush_events(csoundlib_state->soundio);
    return soundio_output_device_count(csoundlib_state->soundio);
}

char* lib_getDefaultOutputDeviceName() {
    /* returns "" on error */
    if (lib_checkEnvironmentAndBackendConnected() != SoundIoErrorNone) return emptyString;
    int default_output_device_index = lib_getDefaultOutputDeviceIndex();
    return (csoundlib_state->output_devices)[default_output_device_index]->name;
}

char* lib_getOutputDeviceName(int index) {
    /* returns "" on error */
    if (lib_checkEnvironmentAndBackendConnected() != SoundIoErrorNone) return emptyString;
    return (csoundlib_state->output_devices)[index]->name;
}

char* lib_getOutputDeviceId(int index) {
    /* returns "" on error */
    if (lib_checkEnvironmentAndBackendConnected() != SoundIoErrorNone) return emptyString;
    return (csoundlib_state->output_devices)[index]->id;
}

int lib_getNumChannelsOfOutputDevice(int index) {
    /* returns -1 on error */
    if (lib_checkEnvironmentAndBackendConnected() != SoundIoErrorNone) return -1;
    return (csoundlib_state->output_devices)[index]->current_layout.channel_count;
}

char* lib_getNameOfChannelOfOutputDevice(int deviceIndex, int channelIndex) {
    /* returns "" on error */
    if (lib_checkEnvironmentAndBackendConnected() != SoundIoErrorNone) return emptyString;
    return soundio_get_channel_name(
        (csoundlib_state->output_devices)[deviceIndex]->current_layout.channels[channelIndex]
    );
}