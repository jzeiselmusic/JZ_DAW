package org.jzeisel.app_test.viewmodel

import org.jzeisel.app_test.audio.AudioEngineManager
import org.jzeisel.app_test.stateflow.AudioStateFlow
import org.jzeisel.app_test.viewmodel.controller.ViewModelController

class AudioViewModel(val viewModelController: ViewModelController) {

    private val _audioStateFlow = AudioStateFlow()
    private val audioEngineManager = AudioEngineManager()
}