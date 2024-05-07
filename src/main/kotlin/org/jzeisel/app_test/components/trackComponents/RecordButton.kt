package org.jzeisel.app_test.components.trackComponents

import javafx.event.EventHandler
import javafx.scene.input.MouseEvent
import javafx.scene.layout.StackPane
import javafx.scene.paint.Color
import javafx.scene.shape.Circle
import org.jzeisel.app_test.components.NormalTrack
import org.jzeisel.app_test.components.Track
import org.jzeisel.app_test.components.interfaces.TrackElement
import org.jzeisel.app_test.components.interfaces.WindowElement
import org.jzeisel.app_test.components.interfaces.widget.NodeWidget
import org.jzeisel.app_test.components.interfaces.widget.Widget
import org.jzeisel.app_test.util.BroadcastType
import org.jzeisel.app_test.util.animateObjectScale
import org.jzeisel.app_test.util.runLater
import org.jzeisel.app_test.util.viewOrderFlip

class RecordButton(override val parent: Widget)
    : NodeWidget, TrackElement, WindowElement {

    private val parentTrack = parent as Track
    private val trackListViewModel = parentTrack.trackListViewModel
    private val trackListState = trackListViewModel._trackListStateFlow.state
    private val buttonWidth = trackListState.recordButtonWidth
    private var buttonOffsetY = parentTrack.trackOffsetY
    private val buttonOffsetX = -(trackListViewModel.stage.width / 2) + trackListState.recordButtonOffset

    override val children: MutableList<Widget> = mutableListOf()

    /* create 3 concentric circles */
    private val buttonOutside = Circle(buttonWidth, Color.rgb(128, 15, 15))
    private val buttonMiddle = Circle(buttonWidth - 2.2, Color.WHITESMOKE.darker().darker())
    private val buttonInside = Circle(buttonWidth - 5.5, Color.rgb(108, 10, 10))

    private val mousePressEvent = EventHandler<MouseEvent> {
        mousePress()
    }

    private val mouseReleaseEvent = EventHandler<MouseEvent> {
        mouseReleaseLeft()
    }

    var enabled: Boolean = false
        set(value) {
            if (value) {
                buttonOutside.fill = Color.rgb(210,0,0)
                buttonInside.fill = Color.rgb(210,0,0)
                buttonMiddle.fill = Color.WHITESMOKE.darker()
            }
            else {
                buttonOutside.fill = Color.rgb(128, 15, 15)
                buttonInside.fill = Color.rgb(108, 10, 10)
                buttonMiddle.fill = Color.WHITESMOKE.darker().darker()
            }
            field = value
        }

    init {
        buttonOutside.translateX = buttonOffsetX
        buttonOutside.translateY = buttonOffsetY
        buttonOutside.viewOrder = viewOrderFlip - 0.31

        buttonMiddle.translateX = buttonOffsetX
        buttonMiddle.translateY = buttonOffsetY
        buttonMiddle.viewOrder = viewOrderFlip - 0.32

        buttonInside.translateX = buttonOffsetX
        buttonInside.translateY = buttonOffsetY
        buttonInside.viewOrder = viewOrderFlip - 0.33

        buttonMiddle.isMouseTransparent = true
        buttonInside.isMouseTransparent = true

        buttonOutside.onMousePressed = mousePressEvent
        buttonOutside.onMouseReleased = mouseReleaseEvent
    }
    override fun addChild(child: Widget) { }
    override fun addMeToScene(root: StackPane) {
        registerForBroadcasts()
        root.children.add(buttonInside)
        root.children.add(buttonMiddle)
        root.children.add(buttonOutside)
    }

    override fun removeMeFromScene(root: StackPane) {
        unregisterForBroadcasts()
        runLater {
            root.children.remove(buttonOutside)
            root.children.remove(buttonMiddle)
            root.children.remove(buttonInside)
        }
    }

    override fun respondToIndexChange(old: Double, new: Double) {
        buttonOffsetY = parentTrack.trackOffsetY
        buttonOutside.translateY = buttonOffsetY
        buttonMiddle.translateY = buttonOffsetY
        buttonInside.translateY = buttonOffsetY
    }

    override fun respondToHeightChange(old: Double, new: Double) {
        ((new - old)/2.0).let {
            buttonOutside.translateY -= it
            buttonMiddle.translateY -= it
            buttonInside.translateY -= it
        }
    }

    override fun respondToWidthChange(old: Double, new: Double) {
        ((new - old)/2.0).let {
            buttonOutside.translateX -= it
            buttonMiddle.translateX -= it
            buttonInside.translateX -= it
        }
    }

    override fun respondToChange(broadcastType: BroadcastType, old: Double, new: Double) {
        when(broadcastType) {
            BroadcastType.STAGE_WIDTH -> respondToWidthChange(old, new)
            BroadcastType.STAGE_HEIGHT -> respondToHeightChange(old, new)
            BroadcastType.INDEX -> respondToIndexChange(old, new)
            BroadcastType.DIVIDER -> {}
            BroadcastType.SCROLL -> {}
        }
    }

    override fun registerForBroadcasts() {
        trackListViewModel.registerForWidthChanges(this)
        trackListViewModel.registerForHeightChanges(this)
        if (parentTrack is NormalTrack) {
            parentTrack.registerForIndexChanges(this)
        }
    }

    override fun unregisterForBroadcasts() {
        trackListViewModel.unregisterForWidthChanges(this)
        trackListViewModel.unregisterForHeightChanges(this)
        if (parentTrack is NormalTrack) {
            parentTrack.unregisterForIndexChanges(this)
        }
    }

    private fun mousePress() {
        animateObjectScale(1.0, 0.85, buttonOutside, 80.0)
        animateObjectScale(1.0, 0.85, buttonMiddle, 80.0)
        animateObjectScale(1.0, 0.7, buttonInside, 80.0)
    }

    private fun mouseReleaseLeft() {
        animateObjectScale(0.7, 1.0, buttonInside, 80.0)
        animateObjectScale(0.85, 1.0, buttonMiddle, 80.0)
        animateObjectScale(0.85, 1.0, buttonOutside, 80.0)
        if (enabled)
            (parentTrack as NormalTrack).disarmRecording()
        else
            (parentTrack as NormalTrack).armRecording()
    }
}