package org.jzeisel.app_test.audio

data class Device(
    var index: Int,
    var name: String,
    var id: String,
    var direction: Direction,
    var channels: List<Channel>? = null
)
data class Channel(
    var index: Int,
    var name: String,
)
data class TrackData(
    var trackName: String,
    var trackIndex: Int,
    var inputDevice: Device?,
    var inputChannel: Channel?,
    var volume: Double,
    var panning: Int,
)
enum class Direction {
    INPUT, OUTPUT
}
enum class AudioBackend(val readable: String) {
    SoundIoBackendNone("None"),
    SoundIoBackendJack("JACK"),
    SoundIoBackendPulseAudio("Pulse Audio"),
    SoundIoBackendAlsa("Alsa"),
    SoundIoBackendCoreAudio("Core Audio"),
    SoundIoBackendWasapi("WASAPI"),
    SoundIoBackendDummy("Dummy"),
}