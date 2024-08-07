package org.jzeisel.app_test

import javafx.application.Application
import javafx.scene.Scene
import javafx.scene.input.MouseEvent
import javafx.scene.layout.StackPane
import javafx.scene.paint.Color
import javafx.stage.Stage
import org.jzeisel.app_test.util.Logger
import org.jzeisel.app_test.util.MouseEventBroadcaster
import org.jzeisel.app_test.util.viewOrderFlip
import org.jzeisel.app_test.audio.viewmodel.AudioViewModel
import org.jzeisel.app_test.viewmodel.TrackListViewModel
import org.jzeisel.app_test.audio.viewmodel.ViewModelController
import org.jzeisel.app_test.viewmodel.MixerViewModel
import javax.sound.sampled.Mixer

class AudioWaveform : Application() {
    companion object {
        private const val INIT_STAGE_HEIGHT = 550.0
        private const val INIT_STAGE_WIDTH = 900.0
    }
    private lateinit var root: StackPane
    private lateinit var everythingPane: StackPane
    private lateinit var verticalScrollBarPane: StackPane
    private lateinit var mixerPane: StackPane
    private lateinit var scene: Scene
    private lateinit var trackListViewModel: TrackListViewModel
    private lateinit var audioViewModel: AudioViewModel
    private lateinit var mixerViewModel: MixerViewModel
    private lateinit var viewModelController: ViewModelController
    /*

    the z values of the nodes will be laid out in the following way

           waveform box and cursor - 10-20
           track boxes and nodes - 30-40
             -   divider lines - 41-49
           scrollbars - 50-60
             -   ephemerals (textbox, dropboxes) - 61-69
           mixer - 70-80
           error msgs - 100-120
           
    */
    override fun start(stage: Stage) {
        stage.title = "JZ Digital Audio Workstation"
        stage.width = INIT_STAGE_WIDTH
        stage.height = INIT_STAGE_HEIGHT + 4
        stage.isResizable = true

        Logger.setDebug(true)

        root = StackPane()
        scene = Scene(root, null)
        everythingPane = StackPane()
        verticalScrollBarPane = StackPane()
        mixerPane = StackPane()

        verticalScrollBarPane.isMouseTransparent = true
        verticalScrollBarPane.viewOrder = viewOrderFlip - 1.0
        everythingPane.viewOrder = viewOrderFlip - 0.5
        mixerPane.viewOrder = viewOrderFlip - 2.0
        mixerPane.isMouseTransparent = false

        root.children.addAll(everythingPane, verticalScrollBarPane, mixerPane)
        scene.fill = Color.DIMGREY.darker().darker()
        stage.scene = scene

        trackListViewModel = TrackListViewModel(everythingPane, stage, verticalScrollBarPane)
        viewModelController = ViewModelController(trackListViewModel)
        audioViewModel = AudioViewModel(viewModelController, trackListViewModel._trackListStateFlow)
        trackListViewModel.addAudioEngine(audioViewModel)
        mixerViewModel = MixerViewModel(mixerPane, viewModelController, trackListViewModel._trackListStateFlow, audioViewModel)
        trackListViewModel.addMixer(mixerViewModel, mixerPane)

        MouseEventBroadcaster.initializeBroadcasts(everythingPane, scene, trackListViewModel)

        trackListViewModel.addMeToScene(everythingPane)
        stage.show()
    }

    override fun stop() {
        Logger.debug(javaClass.simpleName, "quit application", 5)
        audioViewModel.deinitialize()
    }
}

fun main() {
    Application.launch(AudioWaveform::class.java)
}