package org.jzeisel.app_test.util

import javafx.scene.Scene
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.scene.input.MouseEvent
import javafx.scene.input.ScrollEvent
import javafx.scene.layout.StackPane
import org.jzeisel.app_test.viewmodel.TrackListViewModel

object MouseEventBroadcaster {
    private lateinit var root: StackPane
    private lateinit var trackListViewModel: TrackListViewModel
    private lateinit var scene: Scene

    fun initializeBroadcasts(root: StackPane, scene: Scene, trackListViewModel: TrackListViewModel) {
        this.root = root
        this.scene = scene
        this.trackListViewModel = trackListViewModel
        scene.addEventFilter(MouseEvent.MOUSE_PRESSED) {
            trackListViewModel.mouseClicked()
        }
        scene.addEventFilter(KeyEvent.KEY_PRESSED) {
            trackListViewModel.keyPressed(it)
        }
        scene.addEventFilter(KeyEvent.KEY_RELEASED) {
            trackListViewModel.keyReleased(it)
        }
        scene.addEventFilter(ScrollEvent.SCROLL) {
            trackListViewModel.scrollSceneVertically(it.deltaY)
        }
    }
}