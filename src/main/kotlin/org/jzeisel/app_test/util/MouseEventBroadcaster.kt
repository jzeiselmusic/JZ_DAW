package org.jzeisel.app_test.util

import javafx.scene.Scene
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.scene.input.MouseEvent
import javafx.scene.input.ScrollEvent
import javafx.scene.layout.StackPane
import org.jzeisel.app_test.TrackListViewModel

object MouseEventBroadcaster {
    private lateinit var root: StackPane
    private lateinit var trackListViewModel: TrackListViewModel
    private lateinit var scene: Scene

    fun initializeBroadcasts(root: StackPane, scene: Scene, trackListViewModel: TrackListViewModel) {
        this.root = root
        this.scene = scene
        this.trackListViewModel = trackListViewModel
        scene.addEventFilter(MouseEvent.MOUSE_CLICKED) {
            broadcastMouseClick()
        }
        scene.addEventFilter(KeyEvent.KEY_PRESSED) {
            broadcastKeyPressForTyping(it)
        }
        scene.addEventFilter(ScrollEvent.SCROLL) {
            trackListViewModel.scrollSceneVertically(it.deltaY)
        }
    }

    private fun broadcastMouseClick() {
        trackListViewModel.broadcastMouseClick(root)
    }

    private fun broadcastKeyPressForTyping(event: KeyEvent) {
        if (event.code == KeyCode.BACK_SPACE) {
            trackListViewModel.broadcastBackSpace()
        }
        else if ((event.code.isLetterKey || event.code.isWhitespaceKey || event.code.isDigitKey)
                && event.code != KeyCode.ENTER) {
            trackListViewModel.broadcastCharacter(event)
        }
        else if (event.code == KeyCode.ENTER) {
            trackListViewModel.broadcastMouseClick(root)
        }
    }
}