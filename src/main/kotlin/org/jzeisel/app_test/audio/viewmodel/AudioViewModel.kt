package org.jzeisel.app_test.audio.viewmodel

import org.jzeisel.app_test.audio.*
import org.jzeisel.app_test.util.Logger

class AudioViewModel(val viewModelController: ViewModelController) {

    private val audioStateFlow = AudioStateFlow()
    private val audioEngineManager = AudioEngineManager()

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
        audioEngineManager.deinitialize()
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
        tList.add(trackIndex, TrackData(trackName, trackIndex, null, null, 0.0, 0))
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
}