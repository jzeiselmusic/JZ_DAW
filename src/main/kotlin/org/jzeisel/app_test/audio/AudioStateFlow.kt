package org.jzeisel.app_test.audio

data class AudioState(
    var isInitialized: Boolean = false,
    var backend: AudioBackend = AudioBackend.SoundIoBackendNone,
    var isPlayingBack: Boolean = false,
    var playBackLocation: Double = 0.0,
    var beatsPerMinute: Double = 120.0, // if this goes above ~275 metronome will sound weird
    var beatsPerSecond: Double = beatsPerMinute/60.0,
    var tSignatureTop: UInt = 4U,
    var tSignatureBottom: UInt = 4U,
    var sampleRate: SampleRate = SampleRate.FOUR_EIGHT_K,
    var bitDepth: BitDepth = BitDepth.TWENTY_FOUR,
    var samplesInABeat: Int = (sampleRate.value / beatsPerSecond).toInt(),
    var numTracks: Int = 0,
    var trackList: MutableList<TrackData> = mutableListOf(),
    var outputDevice: Device? = null,
    var cursorOffsetSamples: Int = 0,
    var savedCursorOffsetSamples: Int = 0,
    var envelopeAttack: Double = 0.00001,
    var envelopeRelease: Double = 0.12,
    var metronomeEnabled: Boolean = false,
)
class AudioStateFlow {
    var _state = AudioState()
}