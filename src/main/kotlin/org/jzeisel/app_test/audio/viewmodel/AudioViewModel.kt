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
        val error = audioEngineManager.initialize()
        if (error != AudioError.SoundIoErrorNone) {
            viewModelController.throwAudioError(error)
            return
        }
        audioEngineManager.getCurrentBackend()?.let {
            audioStateFlow._state = audioStateFlow._state.copy(isInitialized = true, backend = it)
        } ?: viewModelController.throwAudioError(AudioError.SoundIoErrorInitAudioBackend)

        audioStateFlow._state = audioStateFlow._state.copy(outputDevice =
            audioEngineManager.getOutputDeviceFromIndex(audioEngineManager.defaultOutputIndex))

        vuMeterThread.addAllTracksToStreaming()
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
        // stopInputStream(trackId)
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

    fun deleteFile(trackId: Int, fileId: Int) {
        audioEngineManager.deleteFile(trackId, fileId).whenNot(AudioError.SoundIoErrorNone) {
            viewModelController.throwAudioError(it)
        }
    }

    fun moveFile(destTrackId: Int, sourceTrackId: Int, sourceFileId: Int) {
        audioEngineManager.moveFile(destTrackId, sourceTrackId, sourceFileId).whenNot(AudioError.SoundIoErrorNone) {
            viewModelController.throwAudioError(it)
        }
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
            .chooseInputDeviceIndexForTrack(track.trackId, deviceIndex)
            .whenNot(AudioError.SoundIoErrorNone) {
                viewModelController.throwAudioError(it)
            }
        audioEngineManager
            .chooseInputChannelIndexForTrack(track.trackId, channelIndex)
            .whenNot(AudioError.SoundIoErrorNone) {
                viewModelController.throwAudioError(it)
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

    fun startPlayback(fileId: Int) {
        val error = audioEngineManager.startPlayback(fileId)
        if (error != AudioError.SoundIoErrorNone) {
            viewModelController.throwAudioError(error)
        }
        else {
            audioStateFlow._state = audioStateFlow._state.copy(isPlayingBack = true)
        }
    }

    fun stopPlayback() {
        audioEngineManager.stopPlayback()
        audioStateFlow._state = audioStateFlow._state.copy(isPlayingBack = false)
    }

    fun updateCursorOffsetSamples(samples: Int) {
        audioStateFlow._state = audioStateFlow._state.copy(cursorOffsetSamples = samples)
        audioEngineManager.updateCursorOffsetSamples(samples)
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
        track.recordingEnabled = true
        audioStateFlow._state = audioStateFlow._state.copy( trackList = tList )
        vuMeterThread.updateSynchronizedTrackList(audioStateFlow._state.trackList)

        audioEngineManager.armTrackForRecording(trackId).whenNot(AudioError.SoundIoErrorNone) {
            viewModelController.throwAudioError(it)
        }
    }

    fun disarmRecording(trackId: Int) {
        val tList = audioStateFlow._state.trackList
        val track = tList.first { it.trackId == trackId }
        track.recordingEnabled = false
        audioStateFlow._state = audioStateFlow._state.copy( trackList = tList )
        vuMeterThread.updateSynchronizedTrackList(audioStateFlow._state.trackList)

        audioEngineManager.disarmTrackForRecording(trackId).whenNot(AudioError.SoundIoErrorNone) {
            viewModelController.throwAudioError(it)
        }
    }

    fun enableInputForTrack(trackId: Int) : Boolean {
        var ret = true
        val tList = audioStateFlow._state.trackList
        val track = tList.first { it.trackId == trackId }
        track.inputEnabled = true
        audioStateFlow._state = audioStateFlow._state.copy( trackList = tList )
        vuMeterThread.updateSynchronizedTrackList(audioStateFlow._state.trackList)

        audioEngineManager.inputEnable(trackId, true).whenNot(AudioError.SoundIoErrorNone) {
            viewModelController.throwAudioError(it)
            ret = false
        }
        return ret
    }

    fun disableInputForTrack(trackId: Int) : Boolean {
        var ret = true
        val tList = audioStateFlow._state.trackList
        val track = tList.first { it.trackId == trackId }
        track.inputEnabled = false
        audioStateFlow._state = audioStateFlow._state.copy( trackList = tList )
        vuMeterThread.updateSynchronizedTrackList(audioStateFlow._state.trackList)

        audioEngineManager.inputEnable(trackId, false).whenNot(AudioError.SoundIoErrorNone) {
            viewModelController.throwAudioError(it)
            ret = false
        }
        return ret
    }

    fun getRmsVolumeInputStream(trackId: Int) : Double {
        return audioEngineManager.getRmsVolumeInputStream(trackId)
    }

    fun getRmsVolumeTrackPlayback(trackId: Int) : Double {
        return audioEngineManager.getRmsVolumeTrackPlayback(trackId)
    }

    fun updateTrackOffset(trackId: Int, fileId: Int, newOffset: Int) {
        audioEngineManager.updateTrackOffset(trackId, fileId, newOffset).whenNot(AudioError.SoundIoErrorNone) {
            viewModelController.throwAudioError(it)
        }
    }

    fun setSolo(enabled: Boolean, trackId: Int) {
        audioEngineManager.setSolo(enabled, trackId).whenNot(AudioError.SoundIoErrorNone) {
            viewModelController.throwAudioError(it)
        }
    }

    fun setMute(enabled: Boolean, trackId: Int) {
        audioEngineManager.setMute(enabled, trackId).whenNot(AudioError.SoundIoErrorNone) {
            viewModelController.throwAudioError(it)
        }
    }

    fun bounceMasterToWav(startSample: Int, endSample: Int) {
        audioEngineManager.bounceMasterToWav(startSample, endSample).whenNot(AudioError.SoundIoErrorNone) {
            viewModelController.throwAudioError(it)
        }
    }
}