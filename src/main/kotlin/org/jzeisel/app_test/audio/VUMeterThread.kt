package org.jzeisel.app_test.audio

import kotlinx.coroutines.*
import org.jzeisel.app_test.audio.viewmodel.ViewModelController
import org.jzeisel.app_test.util.Logger
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

    fun addToTracksStreaming(track: TrackData) {
        if (synchronizedTrackList.any { it.trackIndex == track.trackIndex && it.isStreaming })
            return

        if (!synchronizedTrackList.any { it.isStreaming })
            startVUMeterThread()

        val index = track.trackIndex
        val tList = audioStateFlow._state.trackList
        tList.forEach { if (it.trackIndex == index) it.isStreaming = true }
        audioStateFlow._state = audioStateFlow._state.copy(trackList = tList)
        updateSynchronizedTrackList(tList)
    }

    fun removeFromTracksStreaming(track: TrackData) {
        val index = track.trackIndex
        val tList = audioStateFlow._state.trackList
        tList.forEach { if (it.trackIndex == index) it.isStreaming = false }
        audioStateFlow._state = audioStateFlow._state.copy(trackList = tList)
        updateSynchronizedTrackList(tList)

        if (!tList.any { it.isStreaming }) stopVUMeterThread()
    }

    private fun startVUMeterThread() {
        if (vuMeterThreadJob == null) {
            vuMeterThreadJob = scope.launch {
                loop(threadDelay) {
                    if (isActive) {
                        synchronizedTrackList.forEach { track ->
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
        val tList = audioStateFlow._state.trackList
        tList.forEach {
            it.isStreaming = false
            it.audioStream = null
        }
        audioStateFlow._state = audioStateFlow._state.copy(trackList = tList)
        updateSynchronizedTrackList(tList)
        stopVUMeterThread()
    }

    private fun stopVUMeterThread() {
        vuMeterThreadJob?.cancel()
        vuMeterThreadJob = null
    }
}