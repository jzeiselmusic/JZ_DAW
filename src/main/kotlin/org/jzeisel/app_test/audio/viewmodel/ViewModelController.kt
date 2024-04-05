package org.jzeisel.app_test.audio.viewmodel

import org.jzeisel.app_test.error.AudioError
import org.jzeisel.app_test.viewmodel.TrackListViewModel

class ViewModelController(private val viewModel: TrackListViewModel) {
    /* used by the audio model to communicate with the view model */
    fun throwPlaybackError(error: AudioError) {
        viewModel.onPlaybackError(error)
    }

    fun throwAudioStartupError(error: AudioError) {
        viewModel.createAudioErrorMessage(error)
    }

    fun sendTrackRMSVolume(volume: Double, trackIndex: Int) {
        viewModel.updateTrackRMSVolume(volume, trackIndex)
    }
}