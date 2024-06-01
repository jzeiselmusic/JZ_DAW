package org.jzeisel.app_test.audio

import kotlinx.coroutines.*
import org.jzeisel.app_test.audio.viewmodel.ViewModelController
import org.jzeisel.app_test.util.Logger
import org.jzeisel.app_test.util.combineRmsVolumes
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
                        synchronizedTrackList.forEach { track ->
                            if (track.inputEnabled || track.recordingEnabled || audioStateFlow._state.isPlayingBack) {
                                val listOfInputs = mutableListOf<Double>()
                                listOfInputs.add(audioEngineManager.getRmsVolumeTrackPlayback(track.trackId))
                                if (track.inputEnabled || track.recordingEnabled) {
                                    listOfInputs.add(audioEngineManager.getRmsVolumeInputStream(track.trackId))
                                }
                                val total = combineRmsVolumes(*listOfInputs.toDoubleArray())
                                viewModelController.sendTrackRMSVolume(20 * log10(total), track.trackId)
                            }
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