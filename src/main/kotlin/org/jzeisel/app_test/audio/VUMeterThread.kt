package org.jzeisel.app_test.audio

import kotlinx.coroutines.*
import org.jzeisel.app_test.audio.viewmodel.ViewModelController
import org.jzeisel.app_test.util.loop
import java.util.*

class VUMeterThread(private val audioEngineManager: AudioEngineManager,
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
                            if (track.inputEnabled || track.recordingEnabled) {
                                val rmsVolume = audioEngineManager.getRmsVolumeInputStream(track.trackId)
                                viewModelController.sendTrackRMSVolume(rmsVolume, track.trackId)
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