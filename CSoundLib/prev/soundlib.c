
#include <stdio.h>
#include <stdint.h>
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
#define SoundIoErrorTrackNotFound                 23

#define ATTACK                                    0.001
#define RELEASE                                   0.15

#define BYTES_PER_FRAME_MONO                      4
#define BYTES_PER_FRAME_STEREO                    8
#define DEFAULT_BUFFER_SIZE                       64
#define MAX_BUFFER_SIZE_BYTES                     8192
#define MAX_TRACKS                                500

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
        static bool* input_streams_written;
        static int num_input_streams = 0;
        static int input_stream_read_write_counter = 0;
static struct SoundIoDevice** output_devices;
        static struct SoundIoOutStream** output_streams;
        /* will be index of output device when active, otherwise -1 */
        static int output_stream_started = -1;
        static bool output_stream_initialized = false;
static char* mixed_input_buffer; // allocate to max buffer size in bytes

static double* list_of_rms_volume_decibel;

static bool input_memory_allocated = false;
static bool output_memory_allocated = false;
static bool environment_initialized = false;
static bool backend_connected = false;

static char emptyString[] = "";

static soundLibCallback logCallback;
static soundLibCallback panicCallback;
static soundStreamCallback inputStreamCallback;
static soundStreamCallback outputStreamCallback;
static floatPrintCallback audioStreamCallback;
static charCallback audioStreamCallbackChar;
static outputProcessedCallback outputProcessed;

static bool playback_started = false;
static int num_tracks = 0;
static trackObject* list_of_track_objects;

__attribute__ ((cold))
__attribute__ ((noreturn))
__attribute__ ((format (printf, 1, 2)))
static void _panic(const char *format, ...) {
    panicCallback(format);
    abort();
}

__attribute__ ((cold))
__attribute__ ((format (printf, 1, 2)))
static void _formatPanic(const char *format, ...) {
    panicCallback(format);
}

static void _underflowCallback(struct SoundIoOutStream *outstream) {
    static int count = 0;
    logCallback("underflow reported");
}

void registerAudioPanicCallback(soundLibCallback func) {
    panicCallback = func;
}

void registerAudioLogCallback(soundLibCallback func) {
    logCallback = func;
}

void registerInputStreamCallback(soundStreamCallback func) {
    inputStreamCallback = func;
}

void registerOutputStreamCallback(soundStreamCallback func) {
    outputStreamCallback = func;
}

void registerFloatPrintCallback(floatPrintCallback func) {
    audioStreamCallback = func;
}

void registerCharCallback(charCallback func) {
    audioStreamCallbackChar = func;
}

void registerOutputProcessedCallback(outputProcessedCallback func) {
    outputProcessed = func;
}

/********************/

/********************/

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

void lib_startPlayback() {
    playback_started = true;
}

void lib_stopPlayback() {
    playback_started = false;
}

/********************/

/* functions for input devices */

/********************/

int lib_loadInputDevices() {
    soundio_flush_events(soundio);
    logCallback("loading all input devices");
    int num_input_devices = lib_getNumInputDevices();
    if (num_input_devices > 0) {
        input_devices = malloc(num_input_devices * sizeof( struct SoundIoDevice*) );
        input_buffers = malloc(num_input_devices * sizeof( struct SoundIoRingBuffer*) );
        input_streams = malloc(num_input_devices * sizeof( struct SoundIoInStream*) );
        input_streams_started = malloc(num_input_devices * sizeof(bool));
        input_streams_written = malloc(num_input_devices * sizeof(bool));
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
    /* gets called repeatedly every time audio data is available to be read on this particular input stream */

    /* every input stream started gets a read callback associated with it that gets repeatedly called */

    /* this function takes the data its received and writes it into the mix buffer */
    /* once all input streams have written to the mix buffer, increment write pointer */
    /* the write callback then has to take that data, read it, and send it to the output stream, and increment read ptr */
    int device_index = -1;
    while(input_streams_written[device_index] == true) {

    }
    // inputStreamCallback("sample rate", instream->sample_rate);
    // inputStreamCallback("bytes per frame", instream->bytes_per_frame);
    // inputStreamCallback("bytes per sample", instream->bytes_per_sample);
    for (int i = 0; i < lib_getNumInputDevices(); i++) {
        if (input_devices[i]->id == instream->device->id) {
            device_index = i;
            break;
        }
    }
    if (device_index == -1) {
        _panic("error finding device");
    }
    if (output_stream_initialized == false) {
        _panic("output device not initialized");
    }
    // inputStreamCallback("input device read data", device_index);

    struct SoundIoRingBuffer* ring_buffer = input_buffers[device_index];
    /* get the write ptr for this inputs ring buffer */
    char *write_ptr = soundio_ring_buffer_write_ptr(ring_buffer);
    // inputStreamCallback("current pointer address to write", (unsigned long)write_ptr);
    int bytes_count = soundio_ring_buffer_free_count(ring_buffer);
    int frame_count = bytes_count / BYTES_PER_FRAME_MONO;

    if (frame_count_min > frame_count) {
        _panic("ring buffer writer overtook reader");
    }

    int write_frames = min_int(frame_count, frame_count_max);
    // inputStreamCallback("frames to write", write_frames);
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
            /* Due to an overflow there is a hole. Fill the ring buffer with
               silence for the size of the hole.  */
            memset(write_ptr, 0, frame_count * BYTES_PER_FRAME_MONO);
            _formatPanic("Dropped %d frames due to internal overflow\n", frame_count);
        } 
        else {
            /* normal operation */
            int rms_value = 0.0;
            double double_value = 0.0;
            for (int frame = 0; frame < frame_count; frame += 1) {
                for (int ch = 0; ch < instream->layout.channel_count; ch += 1) {
                    char* bytes = areas[ch].ptr;

                    memcpy(write_ptr, bytes, instream->bytes_per_sample);

                    /* ***************************** */
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
                }
                write_ptr += instream->bytes_per_sample;
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
    int advance_bytes = write_frames * BYTES_PER_FRAME_MONO;
    // inputStreamCallback("wrote frames", write_frames);
    // inputStreamCallback("advancing bytes", advance_bytes);
    soundio_ring_buffer_advance_write_ptr(ring_buffer, advance_bytes);
    // inputStreamCallback("posted my audio", device_index);
    input_streams_written[device_index] = true;
    input_stream_read_write_counter += 1;
}

static void _outputStreamWriteCallback(struct SoundIoOutStream *outstream, int frame_count_min, int frame_count_max) {
    /* gets called repeatedly every time audio data is ready to be posted to this particular output stream */

    /* this function takes the data in the mix buffer and places it into the stream associated with the output device */
    /* we then will increment the read ptr because we read the data placed in by the input streams */
    int frames_left;
    int frame_count;
    int err;
    struct SoundIoChannelArea *areas;
    struct SoundIoRingBuffer* ring_buffer;

    /* search for device index of this output stream */
    int device_index = -1;
    for (int i = 0; i < lib_getNumInputDevices(); i++) {
        if (output_devices[i]->id == outstream->device->id) {
            device_index = i;
            break;
        }
    }
    if (device_index == -1) {
        return;
    }
    if (output_stream_initialized == false) {
        return;
    }
    // outputStreamCallback("ready for audio", device_index);
    if (input_stream_read_write_counter < num_input_streams) {
        // outputStreamCallback("waiting for more streams to post", device_index);
    }
    /* wait for streams to be finished writing to their buffers */
    while(input_stream_read_write_counter < num_input_streams) {}
    input_stream_read_write_counter = 0;
    /* input streams are ready to read */

    /* clear mixed input buffer */
    memset(mixed_input_buffer, 0, MAX_BUFFER_SIZE_BYTES);

    /* go through each input stream and read to a buffer if it has been written */
    int max_fill_count = 0;
    for (int inputStreamIdx = 0; inputStreamIdx < lib_getNumInputDevices(); inputStreamIdx++) {
        if (input_streams_started[inputStreamIdx] == true) {
            /* wait for input stream to be written before reading */
            while (input_streams_written[inputStreamIdx] == false) {};

            ring_buffer = input_buffers[inputStreamIdx];
            char *read_ptr = soundio_ring_buffer_read_ptr(ring_buffer);
            /* number of bytes available for reading */
            int fill_bytes = soundio_ring_buffer_fill_count(ring_buffer);
            int fill_count = fill_bytes / BYTES_PER_FRAME_MONO;
            if (fill_count > max_fill_count) max_fill_count = fill_count;

            add_audio_buffers_24bitNE(mixed_input_buffer, read_ptr, fill_bytes);
        }
    }

    /* now place data from mixed input buffer into output stream */
    int read_count = min_int(frame_count_max, max_fill_count);
    /* handle case of no input streams */
    if (read_count == 0) read_count = frame_count_min;
    /* there is data to be read to output */
    frames_left = read_count;
    char* mixed_read_ptr = mixed_input_buffer;
    while (frames_left > 0) {
        int frame_count = frames_left;
        if ((err = soundio_outstream_begin_write(outstream, &areas, &frame_count)))
            _panic("begin write error: %s", soundio_strerror(err));
        if (frame_count <= 0)
            break;

        for (int frame = 0; frame < frame_count; frame += 1) {
            for (int ch = 0; ch < outstream->layout.channel_count; ch += 1) {
                memcpy(areas[ch].ptr, mixed_read_ptr, outstream->bytes_per_sample);
                areas[ch].ptr += areas[ch].step;
            }
            mixed_read_ptr += outstream->bytes_per_sample;
        }
        if ((err = soundio_outstream_end_write(outstream)))
            _panic("end write error: %s", soundio_strerror(err));

        frames_left -= frame_count;
    }
    for (int inputStreamIdx = 0; inputStreamIdx < lib_getNumInputDevices(); inputStreamIdx++) {
        if (input_streams_started[inputStreamIdx] == true) {
            ring_buffer = input_buffers[inputStreamIdx];
            soundio_ring_buffer_advance_read_ptr(ring_buffer, read_count * BYTES_PER_FRAME_MONO);
            input_streams_written[inputStreamIdx] = false;
        }
    }
    if (playback_started) outputProcessed(read_count);
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

    int capacity = DEFAULT_BUFFER_SIZE * instream->bytes_per_sample;
    struct SoundIoRingBuffer* ring_buffer = soundio_ring_buffer_create(soundio, capacity);
    if (!ring_buffer) return SoundIoErrorNoMem;

    char *buf = soundio_ring_buffer_write_ptr(ring_buffer);
    int fill_count = soundio_ring_buffer_capacity(ring_buffer);
    memset(buf, 0, fill_count);
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

int lib_createAndStartInputStream(int device_index, double microphone_latency, int sample_rate) {
    int err;
    err = lib_createInputStream(device_index, microphone_latency, sample_rate);
    if (err != SoundIoErrorNone) return err;
    if ((err = soundio_instream_start(input_streams[device_index])) != SoundIoErrorNone) {
        return err;
    }
    input_streams_started[device_index] = true;
    input_streams_written[device_index] = false;
    num_input_streams += 1;
    return SoundIoErrorNone;
}

int lib_stopInputStream(int device_index) {
    input_streams_started[device_index] = false;
    input_streams_written[device_index] = false;
    num_input_streams -= 1;
    soundio_instream_destroy(input_streams[device_index]);
    return SoundIoErrorNone;
}

int lib_createOutputStream(int device_index, double microphone_latency, int sample_rate) {
    int err;
    err = lib_checkEnvironmentAndBackendConnected();
    if (err != SoundIoErrorNone) return err;

    if (output_stream_started != -1) {
        soundio_outstream_destroy(output_streams[device_index]);
        output_stream_started = -1;
    }

    struct SoundIoDevice* output_device = output_devices[device_index];
    struct SoundIoOutStream* outstream = soundio_outstream_create(output_device);
    if (!outstream) return SoundIoErrorNoMem;

    outstream->format = SoundIoFormatS24NE;
    outstream->sample_rate = sample_rate;
    outstream->layout = output_device->current_layout;
    outstream->software_latency = microphone_latency;
    outstream->write_callback = _outputStreamWriteCallback;
    outstream->underflow_callback = _underflowCallback;
    output_streams[device_index] = outstream;

    err = soundio_outstream_open(outstream);
    if (err != SoundIoErrorNone) return err;
    output_stream_initialized = true;

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
    output_stream_started = -1;
    output_stream_initialized = false;
    soundio_outstream_destroy(output_streams[deviceIndex]);
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

/* handling tracks */

int lib_addNewTrack(int trackId) {
    FILE** file_ptrs = malloc(MAX_TRACKS * sizeof(FILE*));
    uint32_t* offset_ptrs = malloc(MAX_TRACKS * sizeof(uint32_t));
    uint32_t* byte_ptrs = malloc(MAX_TRACKS * sizeof(uint32_t));
    if (!file_ptrs || !offset_ptrs || !byte_ptrs) {
        return SoundIoErrorNoMem;
    }

    trackObject track =
        {
            .track_id = trackId,
            .files = file_ptrs,
            .file_sample_offsets = offset_ptrs,
            .file_num_bytes = byte_ptrs,
            .num_files = 0,

            .record_enabled = false,
            .input_device_index = lib_getDefaultInputDeviceIndex(),
            .current_rms_volume = 0.0
        };

    list_of_track_objects[num_tracks] = track;
    num_tracks += 1;
    return SoundIoErrorNone;
}

int lib_deleteTrack(int trackId) {
    for (int idx = 0; idx < num_tracks; idx++) {
        if (list_of_track_objects[idx].track_id == trackId) {
            trackObject track = list_of_track_objects[idx];
            free(track.files);
            free(track.file_sample_offsets);
            free(track.file_num_bytes);
            for (int jdx = idx+1; jdx < num_tracks; jdx++) {
                memcpy(&list_of_track_objects[jdx-1], &list_of_track_objects[jdx], sizeof(trackObject));
            }
            num_tracks -= 1;
            return SoundIoErrorNone;
        }
    }
    return SoundIoErrorTrackNotFound;
}

int lib_trackChooseInputDevice(int trackId, int device_index) {
    for (int idx = 0; idx < num_tracks; idx ++) {
        if (list_of_track_objects[idx].track_id == trackId) {
            list_of_track_objects[idx].input_device_index = device_index;
            return SoundIoErrorNone;
        }
    }
    return SoundIoErrorTrackNotFound;
}




