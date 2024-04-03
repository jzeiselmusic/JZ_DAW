package org.jzeisel.app_test.audio

import kotlinx.coroutines.*
import org.jzeisel.app_test.audio.viewmodel.ViewModelController
import org.jzeisel.app_test.util.loop
import java.util.*

class VUMeterThread(val audioEngineManager: AudioEngineManager,
                    val viewModelController: ViewModelController) {
    private val threadDelay = 50L
    private val tracksStreaming = Collections.synchronizedList(mutableListOf<TrackData>())
    private val scope = CoroutineScope(Dispatchers.Default)
    private var vuMeterThreadJob: Job? = null

    fun addToTracksStreaming(track: TrackData) {
        if (tracksStreaming.any { it.trackIndex == track.trackIndex }) return
        if (tracksStreaming.isEmpty()) startVUMeterThread()
        tracksStreaming.add(track)
    }
    fun removeFromTracksStreaming(track: TrackData) {
        tracksStreaming.firstOrNull { it.trackIndex == track.trackIndex }?.let { tracksStreaming.remove(it) }
        if (tracksStreaming.isEmpty()) stopVUMeterThread()
    }
    private fun startVUMeterThread() {
        if (vuMeterThreadJob == null) {
            vuMeterThreadJob = scope.launch {
                loop(threadDelay) {
                    if (isActive) {
                        tracksStreaming.forEach { track ->
                            track.audioStream?.let {
                                val rmsVolume = audioEngineManager.getCurrentRMSVolume(it.device.index)
                                viewModelController.sendTrackRMSVolume(rmsVolume, track.trackIndex)
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
        tracksStreaming.clear()
        stopVUMeterThread()
    }

    private fun stopVUMeterThread() {
        vuMeterThreadJob?.cancel()
        vuMeterThreadJob = null
    }
}