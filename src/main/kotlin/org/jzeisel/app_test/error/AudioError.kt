package org.jzeisel.app_test.error

enum class AudioError(val readable: String) {
    /* errors defined in libsoundio */
    SoundIoErrorNone("No error"),
    SoundIoErrorNoMem("Out of memory"),
    SoundIoErrorInitAudioBackend("The backend does not appear to be active or running"),
    SoundIoErrorSystemResources("A system resource other than memory was not available"),
    SoundIoErrorOpeningDevice("Attempted to open a device and failed"),
    SoundIoErrorNoSuchDevice("No device found with this identifier"),
    SoundIoErrorInvalid("The programmer did not comply with the API"),
    SoundIoErrorBackendUnavailable("audio engine was compiled without support for that backend"),
    SoundIoErrorStreaming("An open stream had an error that can only be recovered from by destroying the stream and creating it again"),
    SoundIoErrorIncompatibleDevice("Attempted to use a device with parameters it cannot support"),
    SoundIoErrorNoSuchClient("JACK returned JackNoSuchClient"),
    SoundIoErrorIncompatibleBackend("Attempted to use parameters that the backend cannot support"),
    SoundIoErrorBackendDisconnected("Backend server shutdown or became inactive"),
    SoundIoErrorInterrupted("Error Interrupted"),
    SoundIoErrorUnderflow("Buffer under-run occurred"),
    SoundIoErrorEncodingString("Unable to convert to or from UTF-8 to the native string format"),

    /* custom errors */
    DevicesNotInitialized("device for track is null"),
    EnvironmentNotInitialized("No memory to initialize sound environment"),
    IndexOutOfBounds("Index chosen out of bounds of number of devices"),
    DevicesNotLoaded("Devices not loaded"),
    InputStreamError("Input stream error"),
    SoundIoInputMemoryNotAllocated("Input memory not allocated, cannot access data"),
    SoundIoOutputMemoryNotAllocated("Output memory not allocated, cannot access data"),
    SoundIoErrorTrackNotFound("Track not found with that track ID"),
    SoundIoErrorOpeningFile("Error opening file for recording"),
    SoundIoErrorFileNotFound("Error during playback: file not found"),
    SoundIoErrorLoadingMetronomeFile("Error loading metronome wav file"),
    SoundIoErrorResampling("Error while resampling audio")
}