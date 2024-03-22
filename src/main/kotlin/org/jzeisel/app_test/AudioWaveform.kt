package org.jzeisel.app_test

import javafx.application.Application
import javafx.scene.Scene
import javafx.scene.layout.StackPane
import javafx.scene.paint.Color
import javafx.stage.Stage
import org.jzeisel.app_test.audio.AudioEngineManager
import org.jzeisel.app_test.audio.AudioError
import org.jzeisel.app_test.jna.SoundIoInterface
import org.jzeisel.app_test.util.Logger
import org.jzeisel.app_test.util.MouseEventBroadcaster
import org.jzeisel.app_test.util.viewOrderFlip
import org.jzeisel.app_test.viewmodel.TrackListViewModel

class AudioWaveform : Application() {
    companion object {
        private const val INIT_STAGE_HEIGHT = 550.0
        private const val INIT_STAGE_WIDTH = 950.0
    }
    private lateinit var root: StackPane
    private lateinit var everythingPane: StackPane
    private lateinit var verticalScrollBarPane: StackPane
    private lateinit var scene: Scene
    private lateinit var trackListViewModel: TrackListViewModel
    override fun start(stage: Stage) {
        /* the z values of the nodes should be laid out in the following way

                   waveform box and cursor - 10-20
                   track boxes and nodes - 30-40
                     -   divider lines - 41-49
                   scrollbars - 50-60
                     -   ephemerals (textbox, dropboxes) - 61-69
                   mixer - 70-80
                   plugin boxes - 100-200

         */
        stage.title = "JZ Digital Audio Workstation"
        stage.width = INIT_STAGE_WIDTH
        stage.height = INIT_STAGE_HEIGHT + 4
        stage.isResizable = true

        Logger.setDebug(true)

        root = StackPane()
        scene = Scene(root, null)
        everythingPane = StackPane()
        verticalScrollBarPane = StackPane()

        verticalScrollBarPane.isMouseTransparent = true
        verticalScrollBarPane.viewOrder = viewOrderFlip - 1.0
        everythingPane.viewOrder = viewOrderFlip - 0.5

        root.children.addAll(everythingPane, verticalScrollBarPane)
        scene.fill = Color.DIMGREY.darker().darker()
        stage.scene = scene
        trackListViewModel = TrackListViewModel(everythingPane, stage, verticalScrollBarPane)

        MouseEventBroadcaster.initializeBroadcasts(everythingPane, scene, trackListViewModel)

        trackListViewModel.addMeToScene(everythingPane)
        stage.show()
    }

    override fun stop() {
        Logger.debug(javaClass.simpleName, "application stopping", 5)
    }
}

fun main() {
    Application.launch(AudioWaveform::class.java)
}