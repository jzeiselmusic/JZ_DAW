package org.jzeisel.app_test.audio.viewmodel

import org.jzeisel.app_test.audio.*
import org.jzeisel.app_test.error.AudioError
import org.jzeisel.app_test.stateflow.TrackListStateFlow
import org.jzeisel.app_test.util.Logger

class AudioViewModel(
    private val viewModelController: ViewModelController,
    private val viewModelState: TrackListStateFlow) {

    private val audioStateFlow = AudioStateFlow()
    private val audioEngineManager = AudioEngineManager(this)
    private val vuMeterThread = VUMeterThread(audioEngineManager, viewModelController, audioStateFlow)
    val defaultInputIndex: Int get() { return audioEngineManager.defaultInputIndex }

    val tempo: Double get() { return audioStateFlow._state.tempo }
    val sampleRate: Int get() { return audioStateFlow._state.sampleRate }
    val numBeats: UInt get() { return audioStateFlow._state.tSignatureTop }
    val cursorOffsetSamples: Int get() { return audioStateFlow._state.cursorOffsetSamples }

    fun initialize() {
        audioEngineManager.initialize().whenNot(AudioError.SoundIoErrorNone) {
            viewModelController.throwAudioStartupError(it)
        }

        audioEngineManager.getCurrentBackend()?.let {
            audioStateFlow._state = audioStateFlow._state.copy(isInitialized = true, backend = it)
        } ?: viewModelController.throwAudioStartupError(AudioError.SoundIoErrorInitAudioBackend)

        audioStateFlow._state = audioStateFlow._state.copy(outputDevice =
            audioEngineManager.getOutputDeviceFromIndex(audioEngineManager.defaultOutputIndex))
    }

    fun deinitialize() {
        vuMeterThread.removeAllTracksFromStreaming()
        audioEngineManager.deinitialize()
    }

    private fun printTrackData() {
        Logger.debug(javaClass.simpleName, "tracks: ${audioStateFlow._state.numTracks}", 1)
        for (t in audioStateFlow._state.trackList) {
            Logger.debug(javaClass.simpleName, "index: ${t.trackIndex}", 1)
            Logger.debug(javaClass.simpleName, "\tdevice: ${t.inputDevice}", 1)
            Logger.debug(javaClass.simpleName, "\tchannel: ${t.inputChannel}", 1)
        }
    }

    fun getChannelsFromDevice(index: Int): List<Channel>? {
        if (audioStateFlow._state.isInitialized) return audioEngineManager.getChannelsFromDeviceIndex(index)!!
        else return null
    }

    fun getInputDeviceList(): List<Device>? {
        if (audioStateFlow._state.isInitialized) return audioEngineManager.getInputDevices()
        else return null
    }

    fun addTrack(trackIndex: Int) {
        val nTracks = viewModelState.numTracks
        val device = audioEngineManager.getInputDeviceFromIndex(audioEngineManager.defaultInputIndex)
        val defaultChannel = Channel(0, audioEngineManager.getNameOfChannelFromIndex(device.index, 0))
        val tList = audioStateFlow._state.trackList
        tList.forEach { if(it.trackIndex >= trackIndex) it.trackIndex += 1 }
        val trackData = TrackData(trackIndex, 0.0, 0, inputDevice = device, inputChannel = defaultChannel)
        tList.add(trackIndex, trackData)
        audioStateFlow._state = audioStateFlow._state.copy(
            numTracks = nTracks,
            trackList = tList
        )
        /* then make sure the vu meter thread has the same information */
        vuMeterThread.updateSynchronizedTrackList(trackList = tList)
    }

    fun removeTrack(trackIndex: Int) {
        val nTracks = audioStateFlow._state.numTracks
        val tList = audioStateFlow._state.trackList
        stopInputStream(trackIndex)
        tList.remove(tList.first { it.trackIndex == trackIndex })
        tList.forEach { if(it.trackIndex >= trackIndex) it.trackIndex -= 1 }
        audioStateFlow._state = audioStateFlow._state.copy(
            numTracks = nTracks,
            trackList = tList
        )
        audioStateFlow._state = audioStateFlow._state.copy(numTracks = nTracks, trackList = tList)
        vuMeterThread.updateSynchronizedTrackList(tList)
    }

    fun setTrackDeviceAndChannel(trackIndex: Int, deviceIndex: Int, channelIndex: Int) {
        val device = audioEngineManager.getInputDeviceFromIndex(deviceIndex)
        val channel = audioEngineManager.getChannelsFromDeviceIndex(deviceIndex)!![channelIndex]
        val tList = audioStateFlow._state.trackList
        tList.firstOrNull { it.trackIndex == trackIndex }?.apply {
            inputDevice = device
            inputChannel = channel
        }
        audioStateFlow._state = audioStateFlow._state.copy(trackList = tList)
    }

    fun startInputStream(trackIndex: Int): AudioError {
        val tList = audioStateFlow._state.trackList
        val chosenTrack = tList.firstOrNull { it.trackIndex == trackIndex }
        chosenTrack?.inputDevice?.let {
            tList.forEach {otherTrack ->
                if (otherTrack.trackIndex != chosenTrack.trackIndex) {
                    if (otherTrack.audioStream != null) {
                        if (otherTrack.audioStream!!.device == chosenTrack.inputDevice) {
                            chosenTrack.audioStream = AudioStream(chosenTrack.inputDevice)
                            tList[trackIndex] = chosenTrack
                            audioStateFlow._state = audioStateFlow._state.copy(trackList = tList)
                            vuMeterThread.addToTracksStreaming(chosenTrack)
                            return AudioError.SoundIoErrorNone
                        }
                    }
                }
            }
            chosenTrack.audioStream = AudioStream(chosenTrack.inputDevice)
            tList[trackIndex] = chosenTrack
            audioStateFlow._state = audioStateFlow._state.copy(trackList = tList)
            val err = audioEngineManager.startInputStream(it.index)
            if (err == AudioError.SoundIoErrorNone) vuMeterThread.addToTracksStreaming(chosenTrack)
            return err
        }
        return AudioError.DevicesNotInitialized
    }

    fun stopInputStream(trackIndex: Int) {
        val tList = audioStateFlow._state.trackList
        val chosenTrack = tList.firstOrNull { it.trackIndex == trackIndex }
        chosenTrack?.audioStream?.let {
            tList.forEach {otherTrack ->
                if (otherTrack.trackIndex != chosenTrack.trackIndex) {
                    if (otherTrack.audioStream != null) {
                        if (otherTrack.audioStream!!.device == chosenTrack.inputDevice) {
                            chosenTrack.audioStream = null
                            tList[trackIndex] = chosenTrack
                            audioStateFlow._state = audioStateFlow._state.copy(trackList = tList)
                            vuMeterThread.removeFromTracksStreaming(chosenTrack)
                            return
                        }
                    }
                }
            }
            chosenTrack.audioStream = null
            vuMeterThread.removeFromTracksStreaming(chosenTrack)
            tList[trackIndex] = chosenTrack
            audioStateFlow._state = audioStateFlow._state.copy(trackList = tList)
            audioEngineManager.stopInputStream(it.device.index)
        }
    }

    fun getTrackInputDeviceIndex(trackIndex: Int): Int {
        val tList = audioStateFlow._state.trackList
        val track = tList[trackIndex]
        return track.inputDevice.index
    }

    fun outputSamplesProcessed(numSamples: Int) {
        viewModelController.reportAudioSamplesProcessed(numSamples)
    }

    fun startPlayback() {
        audioEngineManager.startPlayback()
    }

    fun stopPlayback() {
        audioEngineManager.stopPlayback()
    }

    fun updateCursorOffsetSamples(samples: Int) {
        audioStateFlow._state = audioStateFlow._state.copy(cursorOffsetSamples = samples)
    }

    fun saveCurrentCursorOffsetSamples(samples: Int) {
        audioStateFlow._state = audioStateFlow._state.copy(savedCursorOffsetSamples = samples)
    }

    fun resetCursorOffsetSamples() {
        val savedOffset = audioStateFlow._state.savedCursorOffsetSamples
        audioStateFlow._state = audioStateFlow._state.copy(cursorOffsetSamples = savedOffset)
    }
}