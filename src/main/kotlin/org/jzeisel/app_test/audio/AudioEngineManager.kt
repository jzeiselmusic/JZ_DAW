package org.jzeisel.app_test.audio

import org.jzeisel.app_test.audio.viewmodel.AudioViewModel
import org.jzeisel.app_test.error.AudioError
import org.jzeisel.app_test.util.Logger

class AudioEngineManager(private val viewModel: AudioViewModel) {
    private val soundInterface = SoundIoInterface(this)
    private var initialized = false

    private var inputDevicesLoaded = false
    private var outputDevicesLoaded = false

    private val microphoneLatency = 0.01
    private val defaultSampleRate = 44100

    val defaultInputIndex: Int get() { return soundInterface.lib_getDefaultInputDeviceIndex() }
    val defaultOutputIndex: Int get() { return soundInterface.lib_getDefaultOutputDeviceIndex() }

    fun initialize() : AudioError {
        soundInterface.registerAudioLogCallback()
        soundInterface.registerAudioPanicCallback()
        soundInterface.registerInputStreamCallback()
        soundInterface.registerOutputStreamCallback()
        soundInterface.registerFloatPrintCallback()
        soundInterface.registerCharCallback()

        var returnError = soundInterface.lib_startSession()
        if (returnError != AudioError.SoundIoErrorNone.ordinal) {
            initialized = false
            return AudioError.values()[returnError]
        }
        returnError = soundInterface.lib_loadInputDevices()
        if (returnError != AudioError.SoundIoErrorNone.ordinal) {
            inputDevicesLoaded = false
            return AudioError.values()[returnError]
        }
        inputDevicesLoaded = true
        returnError = soundInterface.lib_loadOutputDevices()
        if (returnError != AudioError.SoundIoErrorNone.ordinal) {
            outputDevicesLoaded = false
            return AudioError.values()[returnError]
        }

        returnError = soundInterface.lib_createAndStartOutputStream(
            defaultOutputIndex, microphoneLatency, defaultSampleRate)
        if (returnError != AudioError.SoundIoErrorNone.ordinal) {
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
                deviceIndex, microphoneLatency, defaultSampleRate)
            if (err != 0) {
                return AudioError.InputStreamError
            }
            else return AudioError.SoundIoErrorNone
        }
        else {
            return AudioError.EnvironmentNotInitialized
        }
    }

    fun stopInputStream(deviceIndex: Int) {
        soundInterface.lib_stopInputStream(deviceIndex)
    }

    fun getCurrentRMSVolume(deviceIndex: Int): Double {
        return soundInterface.lib_getCurrentRmsVolume(deviceIndex)
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
}