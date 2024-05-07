package org.jzeisel.app_test.audio.viewmodel

import org.jzeisel.app_test.audio.*
import org.jzeisel.app_test.error.AudioError
import org.jzeisel.app_test.stateflow.TrackListStateFlow
import org.jzeisel.app_test.util.Logger
import kotlin.contracts.contract
import kotlin.random.Random

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
            viewModelController.throwAudioError(it)
        }

        audioEngineManager.getCurrentBackend()?.let {
            audioStateFlow._state = audioStateFlow._state.copy(isInitialized = true, backend = it)
        } ?: viewModelController.throwAudioError(AudioError.SoundIoErrorInitAudioBackend)

        audioStateFlow._state = audioStateFlow._state.copy(outputDevice =
            audioEngineManager.getOutputDeviceFromIndex(audioEngineManager.defaultOutputIndex))
    }

    fun deinitialize() {
        vuMeterThread.removeAllTracksFromStreaming()
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

    fun addTrack(trackId: Int) {
        val nTracks = viewModelState.numTracks
        val device = audioEngineManager.getInputDeviceFromIndex(audioEngineManager.defaultInputIndex)
        val defaultChannel = Channel(0, audioEngineManager.getNameOfChannelFromIndex(device.index, 0))
        val tList = audioStateFlow._state.trackList
        val trackData = TrackData(trackId = trackId, 0.0, 0, inputDevice = device, inputChannel = defaultChannel)
        tList.add(trackData)
        audioStateFlow._state = audioStateFlow._state.copy(
            numTracks = nTracks,
            trackList = tList
        )
        /* add track to audio library */
        val error = audioEngineManager.addNewTrack(trackId)
        error.whenNot(AudioError.SoundIoErrorNone) {
            viewModelController.throwAudioError(it)
        }
        /* then make sure the vu meter thread has the same information */
        vuMeterThread.updateSynchronizedTrackList(trackList = tList)

    }

    fun removeTrack(trackId: Int) {
        val nTracks = audioStateFlow._state.numTracks
        val tList = audioStateFlow._state.trackList
        stopInputStream(trackId)
        val track = tList.first { it.trackId == trackId }
        tList.remove(track)
        audioStateFlow._state = audioStateFlow._state.copy(
            numTracks = nTracks,
            trackList = tList
        )
        val error = audioEngineManager.deleteTrack(trackId)
        error.whenNot(AudioError.SoundIoErrorNone) {
            viewModelController.throwAudioError(it)
        }
        audioStateFlow._state = audioStateFlow._state.copy(numTracks = nTracks, trackList = tList)
        vuMeterThread.updateSynchronizedTrackList(tList)
    }

    fun setTrackDeviceAndChannel(trackId: Int, deviceIndex: Int, channelIndex: Int) {
        val device = audioEngineManager.getInputDeviceFromIndex(deviceIndex)
        val channel = audioEngineManager.getChannelsFromDeviceIndex(deviceIndex)!![channelIndex]
        val tList = audioStateFlow._state.trackList
        val track = tList.first{ it.trackId == trackId }
        track.apply {
            inputDevice = device
            inputChannel = channel
        }
        audioStateFlow._state = audioStateFlow._state.copy(trackList = tList)
        audioEngineManager
            .chooseInputIndexForTrack(track.trackId, deviceIndex)
            .whenNot(AudioError.SoundIoErrorNone) {
                viewModelController.throwAudioError(it)
            }
    }

    fun startInputStream(trackId: Int): AudioError {
        val tList = audioStateFlow._state.trackList
        val chosenTrack = tList.firstOrNull { it.trackId == trackId }
        chosenTrack?.inputDevice?.let {
            tList.forEach {otherTrack ->
                if (otherTrack.trackId != chosenTrack.trackId) {
                    if (otherTrack.audioStream != null) {
                        if (otherTrack.audioStream!!.device == chosenTrack.inputDevice) {
                            chosenTrack.audioStream = AudioStream(chosenTrack.inputDevice)
                            audioStateFlow._state = audioStateFlow._state.copy(trackList = tList)
                            vuMeterThread.addToTracksStreaming(chosenTrack)
                            return AudioError.SoundIoErrorNone
                        }
                    }
                }
            }
            chosenTrack.audioStream = AudioStream(chosenTrack.inputDevice)
            audioStateFlow._state = audioStateFlow._state.copy(trackList = tList)
            val err = audioEngineManager.startInputStream(it.index)
            if (err == AudioError.SoundIoErrorNone) vuMeterThread.addToTracksStreaming(chosenTrack)
            return err
        }
        return AudioError.DevicesNotInitialized
    }

    fun stopInputStream(trackId: Int) {
        val tList = audioStateFlow._state.trackList
        val chosenTrack = tList.firstOrNull { it.trackId == trackId }
        chosenTrack?.audioStream?.let {
            tList.forEach {otherTrack ->
                if (otherTrack.trackId != chosenTrack.trackId) {
                    if (otherTrack.audioStream != null) {
                        if (otherTrack.audioStream!!.device == chosenTrack.inputDevice) {
                            chosenTrack.audioStream = null
                            audioStateFlow._state = audioStateFlow._state.copy(trackList = tList)
                            vuMeterThread.removeFromTracksStreaming(chosenTrack)
                            return
                        }
                    }
                }
            }
            chosenTrack.audioStream = null
            vuMeterThread.removeFromTracksStreaming(chosenTrack)
            audioStateFlow._state = audioStateFlow._state.copy(trackList = tList)
            audioEngineManager.stopInputStream(it.device.index)
        }
    }

    fun getTrackInputDeviceIndex(trackId: Int): Int {
        val tList = audioStateFlow._state.trackList
        val track = tList.first { it.trackId == trackId }
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

    fun armRecording(trackId: Int) {
        val tList = audioStateFlow._state.trackList
        val track = tList.first { it.trackId == trackId }
        track.armedForRecording = true
        audioStateFlow._state = audioStateFlow._state.copy( trackList = tList )
        vuMeterThread.updateSynchronizedTrackList(audioStateFlow._state.trackList)

        audioEngineManager.armTrackForRecording(trackId).whenNot(AudioError.SoundIoErrorNone) {
            viewModelController.throwAudioError(it)
        }
    }

    fun disarmRecording(trackId: Int) {
        val tList = audioStateFlow._state.trackList
        val track = tList.first { it.trackId == trackId }
        track.armedForRecording = false
        audioStateFlow._state = audioStateFlow._state.copy( trackList = tList )
        vuMeterThread.updateSynchronizedTrackList(audioStateFlow._state.trackList)

        audioEngineManager.disarmTrackForRecording(trackId).whenNot(AudioError.SoundIoErrorNone) {
            viewModelController.throwAudioError(it)
        }
    }
}