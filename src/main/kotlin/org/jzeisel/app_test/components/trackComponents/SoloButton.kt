package org.jzeisel.app_test.components.trackComponents

import javafx.event.EventHandler
import javafx.scene.input.MouseEvent
import javafx.scene.layout.StackPane
import javafx.scene.paint.Color
import javafx.scene.paint.Color.rgb
import javafx.scene.shape.Rectangle
import javafx.scene.text.Font
import javafx.scene.text.FontWeight
import javafx.scene.text.Text
import org.jzeisel.app_test.components.NormalTrack
import org.jzeisel.app_test.components.Track
import org.jzeisel.app_test.components.interfaces.PressableButton
import org.jzeisel.app_test.components.interfaces.TrackElement
import org.jzeisel.app_test.components.interfaces.WindowElement
import org.jzeisel.app_test.components.interfaces.widget.NodeWidget
import org.jzeisel.app_test.components.interfaces.widget.Widget
import org.jzeisel.app_test.util.BroadcastType
import org.jzeisel.app_test.util.animateObjectScale
import org.jzeisel.app_test.util.viewOrderFlip

class SoloButton(override val parent: Widget)
    : NodeWidget, TrackElement, WindowElement, PressableButton {

    private val parentTrack = parent as Track
    private val trackListViewModel = parentTrack.trackListViewModel
    private val trackListState = trackListViewModel._trackListStateFlow.state
    private val buttonWidth = trackListState.buttonSize
    private val buttonHeight = trackListState.buttonSize
    private var buttonOffsetY = parentTrack.trackOffsetY - trackListState.verticalDistancesBetweenWidgets
    private val buttonOffsetX = -(trackListViewModel.stage.width / 2) + trackListState.soloButtonOffset
    override val children: MutableList<Widget> = mutableListOf()

    private val buttonRect = Rectangle(buttonWidth, buttonHeight, Color.TRANSPARENT)
    private val sLetter = Text()

    private var isEnabled = false

    override val mousePressEvent = EventHandler<MouseEvent> {
        animateObjectScale(1.0, 0.9, buttonRect, 100.0)
    }

    override val mouseReleaseEvent = EventHandler<MouseEvent> {
        animateObjectScale(0.9, 1.0, buttonRect, 80.0)
        if (isEnabled) {
            parentTrack.soloDisable()
            buttonRect.fill = Color.TRANSPARENT
            isEnabled = false
        }
        else {
            parentTrack.soloEnable()
            buttonRect.fill = rgb(255, 255, 0)
            isEnabled = true
        }
    }
    init {
        sLetter.text = "S"
        sLetter.font = Font.font("Product Sans", FontWeight.THIN, 11.0)
        sLetter.stroke = trackListState.strokeColor
        sLetter.strokeWidth = trackListState.strokeSize
        sLetter.translateY = buttonOffsetY
        sLetter.translateX = buttonOffsetX
        sLetter.isMouseTransparent = true
        sLetter.viewOrder = viewOrderFlip - 0.32
        buttonRect.translateY = buttonOffsetY
        buttonRect.translateX = buttonOffsetX
        buttonRect.arcWidth = trackListState.arcSize
        buttonRect.arcHeight = trackListState.arcSize
        buttonRect.stroke = trackListState.strokeColor
        buttonRect.strokeWidth = trackListState.strokeSize
        buttonRect.viewOrder = viewOrderFlip - 0.31
        buttonRect.onMousePressed = mousePressEvent
        buttonRect.onMouseReleased = mouseReleaseEvent
        buttonRect.opacity = 0.75
    }

    override fun addChild(child: Widget) {
        /* has no children */
    }

    override fun addMeToScene(root: StackPane) {
        registerForBroadcasts()
        root.children.add(buttonRect)
        root.children.add(sLetter)
    }

    override fun removeMeFromScene(root: StackPane) {
        unregisterForBroadcasts()
        root.children.remove(buttonRect)
        root.children.remove(sLetter)
    }

    override fun respondToIndexChange(old: Double, new: Double) {
        buttonOffsetY = parentTrack.trackOffsetY - trackListState.verticalDistancesBetweenWidgets
        sLetter.translateY = buttonOffsetY
        buttonRect.translateY = buttonOffsetY
    }

    override fun respondToHeightChange(old: Double, new: Double) {
        ((new - old)/2.0).let {
            buttonRect.translateY -= it
            sLetter.translateY -= it
        }
    }

    override fun respondToWidthChange(old: Double, new: Double) {
        ((new - old)/2.0).let {
            buttonRect.translateX -= it
            sLetter.translateX -= it
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
        trackListViewModel.unregisterForHeightChanges(this)
        trackListViewModel.unregisterForWidthChanges(this)
        if (parentTrack is NormalTrack) {
            parentTrack.unregisterForIndexChanges(this)
        }
    }
}