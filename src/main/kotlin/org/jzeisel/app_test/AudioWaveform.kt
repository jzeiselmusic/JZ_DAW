package org.jzeisel.app_test

import javafx.application.Application
import javafx.scene.Scene
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.scene.input.MouseEvent
import javafx.scene.layout.StackPane
import javafx.scene.paint.Color
import javafx.stage.Stage
import org.jzeisel.app_test.logger.Logger
import javafx.scene.input.KeyCode.BACK_SPACE

class AudioWaveform : Application() {
    companion object {
        private const val INIT_STAGE_HEIGHT = 440.0
        private const val INIT_STAGE_WIDTH = 950.0
    }
    private lateinit var root: StackPane

    override fun start(stage: Stage) {
        stage.title = "JZ Digital Audio Workstation"
        stage.width = INIT_STAGE_WIDTH
        stage.height = INIT_STAGE_HEIGHT + 4
        stage.isResizable = true

        Logger.setDebug(true)

        root = StackPane()
        val scene = Scene(root, null)
        scene.fill = Color.DIMGREY.darker().darker()
        stage.scene = scene

        val trackListViewModel = TrackListViewModel(root, stage)

        scene.addEventFilter(MouseEvent.MOUSE_CLICKED) {
            trackListViewModel.broadcastMouseClick(root)
        }
        scene.addEventFilter(KeyEvent.KEY_PRESSED) {
            if (it.code == BACK_SPACE) {
                trackListViewModel.broadcastBackSpace()
            }
            else if ((it.code.isLetterKey || it.code.isWhitespaceKey || it.code.isDigitKey)
                    && it.code != KeyCode.ENTER) {
                trackListViewModel.broadcastCharacter(it)
            }
        }
        trackListViewModel.addMeToScene(root)
        stage.show()
    }
}

fun main() {
    Application.launch(AudioWaveform::class.java)
}