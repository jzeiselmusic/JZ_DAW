#ifndef AUDIO_INIT_H
#define AUDIO_INIT_H

int lib_startSession();

int lib_destroySession();

int lib_getCurrentBackend();

int _checkEnvironmentAndBackendConnected();

void lib_setSamplesInABeat(int samples);

#endif 