package org.jzeisel.app_test.audio

import kotlinx.coroutines.*
import org.jzeisel.app_test.audio.viewmodel.ViewModelController
import org.jzeisel.app_test.util.Logger
import org.jzeisel.app_test.util.combineRmsVolumes
import org.jzeisel.app_test.util.envelopeFollower
import org.jzeisel.app_test.util.loop
import java.util.*
import kotlin.math.log10

class VUMeterThread(
                    private val audioEngineManager: AudioEngineManager,
                    private val viewModelController: ViewModelController,
                    private val audioStateFlow: AudioStateFlow) {
    private val threadDelay = 50L
    private var synchronizedTrackList = Collections.synchronizedList(audioStateFlow._state.trackList)
    private val scope = CoroutineScope(Dispatchers.Default)
    private var vuMeterThreadJob: Job? = null

    fun updateSynchronizedTrackList(trackList: List<TrackData>) {
        synchronizedTrackList = Collections.synchronizedList(trackList)
    }

    private fun startVUMeterThread() {
        if (vuMeterThreadJob == null) {
            vuMeterThreadJob = scope.launch {
                loop(threadDelay) {
                    if (isActive) {
                        viewModelController.sendOutputRMSVolume(20 * log10(audioEngineManager.getOutputRms()))
                        synchronizedTrackList.forEach { track ->
                            if (track.inputEnabled || track.recordingEnabled || audioStateFlow._state.isPlayingBack) {
                                val listOfInputs = mutableListOf<Double>()
                                /* when input enabled, use track input */
                                /* when not input enabled, and track playing back use track output */
                                val trackInput = audioEngineManager.getRmsVolumeTrackInput(track.trackId)
                                val trackOutput = audioEngineManager.getRmsVolumeTrackOutput(track.trackId)
                                var realRms =
                                    if (track.inputEnabled || track.recordingEnabled) trackInput
                                    else if (audioStateFlow._state.isPlayingBack) trackOutput else 0.0
                                val filteredValue = envelopeFollower(
                                    realRms, audioStateFlow._state.envelopeAttack,
                                    audioStateFlow._state.envelopeRelease, track.lastVUMeterValue)
                                track.lastVUMeterValue = filteredValue
                                viewModelController.sendTrackRMSVolume(20 * log10(filteredValue), track.trackId)
                                return@forEach
                            }
                            track.lastVUMeterValue = 0.0
                        }
                    }
                    else {
                        return@loop
                    }
                }
            }
        }
    }

    fun removeAllTracksFromStreaming() {
        stopVUMeterThread()
    }

    fun addAllTracksToStreaming() {
        startVUMeterThread()
    }

    private fun stopVUMeterThread() {
        vuMeterThreadJob?.cancel()
        vuMeterThreadJob = null
    }
}