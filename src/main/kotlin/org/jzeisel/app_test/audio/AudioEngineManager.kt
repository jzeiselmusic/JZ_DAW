package org.jzeisel.app_test.audio

import org.jzeisel.app_test.jna.SoundIoInterface

class AudioEngineManager {
    private val soundInterface = SoundIoInterface()
    private var initialized = false

    private var inputDevicesLoaded = false
    private var outputDevicesLoaded = false

    var currentInputDevice: Device? = null
    var currentOutputDevice: Device? = null
    fun initialize() : AudioError  {
        var returnError = soundInterface.initializeEnvironment()
        if (returnError != AudioError.SoundIoErrorNone.ordinal) {
            initialized = false
            return AudioError.values()[returnError]
        }
        returnError = soundInterface.connectToBackend()
        if (returnError != AudioError.SoundIoErrorNone.ordinal) {
            initialized = false
            return AudioError.values()[returnError]
        }
        soundInterface.loadInputDevices()
        inputDevicesLoaded = true
        soundInterface.loadOutputDevices()
        outputDevicesLoaded = true
        initialized = true
        return AudioError.SoundIoErrorNone
    }

    fun deinitialize(): AudioError {
        if (initialized) {
            initialized = false
            inputDevicesLoaded = false
            outputDevicesLoaded = false
            currentInputDevice = null
            currentOutputDevice = null
            soundInterface.destroySession()
            return AudioError.SoundIoErrorNone
        }
        return AudioError.SoundIoErrorInitAudioBackend
    }

    fun getNumAudioInputs(): Int? {
        if (initialized) {
            return soundInterface.numInputDevices
        }
        else return null
    }

    fun getNumAudioOutputs(): Int? {
        if (initialized) {
            return soundInterface.numOutputDevices
        }
        else return null
    }

    fun chooseInputDevice(index: Int): AudioError {
        if (initialized) {
            if (index >= getNumAudioInputs()!! || index < 0) {
                return AudioError.IndexOutOfBounds
            }
            soundInterface.pickCurrentInputDevice(index)
            val id = soundInterface.getInputDeviceId(index)
            val name = soundInterface.getInputDeviceName(index)
            val channels = soundInterface.numChannelsOfCurrentInputDevice
            currentInputDevice = Device(index, name, id, Direction.INPUT, channels)
            return AudioError.SoundIoErrorNone
        }
        else return AudioError.SoundIoErrorInitAudioBackend
    }

    fun chooseOutputDevice(index: Int): AudioError {
        if (initialized) {
            if (index >= getNumAudioOutputs()!! || index < 0) {
                return AudioError.IndexOutOfBounds
            }
            soundInterface.pickCurrentOutputDevice(index)
            val id = soundInterface.getOutputDeviceId(index)
            val name = soundInterface.getOutputDeviceName(index)
            val channels = soundInterface.numChannelsOfCurrentOutputDevice
            currentOutputDevice = Device(index, name, id, Direction.OUTPUT, channels)
            return AudioError.SoundIoErrorNone
        }
        else return AudioError.SoundIoErrorInitAudioBackend
    }
}