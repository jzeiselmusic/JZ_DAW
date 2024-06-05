package org.jzeisel.app_test.audio

data class Device(
    var index: Int,
    var name: String,
    var id: String,
    var direction: Direction,
    var channels: List<Channel>
)
data class Channel(
    var index: Int,
    var name: String,
)

data class TrackData(
    var trackId: Int,
    var volume: Double,
    var panning: Int,
    var inputDevice: Device,
    var inputChannel: Channel,
    var inputEnabled: Boolean = false,
    var recordingEnabled: Boolean = false,
    var lastVUMeterValue: Double = 0.0
)
data class AudioStream(
    var device: Device
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

fun <T> T.whenNot(value: T, block: (T) -> Unit): T? {
    return if (this != value) {
        block(this)
        this
    } else {
        null
    }
}

fun <T> T.whenIs(value: T, block: (T) -> Unit): T? {
    return if (this == value) {
        block(this)
        this
    } else {
        null
    }
}