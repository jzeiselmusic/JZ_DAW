package org.jzeisel.app_test.util

import javafx.scene.Scene
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.scene.input.MouseEvent
import javafx.scene.layout.StackPane
import org.jzeisel.app_test.TrackListViewModel

object EventFilter {

    fun initializeBroadcasts(root: StackPane, scene: Scene, trackListViewModel: TrackListViewModel) {
        scene.addEventFilter(MouseEvent.MOUSE_CLICKED) {
            broadcastMouseClick(trackListViewModel, root)
        }
        scene.addEventFilter(KeyEvent.KEY_PRESSED) {
            broadcastKeyPressForTyping(it, trackListViewModel)
        }
    }

    private fun broadcastMouseClick(trackListViewModel: TrackListViewModel, root: StackPane) {
        trackListViewModel.broadcastMouseClick(root)
    }

    private fun broadcastKeyPressForTyping(event: KeyEvent, trackListViewModel: TrackListViewModel) {
        if (event.code == KeyCode.BACK_SPACE) {
            trackListViewModel.broadcastBackSpace()
        }
        else if ((event.code.isLetterKey || event.code.isWhitespaceKey || event.code.isDigitKey)
                && event.code != KeyCode.ENTER) {
            trackListViewModel.broadcastCharacter(event)
        }
    }
}