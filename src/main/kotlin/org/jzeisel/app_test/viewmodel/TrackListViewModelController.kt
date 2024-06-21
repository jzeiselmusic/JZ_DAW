package org.jzeisel.app_test.viewmodel

import org.jzeisel.app_test.components.NormalTrack
import org.jzeisel.app_test.error.AudioError

class TrackListViewModelController(private val viewModel: TrackListViewModel) {
    /* used by the audio model to communicate with the view model */
    fun throwPlaybackError(error: AudioError) {
        viewModel.onPlaybackError(error)
    }

    fun throwAudioError(error: AudioError) {
        viewModel.createAudioErrorMessage(error)
    }

    fun sendTrackRMSVolume(volume: Double, trackId: Int) {
        viewModel.updateTrackRMSVolume(volume, trackId)
    }

    fun sendOutputRMSVolume(volume: Double) {
        viewModel.updateOutputRMSVolume(volume)
    }

    fun getCopyOfTracks(): List<NormalTrack> {
        return viewModel.getCopyOfTracks()
    }

    fun reportAudioSamplesProcessed(numSamples: Int) {
        viewModel.onAudioSamplesProcessed(numSamples)
    }
}