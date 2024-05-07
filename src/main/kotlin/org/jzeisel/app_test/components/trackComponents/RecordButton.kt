package org.jzeisel.app_test.components.trackComponents

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
    private val buttonMiddle = Circle(buttonWidth - 2.0, Color.rgb(156, 156, 156))
    private val buttonInside = Circle(buttonWidth - 5.5, Color.rgb(128, 15, 15))
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
}