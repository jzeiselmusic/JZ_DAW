package org.jzeisel.app_test.components.trackComponents

import javafx.event.EventHandler
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.input.MouseEvent
import javafx.scene.layout.StackPane
import javafx.scene.paint.Color
import javafx.scene.shape.Rectangle
import javafx.scene.text.Font
import javafx.scene.text.FontWeight
import javafx.scene.text.Text
import org.jzeisel.app_test.error.AudioError
import org.jzeisel.app_test.components.interfaces.TrackElement
import org.jzeisel.app_test.components.interfaces.widget.Widget
import org.jzeisel.app_test.components.NormalTrack
import org.jzeisel.app_test.components.Track
import org.jzeisel.app_test.components.interfaces.WindowElement
import org.jzeisel.app_test.components.interfaces.widget.NodeWidget
import org.jzeisel.app_test.util.*

class InputEnableButton(override val parent: Widget?)
    : NodeWidget, TrackElement, WindowElement {

    private val parentTrack = parent as Track
    private val trackListViewModel = parentTrack.trackListViewModel
    private val trackListState = trackListViewModel._trackListStateFlow.state
    var isEnabled: Boolean = false
        set(new) {
            field = new
            if (new) {
                runLater {
                    buttonRect.fill = Color.rgb(255, 0, 0)
                    buttonRect.opacity = 0.9
                    iLetter.stroke = trackListState.generalGray
                }
            }
            else {
                runLater {
                    buttonRect.fill = Color.TRANSPARENT
                    buttonRect.opacity = 1.0
                    iLetter.stroke = Color.BLACK
                }
            }
        }
    private val buttonWidth = trackListState.buttonSize
    private val buttonHeight = trackListState.buttonSize
    private var buttonOffsetY = parentTrack.trackOffsetY - trackListState.verticalDistancesBetweenWidgets
    private val buttonOffsetX = -(trackListViewModel.stage.width / 2) + trackListState.inputButtonsOffset
    override val children = mutableListOf<Widget>()

    override fun addChild(child: Widget) {
        /* AddButton does not have any children */
    }

    private val buttonRect = Rectangle(buttonWidth, buttonHeight, Color.TRANSPARENT)
    private val iLetter = Text()

    private val mousePressEvent = EventHandler<MouseEvent> {
        mousePress()
    }

    private val mouseReleaseEvent = EventHandler<MouseEvent> {
        mouseReleaseLeft()
    }

    init {
        iLetter.text = "I"
        iLetter.font = Font.font("Times New Roman", FontWeight.THIN, 12.0)
        iLetter.stroke = trackListState.strokeColor
        iLetter.strokeWidth = trackListState.strokeSize
        iLetter.translateY = buttonOffsetY
        iLetter.translateX = buttonOffsetX
        iLetter.isMouseTransparent = true
        iLetter.viewOrder = viewOrderFlip - 0.32
        buttonRect.translateY = buttonOffsetY
        buttonRect.translateX = buttonOffsetX
        buttonRect.arcWidth = trackListState.arcSize
        buttonRect.arcHeight = trackListState.arcSize
        buttonRect.stroke = trackListState.strokeColor
        buttonRect.strokeWidth = trackListState.strokeSize
        buttonRect.viewOrder = viewOrderFlip - 0.31

        buttonRect.onMouseReleased = mouseReleaseEvent
        buttonRect.onMousePressed = mousePressEvent
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

    private fun mousePress() {
        animateObjectScale(1.0, 0.9, buttonRect, 20.0)
    }

    private fun mouseReleaseLeft() {
        animateObjectScale(0.9, 1.0, buttonRect, 25.0)
        when (isEnabled) {
            true -> (parentTrack as NormalTrack).audioInputDisable()
            false -> (parentTrack as NormalTrack).audioInputEnable()
        }
    }

    override fun addMeToScene(root: StackPane) {
        registerForBroadcasts()
        root.children.add(buttonRect)
        root.children.add(iLetter)
    }

    override fun removeMeFromScene(root: StackPane) {
        runLater {
            unregisterForBroadcasts()
            root.children.remove(buttonRect)
            root.children.remove(iLetter)
        }
    }

    override fun respondToHeightChange(old: Double, new: Double) {
        ((new - old)/2.0).let {
            buttonRect.translateY -= it
            iLetter.translateY -= it
        }
    }

    override fun respondToWidthChange(old: Double, new: Double) {
        ((new - old)/2.0).let {
            buttonRect.translateX -= it
            iLetter.translateX -= it
        }
    }

    override fun respondToIndexChange(old: Double, new: Double) {
        buttonOffsetY = parentTrack.trackOffsetY - trackListState.verticalDistancesBetweenWidgets
        iLetter.translateY = buttonOffsetY
        buttonRect.translateY = buttonOffsetY
    }

}