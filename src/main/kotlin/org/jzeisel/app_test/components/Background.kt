package org.jzeisel.app_test.components

import javafx.event.EventHandler
import javafx.scene.input.MouseEvent
import javafx.scene.layout.StackPane
import javafx.scene.paint.Color
import javafx.scene.shape.Rectangle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jzeisel.app_test.components.interfaces.WindowElement
import org.jzeisel.app_test.components.interfaces.widget.SingularWidget
import org.jzeisel.app_test.components.interfaces.widget.Widget
import org.jzeisel.app_test.util.BroadcastType
import org.jzeisel.app_test.util.viewOrderFlip
import org.jzeisel.app_test.viewmodel.TrackListViewModel

class Background(parent: Widget): SingularWidget, WindowElement {
    val trackListViewModel = parent as TrackListViewModel
    val trackListState = trackListViewModel._trackListStateFlow.state

    val rect = Rectangle()

    var numPresses: Int = 0
    var timerRunning = false

    override fun respondToHeightChange(old: Double, new: Double) {
        rect.height = trackListState.observableStageHeight.getValue()
    }

    override fun respondToWidthChange(old: Double, new: Double) {
        rect.width = trackListState.observableStageWidth.getValue()
    }

    override fun respondToChange(broadcastType: BroadcastType, old: Double, new: Double) {
        when(broadcastType) {
            BroadcastType.STAGE_WIDTH -> { respondToWidthChange(old, new) }
            BroadcastType.STAGE_HEIGHT -> { respondToHeightChange(old, new) }
            else -> {}
        }
    }

    override fun registerForBroadcasts() {
        trackListViewModel.registerForHeightChanges(this)
        trackListViewModel.registerForWidthChanges(this)
    }

    override fun unregisterForBroadcasts() {
        trackListViewModel.unregisterForWidthChanges(this)
        trackListViewModel.unregisterForHeightChanges(this)
    }

    override fun addMeToScene(root: StackPane) {
        registerForBroadcasts()
        rect.width = trackListState.observableStageWidth.getValue()
        rect.height = trackListState.observableStageHeight.getValue()
        rect.isMouseTransparent = false
        rect.fill = Color.LIGHTGRAY
        rect.opacity = 0.0
        rect.viewOrder = viewOrderFlip - 0.001
        rect.onMousePressed = mousePressEvent
        rect.onMouseReleased = mouseReleaseEvent
        root.children.add(rect)
    }

    override fun removeMeFromScene(root: StackPane) {
        unregisterForBroadcasts()
        root.children.remove(rect)
    }

    private fun doubleClick() {
        trackListViewModel.addTrackFromDoubleClick()
    }

    private fun launchTimer() {
        CoroutineScope(Dispatchers.Default).launch {
            timerRunning = true
            delay(400)
            timerRunning = false
            numPresses = 0
        }
    }

    private val mouseReleaseEvent = EventHandler<MouseEvent> {
        rect.opacity = 0.0
    }

    private val mousePressEvent = EventHandler<MouseEvent> {
        rect.opacity = 0.01
        when (numPresses) {
            0 -> {
                numPresses++
                launchTimer()
                return@EventHandler
            }
            1 -> {
                if (timerRunning) {
                    doubleClick()
                }
                numPresses = 0
                return@EventHandler
            }
            else -> numPresses = 0
        }
    }
}