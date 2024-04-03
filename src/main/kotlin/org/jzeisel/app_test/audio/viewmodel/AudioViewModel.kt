package org.jzeisel.app_test.audio.viewmodel

import org.jzeisel.app_test.audio.*
import org.jzeisel.app_test.error.AudioError
import org.jzeisel.app_test.util.Logger

class AudioViewModel(viewModelController: ViewModelController) {

    private val audioStateFlow = AudioStateFlow()
    private val audioEngineManager = AudioEngineManager()
    private val vuMeterThread = VUMeterThread(audioEngineManager, viewModelController)

    fun initialize() {
        audioEngineManager.initialize()
        audioStateFlow._state = audioStateFlow._state.copy(
            isInitialized = true,
            backend = audioEngineManager.getCurrentBackend()!!)
        Logger.debug(javaClass.simpleName,
            "connected to ${audioStateFlow._state.backend.readable}", 5)

        audioStateFlow._state =
            audioStateFlow._state.copy(outputDevice =
            audioEngineManager.getOutputDeviceFromIndex(audioEngineManager.defaultOutputIndex))
        Logger.debug(javaClass.simpleName,
            "default output: ${audioStateFlow._state.outputDevice.toString()}", 5)
    }

    fun deinitialize() {
        vuMeterThread.removeAllTracksFromStreaming()
        audioEngineManager.deinitialize()
    }

    private fun printTrackData() {
        Logger.debug(javaClass.simpleName, "tracks: ${audioStateFlow._state.numTracks}", 1)
        for (t in audioStateFlow._state.trackList) {
            Logger.debug(javaClass.simpleName, "index: ${t.trackIndex}", 1)
            Logger.debug(javaClass.simpleName, "\tname: ${t.trackName}", 1)
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

    fun addTrack(trackIndex: Int, trackName: String) {
        val nTracks = audioStateFlow._state.numTracks
        val tList = audioStateFlow._state.trackList
        tList.add(trackIndex, TrackData(trackName, trackIndex, 0.0, 0))
        audioStateFlow._state = audioStateFlow._state.copy(numTracks = nTracks + 1, trackList = tList)
    }

    fun removeTrack(trackIndex: Int) {
        val nTracks = audioStateFlow._state.numTracks
        val tList = audioStateFlow._state.trackList
        tList.firstOrNull { it.trackIndex == trackIndex }?.let { tList.remove(it) }
        audioStateFlow._state = audioStateFlow._state.copy(numTracks = nTracks - 1, trackList = tList)
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

    fun updateTrackName(trackIndex: Int, newName: String) {
        val tList = audioStateFlow._state.trackList
        tList.firstOrNull { it.trackIndex == trackIndex }?.apply { trackName = newName }
        audioStateFlow._state = audioStateFlow._state.copy(trackList = tList)
    }

    fun updateTrackIndex(name: String, newIndex: Int) {
        val tList = audioStateFlow._state.trackList
        tList.firstOrNull { it.trackName == name }?.apply { trackIndex = newIndex }
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
                            chosenTrack.audioStream = AudioStream(chosenTrack.inputDevice!!)
                            tList[trackIndex] = chosenTrack
                            audioStateFlow._state = audioStateFlow._state.copy(trackList = tList)
                            vuMeterThread.addToTracksStreaming(chosenTrack)
                            return AudioError.SoundIoErrorNone
                        }
                    }
                }
            }
            chosenTrack.audioStream = AudioStream(chosenTrack.inputDevice!!)
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
            tList[trackIndex] = chosenTrack
            audioStateFlow._state = audioStateFlow._state.copy(trackList = tList)
            audioEngineManager.stopInputStream(it.device.index)
            vuMeterThread.removeFromTracksStreaming(chosenTrack)
        }
    }
}