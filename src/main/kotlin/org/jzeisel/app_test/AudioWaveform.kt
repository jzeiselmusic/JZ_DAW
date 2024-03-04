package org.jzeisel.app_test

import javafx.application.Application
import javafx.scene.Scene
import javafx.scene.layout.StackPane
import javafx.scene.paint.Color
import javafx.stage.Stage
import org.jzeisel.app_test.util.Logger
import org.jzeisel.app_test.util.MouseEventBroadcaster

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
        root.children.addAll(everythingPane, verticalScrollBarPane)
        scene.fill = Color.DIMGREY.darker().darker()
        stage.scene = scene

        trackListViewModel = TrackListViewModel(everythingPane, stage, verticalScrollBarPane)

        MouseEventBroadcaster.initializeBroadcasts(everythingPane, scene, trackListViewModel)

        trackListViewModel.addMeToScene(everythingPane)
        stage.show()
    }
}

fun main() {
    Application.launch(AudioWaveform::class.java)
}