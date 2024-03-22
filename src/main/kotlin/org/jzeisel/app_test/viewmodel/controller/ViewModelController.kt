package org.jzeisel.app_test.viewmodel.controller

import org.jzeisel.app_test.audio.AudioError
import org.jzeisel.app_test.viewmodel.TrackListViewModel

class ViewModelController(private val viewModel: TrackListViewModel) {
    /* used by the audio model to communicate with the view model */
    fun throwPlaybackError(error: AudioError) {
        viewModel.onPlaybackError(error)
    }
}