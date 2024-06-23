#include "soundlib_util.h"

#include <math.h>
#include <stdlib.h>
#include <string.h>
#include "soundio_inc.h"
#include "audio_errors.h"
#include "callbacks.h"
#include "samplerate.h"
#include "audio_playback.h"

void s16le_to_float(const char* input, float* output, int samples) {
    const int16_t* s16_input = (const int16_t*)input;
    for (int i = 0; i < samples; i++) {
        output[i] = s16_input[i] / 32768.0f;
    }
}

void float_to_s16le(const float* input, char* output, int samples) {
    int16_t* s16_output = (int16_t*)output;
    for (int i = 0; i < samples; i++) {
        float sample = input[i] * 32768.0f;
        if (sample > 32767) sample = 32767;
        if (sample < -32768) sample = -32768;
        s16_output[i] = (int16_t)sample;
    }
}

int min_int(int a, int b) {
    return (a < b) ? a : b;
}

int doubleToDecibel(double input) {
    return 20*log10(input);
}

float envelopeFollower(double input, double attack, double release, double prev_envelope) {
    double attackAlpha = 1 - expf(-1.0 / (attack * 100));
    double releaseAlpha = 1 - expf(-1.0 / (release * 100));

    double envelope = 0;
    if (input > prev_envelope)
        envelope = attackAlpha * input + (1 - attackAlpha) * prev_envelope;
    else
        envelope = releaseAlpha * input + (1 - releaseAlpha) * prev_envelope;

    return envelope;
}

void memadd(void *dest, void *src, size_t n) {
    /* same as memcpy except add to data location instead of overwrite */
    char *csrc = (char *)src;
    char *cdest = (char *)dest;

    // Copy contents of src[] to dest[]
    for (int i = 0; i < n; i++) {
        cdest[i] += csrc[i];
    }
}

void add_audio_buffers_24bitNE(char* dest, const char* source, int num_bytes) {
    for (int idx = 0; idx < num_bytes; idx += 4) {
        char* dest_ptr = dest + idx;
        char* src_ptr = source + idx;

        int32_t dest_sample = *(int32_t*)dest_ptr;
        int32_t source_sample = *(int32_t*)src_ptr;
        int32_t sum = dest_sample + source_sample;
        memcpy(dest + idx, &sum, 4);
    }
}

double calculate_rms_level(const char* source, int num_bytes) {
    double rms = 0.0;
    for (int idx = 0; idx < num_bytes; idx += 4) {
        double sample = four_bytes_to_sample(source + idx);
        rms += sample * sample;
    }
    return sqrt(rms / (double)num_bytes);
}

double four_bytes_to_sample(const char* bytes) {
    int sample_value = 
            (int32_t)((bytes[3] << 24) 
                    | (bytes[2] << 16) 
                    | (bytes[1] << 8) 
                    | bytes[0]);
    return sample_value / MAX_24_BIT_SIGNED;
}

void c16bit_to_24bit(const int16_t* input, int32_t* output, size_t sample_count) {
    for (size_t i = 0; i < sample_count; i++) {
        output[i] = ((int32_t)input[i]) << 8;
    }
}

void stereo_to_mono(size_t sample_width, char* mono_buffer, const char* stereo_buffer, size_t* mono_bytes, size_t stereo_bytes) {
    /* go through stereo_bytes amt of data, copying only every other sample into mono buffer */
    for (int idx = 0; idx < stereo_bytes; idx += (sample_width*2)) {
        memcpy(mono_buffer, stereo_buffer + idx, sample_width);
        *mono_bytes += sample_width;
        mono_buffer += sample_width;
    }
}

void mono_to_stereo(size_t sample_width, char* stereo_buffer, const char* mono_buffer, size_t* stereo_bytes, size_t mono_bytes) {
    /* go through mono_bytes amt of data, writing two copies to every other sample in stereo buffer */
    for (int idx = 0; idx < mono_bytes; idx += sample_width) {
        memcpy(stereo_buffer, mono_buffer + idx, sample_width);
        memcpy(stereo_buffer + sample_width, mono_buffer + idx, sample_width);
        *stereo_bytes += sample_width*2;
        stereo_buffer += sample_width*2;
    }   
}

int mono_resample(size_t sample_width, char* out_buffer, const char* in_buffer, size_t* out_bytes, size_t in_bytes, int out_sample_rate, int in_sample_rate) {
    int input_frames = in_bytes / sample_width;
    int output_frames = (int)((float)input_frames * out_sample_rate / in_sample_rate);

    float float_in_buffer[MAX_METRONOME_BUFFER];
    float float_out_buffer[MAX_METRONOME_BUFFER];

    s16le_to_float(in_buffer, float_in_buffer, input_frames);
    int error;
    SRC_DATA src_data;
    src_data.data_in = float_in_buffer;
    src_data.input_frames = input_frames;
    src_data.data_out = float_out_buffer;
    src_data.output_frames = output_frames;
    src_data.src_ratio = (double)out_sample_rate / in_sample_rate;
    error = src_simple(&src_data, SRC_SINC_BEST_QUALITY, 1);

    if (error) {
        return SoundIoErrorResampling;
    }
    float_to_s16le(float_out_buffer, out_buffer, src_data.output_frames);
    *out_bytes = src_data.output_frames * sample_width;

    return SoundIoErrorNone;
}