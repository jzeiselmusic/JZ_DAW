package org.jzeisel.app_test.audio

data class Device(
    var index: Int,
    var name: String,
    var id: String,
    var direction: Direction,
    var numChannels: Int
)

data class TrackData(
    var trackId: Int,
    var inputDevice: Device?,
    var volume: Double,
    var panning: Int,
)

enum class Direction {
    INPUT, OUTPUT
}

enum class AudioBackend {
    SoundIoBackendNone,
    SoundIoBackendJack,
    SoundIoBackendPulseAudio,
    SoundIoBackendAlsa,
    SoundIoBackendCoreAudio,
    SoundIoBackendWasapi,
    SoundIoBackendDummy,
}