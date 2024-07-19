#ifndef BUFFERS_STREAMS_H
#define BUFFERS_STREAMS_H

#define BYTES_PER_FRAME_MONO                      4
#define BYTES_PER_FRAME_STEREO                    8
#define DEFAULT_BUFFER_SIZE                       64
#define MAX_BUFFER_SIZE_BYTES                     8192

int lib_createAndStartInputStream(int deviceIndex, float microphone_latency, int sample_rate);

int lib_stopInputStream();

int lib_createAndStartOutputStream(int deviceIndex, float microphone_latency, int sample_rate);

int lib_stopOutputStream();

#endif