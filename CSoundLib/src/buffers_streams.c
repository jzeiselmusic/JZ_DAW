
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
#include "state.h"
#include "wav_driver.h"

#include <fcntl.h>

static int _createInputStream(int device_index, double microphone_latency, int sample_rate);

static int _createOutputStream(int device_index, double microphone_latency, int sample_rate);

static bool _sendChannelToOutput(int channel_index);

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
    /* gets called repeatedly every time audio data is available to be read on this particular input device */

    /* every input stream started gets a read callback associated with it that gets repeatedly called */

    /* when a device sends data, it interleaves the data based on how many channels there are */

    int device_index = -1;
    while(csoundlib_state->input_stream_written == true) {
        /* wait until output is done to start reading again */
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
        return;
    }

    /* all should be the same */
    struct SoundIoRingBuffer* ring_buffer = csoundlib_state->input_channel_buffers[0];
    /* get the write ptr for this inputs ring buffer */
    int bytes_count = soundio_ring_buffer_free_count(ring_buffer);
    int frame_count = bytes_count / BYTES_PER_FRAME_MONO; 

    if (frame_count_min > frame_count) {
        _panic("ring buffer writer overtook reader");
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
            /* Due to an overflow there is a hole. Fill the ring buffer with
               silence for the size of the hole.  */
            _formatPanic("Dropped %d frames due to internal overflow\n", frame_count);
        } 
        else {
            int rms_value = 0.0;
            double double_value = 0.0;

            for (int frame = 0; frame < frame_count; frame ++) {
                for (int ch = 0; ch < instream->layout.channel_count; ch ++) {
                    struct SoundIoRingBuffer* ring_buffer = csoundlib_state->input_channel_buffers[ch];
                    char* write_ptr = soundio_ring_buffer_write_ptr(ring_buffer);
                    char* bytes = areas[ch].ptr;
                    memcpy(write_ptr, bytes, instream->bytes_per_sample);
                    areas[ch].ptr += areas[ch].step;
                    soundio_ring_buffer_advance_write_ptr(ring_buffer, instream->bytes_per_sample);
                }
            }
        }
        if ((err = soundio_instream_end_read(instream))) {
            _panic("end read error: %s\n", soundio_strerror(err));
        }

        frames_left -= frame_count;
        if (frames_left <= 0) {
            break;
        }
    }
    csoundlib_state->input_stream_written = true;
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

    while (csoundlib_state->input_stream_written == false) {}

    /* clear mix buffer */
    memset(csoundlib_state->mixed_output_buffer, 0, MAX_BUFFER_SIZE_BYTES);

    /* clear track input buffers*/
    for (int track = 0; track < csoundlib_state->num_tracks; track++) {
        memset(csoundlib_state->list_of_track_objects[track].input_buffer.buffer, 0, MAX_BUFFER_SIZE_BYTES);
    }

    int max_fill_count = 0;
    for (int channel = 0; channel < csoundlib_state->num_channels_available; channel++) {
        if (csoundlib_state->input_stream_started == true) {
            ring_buffer = csoundlib_state->input_channel_buffers[channel];
            char *read_ptr = soundio_ring_buffer_read_ptr(ring_buffer);
            /* number of bytes available for reading */
            int fill_bytes = soundio_ring_buffer_fill_count(ring_buffer);
            int fill_count = fill_bytes / BYTES_PER_FRAME_MONO;
            if (fill_count > max_fill_count) max_fill_count = fill_count;
            double rms_val = calculate_rms_level(read_ptr, fill_bytes);

            for (int trackIdx = 0; trackIdx < csoundlib_state->num_tracks; trackIdx++) {
                if (csoundlib_state->list_of_track_objects[trackIdx].input_channel_index == channel) {
                    /* this track has chosen this channel for input */

                    /* set rms value based on input RMS of this channel */
                    csoundlib_state->list_of_track_objects[trackIdx].current_rms_volume_input_stream = rms_val;

                    /* write the input stream to the track's input buffer */
                    if (csoundlib_state->list_of_track_objects[trackIdx].input_enabled) {
                        memcpy(csoundlib_state->list_of_track_objects[trackIdx].input_buffer.buffer, read_ptr, fill_bytes);
                        csoundlib_state->list_of_track_objects[trackIdx].input_buffer.write_bytes = fill_bytes;
                    }

                    /* if recording, write the input stream to the track's recently opened wav file */
                    if (csoundlib_state->list_of_track_objects[trackIdx].is_recording && csoundlib_state->playback_started) {
                        thr_write_to_wav_file(&(csoundlib_state->list_of_track_objects[trackIdx]), read_ptr, fill_bytes);
                    }
                } 
            }
            soundio_ring_buffer_advance_read_ptr(ring_buffer, fill_bytes);
        }
    }
    /* go through each track */
    /* write playing back WAV files for each track if available and playing back  */
    /* copy input buffer to output under certain conditions */
    for (int trackIdx = 0; trackIdx < csoundlib_state->num_tracks; trackIdx ++) {
        /* if playback started, copy wav file to track's input buffer */
        if (csoundlib_state->list_of_track_objects[trackIdx].is_playing_back) {
            int bytes_copied = read_wav_file_for_playback(&(csoundlib_state->list_of_track_objects[trackIdx]), 
                                                     csoundlib_state->list_of_track_objects[trackIdx].input_buffer.buffer, 
                                                     frame_count_max * outstream->bytes_per_frame);
            csoundlib_state->list_of_track_objects[trackIdx].input_buffer.write_bytes = bytes_copied;
        }
        else {
            csoundlib_state->list_of_track_objects[trackIdx].current_rms_volume_track_playback = 0.0;
        }
        if (!csoundlib_state->list_of_track_objects[trackIdx].mute_enabled && 
            (!csoundlib_state->solo_engaged || 
                (csoundlib_state->solo_engaged && csoundlib_state->list_of_track_objects[trackIdx].solo_enabled))) {
            add_audio_buffers_24bitNE(
                csoundlib_state->mixed_output_buffer, 
                csoundlib_state->list_of_track_objects[trackIdx].input_buffer.buffer, 
                csoundlib_state->list_of_track_objects[trackIdx].input_buffer.write_bytes
            );
        }
    }

    csoundlib_state->current_rms_ouput = calculate_rms_level(csoundlib_state->mixed_output_buffer, frame_count_max * outstream->bytes_per_frame);

    /* now place data from mixed output buffer into output stream */
    int read_count = min_int(frame_count_max, max_fill_count);
    /* handle case of no input streams */
    if (read_count == 0) read_count = frame_count_min;
    /* there is data to be read to output */
    frames_left = read_count;
    char* mixed_read_ptr = csoundlib_state->mixed_output_buffer;
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
    csoundlib_state->input_stream_written = false;
    if (csoundlib_state->playback_started) outputProcessed(read_count);
}

static int _createInputStream(int device_index, double microphone_latency, int sample_rate) {
    int err;
    err = _checkEnvironmentAndBackendConnected();
    if (err != SoundIoErrorNone) return err;

    struct SoundIoDevice* input_device = csoundlib_state->input_devices[device_index];
    struct SoundIoInStream* instream = soundio_instream_create(input_device);
    if (!instream) return SoundIoErrorNoMem;

    /* signed 24 bit native endian (macos is little endian) */
    instream->format = SoundIoFormatS24NE; 
    instream->sample_rate = sample_rate;

    /* use whatever the default channel layout is (take all the channels available) */
    /* data should come in interleaved based on how many channels are sending data */
    instream->layout = input_device->current_layout;
    instream->software_latency = microphone_latency;
    instream->read_callback = _inputStreamReadCallback;
    csoundlib_state->input_stream = instream;

    err = soundio_instream_open(instream);
    if (err != SoundIoErrorNone) return err;

    int num_channels = lib_getNumChannelsOfInputDevice(device_index);
    /* reset channel buffers */
    free(csoundlib_state->input_channel_buffers);
    csoundlib_state->input_channel_buffers = malloc(num_channels * sizeof(struct SoundIoRingBuffer*));

    for (int idx = 0; idx < num_channels; idx++) {
        int capacity = DEFAULT_BUFFER_SIZE * instream->bytes_per_sample;
        struct SoundIoRingBuffer* ring_buffer = soundio_ring_buffer_create(csoundlib_state->soundio, capacity);
        if (!ring_buffer) return SoundIoErrorNoMem;

        char *buf = soundio_ring_buffer_write_ptr(ring_buffer);
        int fill_count = soundio_ring_buffer_capacity(ring_buffer);
        memset(buf, 0, fill_count);
        csoundlib_state->input_channel_buffers[idx] = ring_buffer;
        csoundlib_state->num_channels_available = num_channels;
    }
    return SoundIoErrorNone;
}

int lib_createAndStartInputStream(int device_index, double microphone_latency, int sample_rate) {
    int err;
    err = _createInputStream(device_index, microphone_latency, sample_rate);
    if (err != SoundIoErrorNone) return err;
    if ((err = soundio_instream_start(csoundlib_state->input_stream)) != SoundIoErrorNone) {
        return err;
    }
    csoundlib_state->input_stream_started = true;
    csoundlib_state->input_stream_written = false;
    return SoundIoErrorNone;
}

int lib_stopInputStream() {
    if (csoundlib_state->input_stream_started) {
        csoundlib_state->input_stream_started = false;
        csoundlib_state->input_stream_written = false;
        soundio_instream_destroy(csoundlib_state->input_stream);
    }
    return SoundIoErrorNone;
}

static int _createOutputStream(int device_index, double microphone_latency, int sample_rate) {
    int err;
    err = _checkEnvironmentAndBackendConnected();
    if (err != SoundIoErrorNone) return err;

    if (csoundlib_state->output_stream_started) {
        soundio_outstream_destroy(csoundlib_state->output_stream);
        csoundlib_state->output_stream_started = false;
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

    csoundlib_state->output_stream = outstream;

    err = soundio_outstream_open(outstream);
    if (err != SoundIoErrorNone) return err;
    csoundlib_state->output_stream_initialized = true;

    return SoundIoErrorNone;
}

int lib_createAndStartOutputStream(int deviceIndex, double microphone_latency, int sample_rate) {
    int err;
    err = _createOutputStream(deviceIndex, microphone_latency, sample_rate);
    if (err != SoundIoErrorNone) return err;

    if ((err = soundio_outstream_start(csoundlib_state->output_stream)) != SoundIoErrorNone) {
        return err;
    }
    csoundlib_state->output_stream_started = true;
    return SoundIoErrorNone;
}

int lib_stopOutputStream() {
    if (csoundlib_state->output_stream_started) {
        csoundlib_state->output_stream_started = false;
        csoundlib_state->output_stream_initialized = false;
        soundio_outstream_pause(csoundlib_state->output_stream, true);
        soundio_outstream_destroy(csoundlib_state->output_stream);
    }
    logCallback("returning true");
    return SoundIoErrorNone;
}

static bool _sendChannelToOutput(int channel_index) {
    for (int idx = 0; idx < csoundlib_state->num_tracks; idx ++) {
        if (
            csoundlib_state->list_of_track_objects[idx].input_channel_index == channel_index
            && csoundlib_state->list_of_track_objects[idx].input_enabled) {
            return true;
        }
    }
    return false;
}