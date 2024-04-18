
#include <stdio.h>
#include <stdlib.h>
#include <stdarg.h>
#include <string.h>
#include <math.h>
#include <unistd.h>
#include <pthread.h>

#include <soundio/soundio.h>
#include "soundlib.h"
#include "soundlib_util.h"

#define SoundIoErrorDevicesNotInitialized         16
#define SoundIoErrorEnvironmentNotInitialized     17
#define SoundIoErrorIndexOutOfBounds              18
#define SoundIoErrorDevicesNotLoaded              19
#define SoundIoInputStreamError                   20
#define SoundIoInputMemoryNotAllocated            21
#define SoundIoOutputMemoryNotAllocated           22

#define ATTACK                                    0.001
#define RELEASE                                   0.15

/* local static function declarations */
static void _deallocateAllMemory();
static int _connectToBackend();
static void _panic(const char *format, ...);
static void _inputStreamReadCallback(struct SoundIoInStream *instream, int frame_count_min, int frame_count_max);
static void _writeCallback(struct SoundIoOutStream *outstream, int frame_count_min, int frame_count_max);
static struct SoundIoRingBuffer** _getInputBuffers();
static struct SoundIoInStream** _getInputStreams();

static struct SoundIo* soundio;

static struct SoundIoDevice** input_devices;
        static struct SoundIoRingBuffer** input_buffers;
        static struct SoundIoInStream** input_streams;
        static bool* input_streams_started;
static struct SoundIoDevice** output_devices;
        static struct SoundIoOutStream** output_streams;
        /* will be index of output device when active, otherwise -1 */
        static int output_stream_started = -1;
        /* single output buffer for holding all output audio */
        static struct SoundIoRingBuffer* output_buffer;

struct SoundIoDevice* default_input_device;
struct SoundIoDevice* default_output_device;

static double* list_of_rms_volume_decibel;

static bool input_memory_allocated = false;
static bool output_memory_allocated = false;
static bool environment_initialized = false;
static bool backend_connected = false;

static char emptyString[] = "";
double MAX_24_BIT_SIGNED = 8388607.0;

__attribute__ ((cold))
__attribute__ ((noreturn))
__attribute__ ((format (printf, 1, 2)))
static void _panic(const char *format, ...) {
    va_list ap;
    va_start(ap, format);
    vfprintf(stderr, format, ap);
    fprintf(stderr, "\n");
    va_end(ap);
    abort();
}

static void _underflowCallback(struct SoundIoOutStream *outstream) {
    static int count = 0;
    /* perform callback function to alert audio underflow */
}

/********************/

/********************/

int lib_startSession() {
    int err = lib_initializeEnvironment();
    if (err != SoundIoErrorNone) return err;
    return  _connectToBackend();
}

int lib_initializeEnvironment() {
    soundio = soundio_create();
    if (soundio) {
        environment_initialized = true;
        return 0;
    }
    else {
        return SoundIoErrorNoMem;
    }
}

int lib_destroySession() {
    for (int idx = 0; idx < lib_getNumInputDevices(); idx++) {
        if (input_streams_started[idx] == true) lib_stopInputStream(idx);
    }
    for (int idx = 0; idx < lib_getNumOutputDevices(); idx++) {
        if (output_stream_started != -1) lib_stopOutputStream(idx);
    } 
    _deallocateAllMemory();
    return lib_deinitializeEnvironment();
}

int lib_deinitializeEnvironment() {
    if (environment_initialized) {
        soundio_destroy(soundio);
        environment_initialized = false;
        backend_connected = false;
        return 0;
    }
    else {
        return SoundIoErrorEnvironmentNotInitialized;
    }
}

static void _deallocateAllMemory() {
    if (input_memory_allocated) {
        free(input_buffers);
        free(input_streams);
        free(input_streams_started);
        free(input_devices);
        free(list_of_rms_volume_decibel);
    }
    if (output_memory_allocated) {
        free(output_streams);
    }
}

static int _connectToBackend() {
    int ret = soundio_connect(soundio);
    if (ret == 0) {
        backend_connected = true;
        soundio_flush_events(soundio);
    }
    return ret;
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

/********************/

/* functions for input devices */

/********************/

int lib_loadInputDevices() {
    soundio_flush_events(soundio);
    int num_input_devices = lib_getNumInputDevices();
    if (num_input_devices > 0) {
        input_devices = malloc(num_input_devices * sizeof( struct SoundIoDevice*) );
        input_buffers = malloc(num_input_devices * sizeof( struct SoundIoRingBuffer*) );
        input_streams = malloc(num_input_devices * sizeof( struct SoundIoInStream*) );
        input_streams_started = malloc(num_input_devices * sizeof(bool));
        list_of_rms_volume_decibel = calloc(num_input_devices, sizeof(double));
        if (!input_devices || !input_buffers || !input_streams || !input_streams_started || !list_of_rms_volume_decibel) {
            input_memory_allocated = false;
            return SoundIoErrorNoMem;
        }
        else {
            input_memory_allocated = true;
            for (int i = 0; i < num_input_devices; i++) {
                struct SoundIoDevice* device = soundio_get_input_device(soundio, i);
                if (!device) {
                    return SoundIoErrorInvalid;
                }
                else {
                    input_devices[i] = soundio_get_input_device(soundio, i);
                }
            }
        }
    }
    int index = lib_getDefaultInputDeviceIndex();
    default_input_device = input_devices[index];
    return SoundIoErrorNone;
}


int lib_getDefaultInputDeviceIndex() {
    /* returns -1 on error */
    soundio_flush_events(soundio);
    return soundio_default_input_device_index(soundio);
}

int lib_getNumInputDevices() {
    /* returns -1 on error */
    soundio_flush_events(soundio);
    return soundio_input_device_count(soundio);
}

char* lib_getDefaultInputDeviceName() {
    /* returns "" on error */
    int device_index = lib_getDefaultInputDeviceIndex();
    if (device_index == -1) return emptyString;
    return input_devices[device_index]->name;
}

char* lib_getInputDeviceName(int index) {
    /* returns "" on error */
    if (lib_checkEnvironmentAndBackendConnected() != SoundIoErrorNone) return emptyString;
    return input_devices[index]->name;
}

char* lib_getInputDeviceId(int index) {
    /* returns "" on error */
    if (lib_checkEnvironmentAndBackendConnected() != SoundIoErrorNone) return emptyString;
    return input_devices[index]->id;
}

int lib_getNumChannelsOfInputDevice(int index) {
    /* returns -1 on error */
    if (lib_checkEnvironmentAndBackendConnected() != SoundIoErrorNone) return -1;
    return input_devices[index]->current_layout.channel_count;
}

char* lib_getNameOfChannelOfInputDevice(int deviceIndex, int channelIndex) {
    /* returns "" on error */
    if (lib_checkEnvironmentAndBackendConnected() != SoundIoErrorNone) return emptyString;
    return soundio_get_channel_name(input_devices[deviceIndex]->current_layout.channels[channelIndex]);
}

/********************/

/* functions for output devices */

/********************/

int lib_loadOutputDevices() {
    soundio_flush_events(soundio);
    int num_output_devices = lib_getNumOutputDevices();
    if (num_output_devices > 0) {
        output_devices = malloc(num_output_devices * sizeof( struct SoundIoDevice*) );
        output_streams = malloc(num_output_devices * sizeof( struct SoundIoOutStreams*) );
        if (!output_devices || !output_streams) {
            output_memory_allocated = false;
            return SoundIoErrorNoMem;
        }
        else {
            output_memory_allocated = true;
            for (int i = 0; i < num_output_devices; i++) {
                struct SoundIoDevice* device = soundio_get_output_device(soundio, i);
                if (!device) {
                    return SoundIoErrorInvalid;
                }
                else {
                    output_devices[i] = soundio_get_output_device(soundio, i);
                }
            }
        }
    }
    int index = lib_getDefaultOutputDeviceIndex();
    default_output_device = output_devices[index];
    return SoundIoErrorNone;
}

int lib_getDefaultOutputDeviceIndex() {
    /* returns -1 on error */
    soundio_flush_events(soundio);
    return soundio_default_output_device_index(soundio);
}

int lib_getNumOutputDevices() {
    /* returns -1 on error */
    soundio_flush_events(soundio);
    return soundio_output_device_count(soundio);
}

char* lib_getDefaultOutputDeviceName() {
    /* returns "" on error */
    if (lib_checkEnvironmentAndBackendConnected() != SoundIoErrorNone) return emptyString;
    int default_output_device_index = lib_getDefaultOutputDeviceIndex();
    return output_devices[default_output_device_index]->name;
}

char* lib_getOutputDeviceName(int index) {
    /* returns "" on error */
    if (lib_checkEnvironmentAndBackendConnected() != SoundIoErrorNone) return emptyString;
    return output_devices[index]->name;
}

char* lib_getOutputDeviceId(int index) {
    /* returns "" on error */
    if (lib_checkEnvironmentAndBackendConnected() != SoundIoErrorNone) return emptyString;
    return output_devices[index]->id;
}

int lib_getNumChannelsOfOutputDevice(int index) {
    /* returns -1 on error */
    if (lib_checkEnvironmentAndBackendConnected() != SoundIoErrorNone) return -1;
    return output_devices[index]->current_layout.channel_count;
}

char* lib_getNameOfChannelOfOutputDevice(int deviceIndex, int channelIndex) {
    /* returns "" on error */
    if (lib_checkEnvironmentAndBackendConnected() != SoundIoErrorNone) return emptyString;
    return soundio_get_channel_name(output_devices[deviceIndex]->current_layout.channels[channelIndex]);
}

/********************/

/* functions for starting and maintaining streams */

/********************/

static void _inputStreamReadCallback(struct SoundIoInStream *instream, int frame_count_min, int frame_count_max) {
    /* gets called repeatedly every time audio data is available on this particular input stream */
    /* every input stream started gets a read callback associated with it that gets repeatedly called */

    /* this function takes the data and places it into a ring buffer associated with the input device */
    /* the write callback then has to take that data, read it, and send it to the output stream, and increment read ptr */
    int device_index = -1;
    for (int i = 0; i < lib_getNumInputDevices(); i++) {
        if (input_devices[i]->id == instream->device->id) {
            device_index = i;
            break;
        }
    }
    if (device_index == -1) {
        _panic("error finding device");
    }
    struct SoundIoRingBuffer* ring_buffer = input_buffers[device_index];
    char *write_ptr = soundio_ring_buffer_write_ptr(ring_buffer);
    int bytes_count = soundio_ring_buffer_free_count(ring_buffer);
    int frame_count = bytes_count / instream->bytes_per_frame;

    if (frame_count_min > frame_count) {
        _panic("ring buffer writer overtook reader: overflow");
    }

    int write_frames = min_int(frame_count, frame_count_max);
    int frames_left = write_frames;

    struct SoundIoChannelArea *areas;
    int err;
    for (;;) {
        int frame_count = frames_left;
        if ((err = soundio_instream_begin_read(instream, &areas, &frame_count))) {
            _panic("begin read error: %s", soundio_strerror(err));
        }
        if (!frame_count) {
            break;
        }
        if (!areas) {
            // Due to an overflow there is a hole. Fill the ring buffer with
            // silence for the size of the hole.
            memset(write_ptr, 0, frame_count * instream->bytes_per_frame);
            fprintf(stderr, "Dropped %d frames due to internal overflow\n", frame_count);
        } 
        else {
            int rms_value = 0.0;
            double double_value = 0.0;
            for (int frame = 0; frame < frame_count; frame += 1) {
                for (int ch = 0; ch < instream->layout.channel_count; ch += 1) {
                    char* bytes = areas[ch].ptr;
                    memcpy(write_ptr, bytes, instream->bytes_per_sample);
                    /* calculate rms value of current sample in channel */
                    int sample_value = (int32_t)((bytes[3] << 24) 
                                    | (bytes[2] << 16) 
                                    | (bytes[1] << 8) 
                                    | bytes[0]);
                    double temp_value = sample_value / MAX_24_BIT_SIGNED;
                    rms_value += sample_value*sample_value;
                    double_value += temp_value*temp_value;
                    /* ***************************** */
                    areas[ch].ptr += areas[ch].step;
                    write_ptr += instream->bytes_per_sample;
                }
            }
            rms_value /= frame_count;
            double_value /= frame_count;
            double current_rms_volume_decibel = (double)doubleToDecibel(envelopeFollower(sqrt(double_value), ATTACK, RELEASE));
            list_of_rms_volume_decibel[device_index] = current_rms_volume_decibel;
        }

        if ((err = soundio_instream_end_read(instream))) {
            _panic("end read error: %s\n", soundio_strerror(err));
        }
        frames_left -= frame_count;
        if (frames_left <= 0) {
            break;
        }
    }

    int advance_bytes = write_frames * instream->bytes_per_frame;
    soundio_ring_buffer_advance_write_ptr(ring_buffer, advance_bytes);
    soundio_ring_buffer_advance_read_ptr(ring_buffer, advance_bytes);
}

static void _writeCallback(struct SoundIoOutStream *outstream, int frame_count_min, int frame_count_max) { 
    /* gets called repeatedly every time audio data is ready to be posted on this particular input stream */

    /* this function takes the data in the mix buffer and places it into the stream associated with the output device */
    /* we then will increment the read ptr because we read the data placed in by the input streams */
    int device_index = -1;
    int err;
    struct SoundIoChannelArea *areas;
    for (int i = 0; i < lib_getNumInputDevices(); i++) {
        if (output_devices[i]->id == outstream->device->id) {
            device_index = i;
            break;
        }
    }
    if (device_index == -1) {
        _panic("error finding device");
    }

    /* take all the data from all the input buffers and copy it into the output buffer */
    /* the output buffer should also be polled for VU Meter levels */
    for (int input_stream_index = 0; input_stream_index < lib_getNumInputDevices(); input_stream_index++ ) {
        if (input_streams_started[input_stream_index] == true) {

            struct SoundIoInStream* instream = input_streams[input_stream_index];
            struct SoundIoRingBuffer* ring_buffer = input_buffers[input_stream_index];
            char *read_ptr = soundio_ring_buffer_read_ptr(ring_buffer);

            int fill_bytes = soundio_ring_buffer_fill_count(ring_buffer);
            int fill_count = fill_bytes / outstream->bytes_per_frame;

            if (frame_count_min > fill_count) {
                // Ring buffer does not have enough data, fill with zeroes.
                int frames_left = frame_count_min;
                for (;;) {
                    int frame_count = frames_left;
                    if (frame_count <= 0)
                        return;
                    if ((err = soundio_outstream_begin_write(outstream, &areas, &frame_count)))
                        _panic("begin write error: %s", soundio_strerror(err));
                    if (frame_count <= 0)
                        return;
                    for (int frame = 0; frame < frame_count; frame += 1) {
                        for (int ch = 0; ch < outstream->layout.channel_count; ch += 1) {
                            memset(areas[ch].ptr, 0, outstream->bytes_per_sample);
                            areas[ch].ptr += areas[ch].step;
                        }
                    }
                    if ((err = soundio_outstream_end_write(outstream)))
                        _panic("end write error: %s", soundio_strerror(err));
                    frames_left -= frame_count;
                }
            }
        }
    }
}

int lib_createInputStream(int device_index, double microphone_latency, int sample_rate) {
    int err;
    err = lib_checkEnvironmentAndBackendConnected();
    if (err != SoundIoErrorNone) return err;

    struct SoundIoDevice* input_device = input_devices[device_index];
    struct SoundIoInStream* instream = soundio_instream_create(input_device);
    if (!instream) return SoundIoErrorNoMem;

    /* signed 24 bit native endian (macos is little endian) */
    instream->format = SoundIoFormatS24NE; 
    instream->sample_rate = sample_rate;
    instream->layout = input_device->current_layout;
    instream->software_latency = microphone_latency;
    instream->read_callback = _inputStreamReadCallback;
    input_streams[device_index] = instream;

    err = soundio_instream_open(instream);
    if (err != SoundIoErrorNone) return err;

    int capacity = microphone_latency * 2.0 * instream->sample_rate * instream->bytes_per_frame;
    struct SoundIoRingBuffer* ring_buffer = soundio_ring_buffer_create(soundio, capacity);
    if (!ring_buffer) return SoundIoErrorNoMem;

    char *buf = soundio_ring_buffer_write_ptr(ring_buffer);
    int fill_count = microphone_latency * instream->sample_rate * instream->bytes_per_frame;
    memset(buf, 0, fill_count);
    soundio_ring_buffer_advance_write_ptr(ring_buffer, fill_count);
    input_buffers[device_index] = ring_buffer;
    return SoundIoErrorNone;
}

int lib_createAllInputStreams(double microphone_latency, int sample_rate) {
    /* create input streams and ring buffers for every device */
    for (int idx = 0; idx < lib_getNumInputDevices(); idx++) {
        int err = lib_createInputStream(idx, microphone_latency, sample_rate);
        if (err != SoundIoErrorNone) return err;
    }
    return SoundIoErrorNone;
}

int lib_createAndStartInputStream(int deviceIndex, double microphone_latency, int sample_rate) {
    int err;
    err = lib_createInputStream(deviceIndex, microphone_latency, sample_rate);
    if (err != SoundIoErrorNone) return err;
    if ((err = soundio_instream_start(input_streams[deviceIndex])) != SoundIoErrorNone) {
        return err;
    }
    input_streams_started[deviceIndex] = true;
    return SoundIoErrorNone;
}

int lib_stopInputStream(int deviceIndex) {
    soundio_instream_destroy(input_streams[deviceIndex]);
    input_streams_started[deviceIndex] = false;
    return SoundIoErrorNone;
}

int lib_createOutputStream(int device_index, double microphone_latency, int sample_rate) {
    int err;
    err = lib_checkEnvironmentAndBackendConnected();
    if (err != SoundIoErrorNone) return err;

    if (output_stream_started != -1) {
        soundio_outstream_destroy(output_streams[device_index]);
        soundio_ring_buffer_destroy(output_buffer);
        output_stream_started = -1;
    }

    struct SoundIoDevice* output_device = output_devices[device_index];
    struct SoundIoOutStream* outstream = soundio_outstream_create(output_device);
    if (!outstream) return SoundIoErrorNoMem;

    outstream->format = SoundIoFormatS24NE;
    outstream->sample_rate = sample_rate;
    outstream->layout = output_device->current_layout;
    outstream->software_latency = microphone_latency;
    outstream->write_callback = _writeCallback;
    outstream->underflow_callback = _underflowCallback;

    err = soundio_outstream_open(outstream);
    if (err != SoundIoErrorNone) return err;
    
    int capacity = microphone_latency * 2.0 * outstream->sample_rate * outstream->bytes_per_frame;
    struct SoundIoRingBuffer* ring_buffer = soundio_ring_buffer_create(soundio, capacity);
    if (!ring_buffer) return SoundIoErrorNoMem;
    output_buffer = ring_buffer;
    /* fill with zeros */
    char *buf = soundio_ring_buffer_write_ptr(ring_buffer);
    int fill_count = microphone_latency * outstream->sample_rate * outstream->bytes_per_frame;
    memset(buf, 0, fill_count);

    return SoundIoErrorNone;
}

int lib_createAndStartOutputStream(int deviceIndex, double microphone_latency, int sample_rate) {
    int err;
    err = lib_createOutputStream(deviceIndex, microphone_latency, sample_rate);
    if (err != SoundIoErrorNone) return err;

    if ((err = soundio_outstream_start(output_streams[deviceIndex])) != SoundIoErrorNone) {
        return err;
    }
    output_stream_started = deviceIndex;
    return SoundIoErrorNone;
}

int lib_stopOutputStream(int deviceIndex) {
    soundio_outstream_destroy(output_streams[deviceIndex]);
    soundio_ring_buffer_destroy(output_buffer);
    output_stream_started = -1;
    return SoundIoErrorNone;
}

double lib_getCurrentRmsVolume(int deviceIndex) {
    return list_of_rms_volume_decibel[deviceIndex];
}

/* miscellaneous */

static struct SoundIoRingBuffer** _getInputBuffers() {
    return input_buffers;
}

static struct SoundIoInStream** _getInputStreams() {
    return input_streams;
}

typedef void (*sig_t) (int);

void *callback_thread(void *callback) {
    sleep(5); // Wait for 5 seconds
    sig_t callback_fn = (sig_t)callback;
    callback_fn(5);
    pthread_exit(NULL);
}

void make_callback(sig_t func) {
    pthread_t thread_handler;
    pthread_create(&thread_handler, NULL, callback_thread, func);
}

