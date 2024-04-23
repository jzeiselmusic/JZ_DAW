#ifndef SOUND_LIB_H
#define SOUND_LIB_H

typedef void (*soundLibCallback) (char*);
typedef void (*soundStreamCallback) (char*, int);

int lib_startSession();
int lib_initializeEnvironment();
int lib_destroySession();
int lib_deinitializeEnvironment();
int lib_getCurrentBackend();
int lib_checkEnvironmentAndBackendConnected();

/* callback function helpers */
void registerAudioPanicCallback(soundLibCallback func);
void registerAudioLogCallback(soundLibCallback func);
void registerInputStreamCallback(soundStreamCallback func);
void registerOutputStreamCallback(soundStreamCallback func);

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

#endif