
#include "soundio_inc.h"

struct SoundIo* soundio;

bool input_memory_allocated = false;

bool output_memory_allocated = false;

bool environment_initialized = false;

bool backend_connected = false;

char emptyString[] = "";