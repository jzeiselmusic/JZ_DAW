package org.jzeisel.app_test.audio

import org.jzeisel.app_test.error.AudioError

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
    var trackIndex: Int,
    var volume: Double,
    var panning: Int,
    var inputDevice: Device,
    var inputChannel: Channel,
    var audioStream: AudioStream? = null,
    var isStreaming: Boolean = false
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