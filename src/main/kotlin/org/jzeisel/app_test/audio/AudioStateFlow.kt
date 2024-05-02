package org.jzeisel.app_test.audio

data class AudioState(
    var isInitialized: Boolean = false,
    var backend: AudioBackend = AudioBackend.SoundIoBackendNone,
    var isPlaying: Boolean = false,
    var playBackLocation: Double = 0.0,
    var tempo: Double = 140.0,
    var tSignatureTop: UInt = 4U,
    var tSignatureBottom: UInt = 4U,
    var sampleRate: Int = 44100,
    var samplesInABeat: Int = 22050,
    var numTracks: Int = 0,
    var trackList: MutableList<TrackData> = mutableListOf(),
    var outputDevice: Device? = null,
    var cursorOffsetSamples: Int = 0,
    var savedCursorOffsetSamples: Int = 0
)
class AudioStateFlow {
    var _state = AudioState()
}