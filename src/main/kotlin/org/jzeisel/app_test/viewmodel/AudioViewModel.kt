package org.jzeisel.app_test.viewmodel

import org.jzeisel.app_test.audio.AudioEngineManager
import org.jzeisel.app_test.audio.Channel
import org.jzeisel.app_test.audio.Device
import org.jzeisel.app_test.stateflow.AudioStateFlow
import org.jzeisel.app_test.util.Logger
import org.jzeisel.app_test.viewmodel.controller.ViewModelController

class AudioViewModel(val viewModelController: ViewModelController) {

    private val _audioStateFlow = AudioStateFlow()
    private val audioEngineManager = AudioEngineManager()

    fun initialize() {
        audioEngineManager.initialize()
        _audioStateFlow.state = _audioStateFlow.state.copy(
            isInitialized = true,
            backend = audioEngineManager.getCurrentBackend()!!)
        Logger.debug(javaClass.simpleName,
            "connected to ${_audioStateFlow.state.backend.readable}", 5)

        _audioStateFlow.state =
            _audioStateFlow.state.copy(outputDevice =
            audioEngineManager.getOutputDeviceFromIndex(audioEngineManager.defaultOutputIndex))
        Logger.debug(javaClass.simpleName,
            "default output: ${_audioStateFlow.state.outputDevice.toString()}", 5)
    }

    fun deinitialize() {
        audioEngineManager.deinitialize()
    }

    fun getChannelsFromDevice(index: Int): List<Channel>? {
        if (_audioStateFlow.state.isInitialized) return audioEngineManager.getChannelsFromDeviceIndex(index)!!
        else return null
    }

    fun getInputDeviceList(): List<Device>? {
        if (_audioStateFlow.state.isInitialized) return audioEngineManager.getInputDevices()
        else return null
    }
}