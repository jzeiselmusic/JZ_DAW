package org.jzeisel.app_test

import javafx.application.Application
import javafx.scene.Scene
import javafx.scene.layout.Pane
import javafx.scene.layout.StackPane
import javafx.scene.paint.Color
import javafx.scene.paint.Paint
import javafx.stage.Stage
import org.jzeisel.app_test.audio.AudioProcessor
import org.jzeisel.app_test.audio.Recorder
import org.jzeisel.app_test.component.trackBar.tracks.TrackList
import org.jzeisel.app_test.logger.Logger

class AudioWaveform : Application() {
    companion object {
        private const val INIT_STAGE_HEIGHT = 360.0
        private const val INIT_STAGE_WIDTH = 640.0
    }
    private lateinit var root: StackPane

    override fun start(stage: Stage) {
        stage.title = "My App"
        stage.isResizable = true
        stage.width = INIT_STAGE_WIDTH
        stage.height = INIT_STAGE_HEIGHT + 4
        stage.isResizable = true

        Logger.setDebug(true)

        root = StackPane()
        val scene = Scene(root, null)
        scene.fill = Color.DIMGREY.darker().darker()
        stage.scene = scene

        val trackList = TrackList(root, stage)
        trackList.addMeToScene(root)
        stage.show()
    }
}

fun main() {
    Application.launch(AudioWaveform::class.java)
}