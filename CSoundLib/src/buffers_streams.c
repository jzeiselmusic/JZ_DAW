
#include "soundio_inc.h"
#include "buffers_streams.h"
#include <stdbool.h>
#include "audio_devices.h"
#include "stdint.h"
#include "soundlib_util.h"
#include "tracks.h"
#include "audio_playback.h"
#include "callbacks.h"
#include "init.h"

#include "audio_state.h"

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

static void _inputStreamReadCallback(struct SoundIoInStream *instream, int frame_count_min, int frame_count_max) {
    /* gets called repeatedly every time audio data is available to be read on this particular input stream */

    /* every input stream started gets a read callback associated with it that gets repeatedly called */

    /* this function takes the data its received and writes it into the mix buffer */
    /* once all input streams have written to the mix buffer, increment write pointer */
    /* the write callback then has to take that data, read it, and send it to the output stream, and increment read ptr */
    int device_index = -1;
    while(csoundlib_state->input_streams_written[device_index] == true) {

    }

    for (int i = 0; i < lib_getNumInputDevices(); i++) {
        if ((csoundlib_state->input_devices)[i]->id == instream->device->id) {
            device_index = i;
            break;
        }
    }
    if (device_index == -1) {
        _panic("error finding device");
    }
    if (csoundlib_state->output_stream_initialized == false) {
        _panic("output device not initialized");
    }

    struct SoundIoRingBuffer* ring_buffer = csoundlib_state->input_buffers[device_index];
    /* get the write ptr for this inputs ring buffer */
    char *write_ptr = soundio_ring_buffer_write_ptr(ring_buffer);
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
            csoundlib_state->list_of_rms_volume_decibel[device_index] = current_rms_volume_decibel;
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
    csoundlib_state->input_streams_written[device_index] = true;
    csoundlib_state->input_stream_read_write_counter += 1;
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
        if (csoundlib_state->output_devices[i]->id == outstream->device->id) {
            device_index = i;
            break;
        }
    }
    if (device_index == -1) {
        return;
    }
    if (csoundlib_state->output_stream_initialized == false) {
        return;
    }

    if (csoundlib_state->input_stream_read_write_counter < csoundlib_state->num_input_streams) {
        // null
    }

    while(csoundlib_state->input_stream_read_write_counter < csoundlib_state->num_input_streams) {
        // wait
    }
    
    csoundlib_state->input_stream_read_write_counter = 0;
    /* input streams are ready to read */

    /* clear mixed input buffer */
    memset(csoundlib_state->mixed_input_buffer, 0, MAX_BUFFER_SIZE_BYTES);

    /* go through each input stream and read to a buffer if it has been written */
    int max_fill_count = 0;
    for (int inputStreamIdx = 0; inputStreamIdx < lib_getNumInputDevices(); inputStreamIdx++) {
        if (csoundlib_state->input_streams_started[inputStreamIdx] == true) {
            /* wait for input stream to be written before reading */
            while (csoundlib_state->input_streams_written[inputStreamIdx] == false) {};

            ring_buffer = csoundlib_state->input_buffers[inputStreamIdx];
            char *read_ptr = soundio_ring_buffer_read_ptr(ring_buffer);
            /* number of bytes available for reading */
            int fill_bytes = soundio_ring_buffer_fill_count(ring_buffer);
            int fill_count = fill_bytes / BYTES_PER_FRAME_MONO;
            if (fill_count > max_fill_count) max_fill_count = fill_count;

            add_audio_buffers_24bitNE(csoundlib_state->mixed_input_buffer, read_ptr, fill_bytes);
        }
    }

    /* now place data from mixed input buffer into output stream */
    int read_count = min_int(frame_count_max, max_fill_count);
    /* handle case of no input streams */
    if (read_count == 0) read_count = frame_count_min;
    /* there is data to be read to output */
    frames_left = read_count;
    char* mixed_read_ptr = csoundlib_state->mixed_input_buffer;
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
        if (csoundlib_state->input_streams_started[inputStreamIdx] == true) {
            ring_buffer = csoundlib_state->input_buffers[inputStreamIdx];
            soundio_ring_buffer_advance_read_ptr(ring_buffer, read_count * BYTES_PER_FRAME_MONO);
            csoundlib_state->input_streams_written[inputStreamIdx] = false;
        }
    }
    if (csoundlib_state->playback_started) outputProcessed(read_count);
}

int lib_createInputStream(int device_index, double microphone_latency, int sample_rate) {
    int err;
    err = lib_checkEnvironmentAndBackendConnected();
    if (err != SoundIoErrorNone) return err;

    struct SoundIoDevice* input_device = csoundlib_state->input_devices[device_index];
    struct SoundIoInStream* instream = soundio_instream_create(input_device);
    if (!instream) return SoundIoErrorNoMem;

    /* signed 24 bit native endian (macos is little endian) */
    instream->format = SoundIoFormatS24NE; 
    instream->sample_rate = sample_rate;
    instream->layout = input_device->current_layout;
    instream->software_latency = microphone_latency;
    instream->read_callback = _inputStreamReadCallback;
    csoundlib_state->input_streams[device_index] = instream;

    err = soundio_instream_open(instream);
    if (err != SoundIoErrorNone) return err;

    int capacity = DEFAULT_BUFFER_SIZE * instream->bytes_per_sample;
    struct SoundIoRingBuffer* ring_buffer = soundio_ring_buffer_create(csoundlib_state->soundio, capacity);
    if (!ring_buffer) return SoundIoErrorNoMem;

    char *buf = soundio_ring_buffer_write_ptr(ring_buffer);
    int fill_count = soundio_ring_buffer_capacity(ring_buffer);
    memset(buf, 0, fill_count);
    csoundlib_state->input_buffers[device_index] = ring_buffer;
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
    if ((err = soundio_instream_start(csoundlib_state->input_streams[device_index])) != SoundIoErrorNone) {
        return err;
    }
    csoundlib_state->input_streams_started[device_index] = true;
    csoundlib_state->input_streams_written[device_index] = false;
    csoundlib_state->num_input_streams += 1;
    return SoundIoErrorNone;
}

int lib_stopInputStream(int device_index) {
    csoundlib_state->input_streams_started[device_index] = false;
    csoundlib_state->input_streams_written[device_index] = false;
    csoundlib_state->num_input_streams -= 1;
    soundio_instream_destroy(csoundlib_state->input_streams[device_index]);
    return SoundIoErrorNone;
}

int lib_createOutputStream(int device_index, double microphone_latency, int sample_rate) {
    int err;
    err = lib_checkEnvironmentAndBackendConnected();
    if (err != SoundIoErrorNone) return err;

    if (csoundlib_state->output_stream_started != -1) {
        soundio_outstream_destroy(csoundlib_state->output_streams[csoundlib_state->output_stream_started]);
        csoundlib_state->output_stream_started = -1;
    }

    struct SoundIoDevice* output_device = csoundlib_state->output_devices[device_index];
    struct SoundIoOutStream* outstream = soundio_outstream_create(output_device);
    if (!outstream) return SoundIoErrorNoMem;

    outstream->format = SoundIoFormatS24NE;
    outstream->sample_rate = sample_rate;
    outstream->layout = output_device->current_layout;
    outstream->software_latency = microphone_latency;
    outstream->write_callback = _outputStreamWriteCallback;
    outstream->underflow_callback = _underflowCallback;
    csoundlib_state->output_streams[device_index] = outstream;

    err = soundio_outstream_open(outstream);
    if (err != SoundIoErrorNone) return err;
    csoundlib_state->output_stream_initialized = true;

    return SoundIoErrorNone;
}

int lib_createAndStartOutputStream(int deviceIndex, double microphone_latency, int sample_rate) {
    int err;
    err = lib_createOutputStream(deviceIndex, microphone_latency, sample_rate);
    if (err != SoundIoErrorNone) return err;

    if ((err = soundio_outstream_start(csoundlib_state->output_streams[deviceIndex])) != SoundIoErrorNone) {
        return err;
    }
    csoundlib_state->output_stream_started = deviceIndex;
    return SoundIoErrorNone;
}

int lib_stopOutputStream(int deviceIndex) {
    csoundlib_state->output_stream_started = -1;
    csoundlib_state->output_stream_initialized = false;
    soundio_outstream_destroy(csoundlib_state->output_streams[deviceIndex]);
    return SoundIoErrorNone;
}

double lib_getCurrentRmsVolume(int deviceIndex) {
    return csoundlib_state->list_of_rms_volume_decibel[deviceIndex];
}