package org.jzeisel.app_test.audio

import org.jzeisel.app_test.audio.viewmodel.AudioViewModel
import org.jzeisel.app_test.error.AudioError
import org.jzeisel.app_test.util.Logger
import kotlin.math.log10

class AudioEngineManager(private val viewModel: AudioViewModel) {
    private val soundInterface = SoundIoInterface(this)
    private var initialized = false

    private var inputDevicesLoaded = false
    private var outputDevicesLoaded = false

    private val microphoneLatency = 0.01

    val defaultInputIndex: Int get() { return soundInterface.lib_getDefaultInputDeviceIndex() }
    val defaultOutputIndex: Int get() { return soundInterface.lib_getDefaultOutputDeviceIndex() }

    fun registerAllCallbackFuncs() {
        soundInterface.registerAudioLogCallback()
        soundInterface.registerAudioPanicCallback()
        soundInterface.registerInputStreamCallback()
        soundInterface.registerOutputStreamCallback()
        soundInterface.registerFloatPrintCallback()
        soundInterface.registerCharCallback()
        soundInterface.registerOutputProcessedCallback()
    }

    fun initialize() : AudioError {
        registerAllCallbackFuncs()
        var returnError = soundInterface.lib_startSession()
        if (returnError != AudioError.SoundIoErrorNone.ordinal) {
            Logger.debug(javaClass.simpleName, "error starting session", 5)
            initialized = false
            return AudioError.values()[returnError]
        }
        returnError = soundInterface.lib_loadInputDevices()
        if (returnError != AudioError.SoundIoErrorNone.ordinal) {
            Logger.debug(javaClass.simpleName, "error loading input devices", 5)
            inputDevicesLoaded = false
            return AudioError.values()[returnError]
        }

        returnError = soundInterface.lib_createAndStartInputStream(
            defaultInputIndex, microphoneLatency, viewModel.sampleRate)

        if (returnError != AudioError.SoundIoErrorNone.ordinal) {
            Logger.debug(javaClass.simpleName, "error starting input stream", 5)
            return AudioError.values()[returnError]
        }

        inputDevicesLoaded = true

        returnError = soundInterface.lib_loadOutputDevices()
        if (returnError != AudioError.SoundIoErrorNone.ordinal) {
            Logger.debug(javaClass.simpleName, "error loading output devices", 5)
            outputDevicesLoaded = false
            return AudioError.values()[returnError]
        }

        returnError = soundInterface.lib_createAndStartOutputStream(
            defaultOutputIndex, microphoneLatency, viewModel.sampleRate)

        if (returnError != AudioError.SoundIoErrorNone.ordinal) {
            Logger.debug(javaClass.simpleName, "error starting output stream", 5)
            return AudioError.values()[returnError]
        }

        outputDevicesLoaded = true
        initialized = true
        return AudioError.SoundIoErrorNone
    }

    fun deinitialize(): AudioError {
        if (initialized) {
            initialized = false
            inputDevicesLoaded = false
            outputDevicesLoaded = false
            val returnError = soundInterface.lib_destroySession()
            return AudioError.values()[returnError]
        }
        return AudioError.SoundIoErrorInitAudioBackend
    }

    fun getNumAudioInputs(): Int? {
        if (initialized) {
            return soundInterface.lib_getNumInputDevices()
        }
        else return null
    }

    fun getNumAudioOutputs(): Int? {
        if (initialized) {
            return soundInterface.lib_getNumOutputDevices()
        }
        else return null
    }

    fun getOutputDeviceFromIndex(deviceIndex: Int): Device {
        val name = soundInterface.lib_getOutputDeviceName(deviceIndex)
        val id = soundInterface.lib_getOutputDeviceId(deviceIndex)
        val nChannels = soundInterface.lib_getNumChannelsOfOutputDevice(deviceIndex)
        val channels =
            List(nChannels) { Channel(0, "") }
        channels.forEachIndexed { channelIndex, element ->
            element.index = channelIndex
            element.name =
                soundInterface.lib_getNameOfChannelOfInputDevice(deviceIndex, channelIndex)
        }
        return Device(deviceIndex, name, id, Direction.OUTPUT, channels)
    }

    fun getInputDeviceFromIndex(deviceIndex: Int): Device {
        val name = soundInterface.lib_getInputDeviceName(deviceIndex)
        val id = soundInterface.lib_getInputDeviceId(deviceIndex)
        val nChannels = soundInterface.lib_getNumChannelsOfInputDevice(deviceIndex)
        val channels =
            List(nChannels) { Channel(0, "") }
        channels.forEachIndexed { channelIndex, element ->
            element.index = channelIndex
            element.name =
                soundInterface.lib_getNameOfChannelOfInputDevice(deviceIndex, channelIndex)
        }
        return Device(deviceIndex, name, id, Direction.INPUT, channels)
    }

    fun getInputDeviceName(index: Int): String? {
        if (initialized) {
            if (index >= getNumAudioInputs()!! || index < 0) {
                return null
            }
            return soundInterface.lib_getInputDeviceName(index)
        }
        else return null
    }

    fun getCurrentBackend(): AudioBackend? {
        if (initialized) {
            return AudioBackend.values()[soundInterface.lib_getCurrentBackend()]
        }
        else return null
    }

    fun getChannelsFromDeviceIndex(deviceIndex: Int): List<Channel>? {
        if (initialized) {
            val channelList = mutableListOf<Channel>()
            repeat(soundInterface.lib_getNumChannelsOfInputDevice(deviceIndex)) { channelIndex->
                channelList.add( Channel(deviceIndex, soundInterface.lib_getNameOfChannelOfInputDevice(deviceIndex, channelIndex)))
            }
            return channelList
        }
        else return null
    }

    fun getInputDevices(): List<Device>? {
        if (initialized) {
            val deviceList = mutableListOf<Device>()
            repeat(soundInterface.lib_getNumInputDevices()) {deviceIndex ->
                val name = soundInterface.lib_getInputDeviceName(deviceIndex)
                val deviceId = soundInterface.lib_getInputDeviceId(deviceIndex)
                val numChannels = soundInterface.lib_getNumChannelsOfInputDevice(deviceIndex)
                val channels =
                    List(numChannels) { Channel(0, "") }
                channels.forEachIndexed { index, element ->
                    element.index = index
                    element.name =
                        soundInterface.lib_getNameOfChannelOfInputDevice(deviceIndex, index)
                }
                deviceList.add(
                    Device(deviceIndex,
                        name,
                        deviceId,
                        Direction.INPUT,
                        channels
                    )
                )
            }
            return deviceList
        }
        else return null
    }

    fun startInputStream(deviceIndex: Int): AudioError {
        if (initialized && inputDevicesLoaded) {
            Logger.debug(javaClass.simpleName, "creating input stream", 5)
            val err: Int = soundInterface.lib_createAndStartInputStream(
                deviceIndex, microphoneLatency, viewModel.sampleRate)
            if (err != 0) {
                return AudioError.InputStreamError
            }
            else return AudioError.SoundIoErrorNone
        }
        else {
            return AudioError.EnvironmentNotInitialized
        }
    }

    fun startPlayback(fileId: Int): AudioError {
        val error = soundInterface.lib_startPlayback(fileId)
        return AudioError.values()[error]
    }

    fun stopPlayback() {
        soundInterface.lib_stopPlayback()
    }

    fun stopInputStream(deviceIndex: Int) {
        soundInterface.lib_stopInputStream()
    }

    fun getNameOfChannelFromIndex(deviceIndex: Int, channelIndex: Int) : String{
        return soundInterface.lib_getNameOfChannelOfInputDevice(deviceIndex, channelIndex)
    }

    fun audioPanic(message: String) {
        Logger.debug("AUDIO PANIC", message, 1)
    }

    fun audioLog(message: String) {
        Logger.debug("AUDIO LOG", message, 3)
    }

    fun inputStreamCallback(message: String, index: Int) {
        Logger.debug("INPUT STREAM", "device $index: $message", 2)
    }

    fun outputStreamCallback(message: String, index: Int) {
        Logger.debug("OUTPUT STREAM", "device $index: $message", 4)
    }

    fun floatPrintCallback(message: String, value: Float) {
        Logger.debug("AUDIO STREAM", "$message: $value", 5)
    }

    fun charPrintCallback(value: Char, offset: Int) {
        Logger.debug("CHAR", "value at offset $offset is ${value.code}", 1)
    }

    fun outputProcessedCallback(numSamples: Int) {
        viewModel.outputSamplesProcessed(numSamples)
    }

    fun addNewTrack(trackId: Int) : AudioError {
        val error = soundInterface.lib_addNewTrack(trackId)
        return AudioError.values()[error]
    }

    fun deleteTrack(trackId: Int) : AudioError {
        val error = soundInterface.lib_deleteTrack(trackId)
        return AudioError.values()[error]
    }

    fun chooseInputDeviceIndexForTrack(trackId: Int, deviceIndex: Int) : AudioError {
        val error = soundInterface.lib_trackChooseInputDevice(trackId, deviceIndex)
        return AudioError.values()[error]
    }

    fun chooseInputChannelIndexForTrack(trackId: Int, channelIndex: Int) : AudioError {
        val error = soundInterface.lib_trackChooseInputChannel(trackId, channelIndex)
        return AudioError.values()[error]
    }

    fun armTrackForRecording(trackId: Int) : AudioError {
        val error = soundInterface.lib_armTrackForRecording(trackId)
        return AudioError.values()[error]
    }

    fun disarmTrackForRecording(trackId: Int) : AudioError {
        val error = soundInterface.lib_disarmTrackForRecording(trackId)
        return AudioError.values()[error]
    }

    fun updateCursorOffsetSamples(offset: Int) {
        soundInterface.lib_updateCursorOffsetSamples(offset)
    }

    fun inputEnable(trackId: Int, enable: Boolean) : AudioError {
        val error = soundInterface.lib_inputEnable(trackId, enable)
        return AudioError.values()[error]
    }

    fun getRmsVolume(trackId: Int) : Double {
        /* filtered with envelope follower for visualization */
        val rms: Double = soundInterface.lib_getRmsVolume(trackId)
        return 20.0 * log10(rms)
    }

    fun getRmsVolumeRaw(trackId: Int) : Double {
        /* unfiltered audio data, rms average for the latest buffer */
        val rms: Double = soundInterface.lib_getCurrentVolumeRaw(trackId)
        return 20.0 * log10(rms)
    }

    fun updateTrackOffset(trackId: Int, fileId: Int, newOffset: Int): AudioError {
        val error = soundInterface.lib_updateTrackOffset(trackId, fileId, newOffset)
        return AudioError.values()[error]
    }
}