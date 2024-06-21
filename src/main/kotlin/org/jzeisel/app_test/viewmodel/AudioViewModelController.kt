package org.jzeisel.app_test.viewmodel

class AudioViewModelController(private val audioViewModel: AudioViewModel) {

    fun setMetronome(enabled: Boolean) {
        audioViewModel.setMetronome(enabled)
    }
}