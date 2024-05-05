#ifndef BUFFERS_STREAMS_H
#define BUFFERS_STREAMS_H

#define BYTES_PER_FRAME_MONO                      4
#define BYTES_PER_FRAME_STEREO                    8
#define DEFAULT_BUFFER_SIZE                       64
#define MAX_BUFFER_SIZE_BYTES                     8192

int lib_createInputStream(int device_index, double microphone_latency, int sample_rate);

int lib_createAndStartInputStream(int deviceIndex, double microphone_latency, int sample_rate);

int lib_createAllInputStreams(double microphone_latency, int sample_rate);

int lib_stopInputStream(int deviceIndex);

int lib_createOutputStream(int device_index, double microphone_latency, int sample_rate);

int lib_createAndStartOutputStream(int deviceIndex, double microphone_latency, int sample_rate);

int lib_stopOutputStream(int deviceIndex);

double lib_getCurrentRmsVolume(int deviceIndex);

#endif