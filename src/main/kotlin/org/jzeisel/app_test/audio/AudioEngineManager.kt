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

    fun getOutputDeviceFromIndex(deviceIndex: Int): Device {
        val name = soundInterface.getOutputDeviceName(deviceIndex)
        val id = soundInterface.getOutputDeviceId(deviceIndex)
        val nChannels = soundInterface.getNumChannelsOfOutputDevice(deviceIndex)
        val channels =
            List(nChannels) { Channel(0, "") }
        channels.forEachIndexed { channelIndex, element ->
            element.index = channelIndex
            element.name =
                soundInterface.getNameOfChannelOfInputDevice(deviceIndex, channelIndex)
        }
        return Device(deviceIndex, name, id, Direction.OUTPUT, channels)
    }

    fun getInputDeviceFromIndex(deviceIndex: Int): Device {
        val name = soundInterface.getInputDeviceName(deviceIndex)
        val id = soundInterface.getInputDeviceId(deviceIndex)
        val nChannels = soundInterface.getNumChannelsOfInputDevice(deviceIndex)
        val channels =
            List(nChannels) { Channel(0, "") }
        channels.forEachIndexed { channelIndex, element ->
            element.index = channelIndex
            element.name =
                soundInterface.getNameOfChannelOfInputDevice(deviceIndex, channelIndex)
        }
        return Device(deviceIndex, name, id, Direction.INPUT, channels)
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
            repeat(soundInterface.numInputDevices) {deviceIndex ->
                val name = soundInterface.getInputDeviceName(deviceIndex)
                val deviceId = soundInterface.getInputDeviceId(deviceIndex)
                val numChannels = soundInterface.getNumChannelsOfInputDevice(deviceIndex)
                val channels =
                    List(numChannels) { Channel(0, "") }
                channels.forEachIndexed { index, element ->
                    element.index = index
                    element.name =
                        soundInterface.getNameOfChannelOfInputDevice(deviceIndex, index)
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
            val err: Int = soundInterface.start_input_stream(deviceIndex)
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
        soundInterface.stop_input_stream(deviceIndex)
    }
}