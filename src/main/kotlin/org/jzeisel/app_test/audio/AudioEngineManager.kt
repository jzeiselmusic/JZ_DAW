package org.jzeisel.app_test.audio

class AudioEngineManager {
    private val soundInterface = SoundIoInterface()
    private var initialized = false

    private var inputDevicesLoaded = false
    private var outputDevicesLoaded = false

    val defaultOutputIndex: Int get() { return soundInterface.defaultOutputDeviceIndex }

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

    fun getOutputDeviceFromIndex(index: Int): Device {
        val name = soundInterface.getOutputDeviceName(index)
        val id = soundInterface.getOutputDeviceId(index)
        val nChannels = soundInterface.getNumChannelsOfOutputDevice(index)
        return Device(index, name, id, Direction.OUTPUT, nChannels)
    }

    fun getInputDeviceFromIndex(index: Int): Device {
        val name = soundInterface.getInputDeviceName(index)
        val id = soundInterface.getInputDeviceId(index)
        val nChannels = soundInterface.getNumChannelsOfInputDevice(index)
        return Device(index, name, id, Direction.INPUT, nChannels)
    }

    fun getInputDeviceName(index: Int): String? {
        if (initialized) {
            if (index >= getNumAudioInputs()!! || index < 0) {
                return null
            }
            return soundInterface.getInputDeviceName(index)
        }
        else return null
    }

    fun getCurrentBackend(): AudioBackend? {
        if (initialized) {
            return AudioBackend.values()[soundInterface.currentBackend]
        }
        else return null
    }

    fun getChannelsFromDeviceIndex(deviceIndex: Int): List<Channel>? {
        if (initialized) {
            val channelList = mutableListOf<Channel>()
            repeat(soundInterface.getNumChannelsOfInputDevice(deviceIndex)) { channelIndex->
                channelList.add( Channel(deviceIndex, soundInterface.getNameOfChannelOfInputDevice(deviceIndex, channelIndex)))
            }
            return channelList
        }
        else return null
    }

    fun getInputDevices(): List<Device>? {
        if (initialized) {
            val deviceList = mutableListOf<Device>()
            repeat(soundInterface.numInputDevices) {
                deviceList.add(Device(it,
                    soundInterface.getInputDeviceName(it),
                    soundInterface.getInputDeviceId(it),
                    Direction.INPUT,
                    soundInterface.getNumChannelsOfInputDevice(it)))
            }
            return deviceList
        }
        else return null
    }
}