#include "state.h"

// keeps the state of the current audio session
audio_state* csoundlib_state;

double lib_getCurrentRmsOutput() {
    return csoundlib_state->current_rms_ouput;
}