#ifndef BUFFERS_STREAMS_H
#define BUFFERS_STREAMS_H

#define BYTES_PER_FRAME_MONO                      4
#define BYTES_PER_FRAME_STEREO                    8
#define DEFAULT_BUFFER_SIZE                       64
#define MAX_BUFFER_SIZE_BYTES                     8192

extern struct SoundIoRingBuffer** input_buffers;
extern struct SoundIoInStream** input_streams;
extern bool* input_streams_started;
extern bool* input_streams_written;
extern int num_input_streams;
extern int input_stream_read_write_counter;

extern struct SoundIoOutStream** output_streams;
/* will be index of output device when active, otherwise -1 */
extern int output_stream_started;
extern bool output_stream_initialized;

extern char* mixed_input_buffer; // allocate to max buffer size in bytes

extern double* list_of_rms_volume_decibel;

int lib_createInputStream(int device_index, double microphone_latency, int sample_rate);

int lib_createAndStartInputStream(int deviceIndex, double microphone_latency, int sample_rate);

int lib_createAllInputStreams(double microphone_latency, int sample_rate);

int lib_stopInputStream(int deviceIndex);

int lib_createOutputStream(int device_index, double microphone_latency, int sample_rate);

int lib_createAndStartOutputStream(int deviceIndex, double microphone_latency, int sample_rate);

int lib_stopOutputStream(int deviceIndex);

double lib_getCurrentRmsVolume(int deviceIndex);

#endif