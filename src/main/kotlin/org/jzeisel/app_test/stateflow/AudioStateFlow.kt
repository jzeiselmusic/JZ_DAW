package org.jzeisel.app_test.stateflow

import org.jzeisel.app_test.audio.AudioBackend
import org.jzeisel.app_test.audio.Device
import org.jzeisel.app_test.audio.TrackData

data class AudioState(
    var isInitialized: Boolean = false,
    var backend: AudioBackend = AudioBackend.SoundIoBackendNone,
    var isPlaying: Boolean = false,
    var playBackLocation: Double = 0.0,
    var tempo: Double = 120.0,
    var tSignatureTop: UInt = 4U,
    var tSignatureBottom: UInt = 4U,
    var numTracks: Int = 0,
    var trackDataList: MutableList<TrackData> = mutableListOf(),
    var outputDevice: Device? = null,
)
class AudioStateFlow {
    var state = AudioState()
}