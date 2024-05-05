#ifndef AUDIO_INIT_H
#define AUDIO_INIT_H

int lib_startSession();

int lib_initializeEnvironment();

int lib_destroySession();

int lib_deinitializeEnvironment();

int lib_getCurrentBackend();

int lib_checkEnvironmentAndBackendConnected();

#endif 