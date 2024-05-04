#ifndef SOUND_LIB_H
#define SOUND_LIB_H

#define MAX_24_BIT_SIGNED            8388607.0
#define MAX_24_BIT_UNSIGNED          16777215.0

typedef struct _trackObj {
    uint32_t track_id; // unique identifier
    FILE** files; // pointer to list of file pointers representing audio data to be read
    uint32_t* file_sample_offsets; // sample offsets where these files begin
    uint32_t* file_num_bytes; // number of bytes to read for each audio file
    uint32_t num_files;

    bool record_enabled;
    uint8_t input_device_index; // input device currently attached to this track
    double current_rms_volume;
} trackObject;

/* print and error callback function helpers */
typedef void (*soundLibCallback) (char*);
typedef void (*soundStreamCallback) (char*, int);
typedef void (*floatPrintCallback) (char*, float);
typedef void (*charCallback) (char, int);

void registerAudioPanicCallback(soundLibCallback func);
void registerAudioLogCallback(soundLibCallback func);
void registerInputStreamCallback(soundStreamCallback func);
void registerOutputStreamCallback(soundStreamCallback func);
void registerFloatPrintCallback(floatPrintCallback func);
void registerCharCallback(charCallback func);

/* callback functions for audio playback */
typedef void (*outputProcessedCallback) (int);

void registerOutputProcessedCallback(outputProcessedCallback func);

/* init and deinit functions */
int lib_startSession();
int lib_initializeEnvironment();
int lib_destroySession();
int lib_deinitializeEnvironment();
int lib_getCurrentBackend();
int lib_checkEnvironmentAndBackendConnected();

void lib_startPlayback();
void lib_stopPlayback();

/* functions for input devices */
int lib_loadInputDevices();
int lib_getDefaultInputDeviceIndex();
int lib_getNumInputDevices();
char* lib_getDefaultInputDeviceName();
char* lib_getInputDeviceName(int index);
char* lib_getInputDeviceId(int index);
int lib_getNumChannelsOfInputDevice(int index);
char* lib_getNameOfChannelOfInputDevice(int deviceIndex, int channelIndex);

/* functions for output devices */
int lib_loadOutputDevices();
int lib_getDefaultOutputDeviceIndex();
int lib_getNumOutputDevices();
char* lib_getDefaultOutputDeviceName();
char* lib_getOutputDeviceName(int index);
char* lib_getOutputDeviceId(int index);
int lib_getNumChannelsOfOutputDevice(int index);
char* lib_getNameOfChannelOfOutputDevice(int deviceIndex, int channelIndex);

/* functions for starting and maintaining streams */
int lib_createInputStream(int device_index, double microphone_latency, int sample_rate);
int lib_createAndStartInputStream(int deviceIndex, double microphone_latency, int sample_rate);
int lib_createAllInputStreams(double microphone_latency, int sample_rate);
int lib_stopInputStream(int deviceIndex);
int lib_createOutputStream(int device_index, double microphone_latency, int sample_rate);
int lib_createAndStartOutputStream(int deviceIndex, double microphone_latency, int sample_rate);
int lib_stopOutputStream(int deviceIndex);
double lib_getCurrentRmsVolume(int deviceIndex);

/* handling tracks */
int lib_addNewTrack(int track_id);
int lib_deleteTrack(int track_id);
int lib_trackChooseInputDevice(int trackId, int device_index);

#endif