package org.jzeisel.app_test.components.trackComponents

import javafx.event.EventHandler
import javafx.scene.input.MouseEvent
import javafx.scene.layout.StackPane
import javafx.scene.paint.Color
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

class MuteButton(override val parent: Widget)
    : NodeWidget, TrackElement, WindowElement, PressableButton {

    private val parentTrack = parent as Track
    private val trackListViewModel = parentTrack.trackListViewModel
    private val trackListState = trackListViewModel._trackListStateFlow.state
    private val buttonWidth = trackListState.buttonSize
    private val buttonHeight = trackListState.buttonSize
    private var buttonOffsetY = parentTrack.trackOffsetY + trackListState.verticalDistancesBetweenWidgets
    private val buttonOffsetX = -(trackListViewModel.stage.width / 2) + trackListState.muteButtonOffset
    override val children: MutableList<Widget> = mutableListOf()

    private val buttonRect = Rectangle(buttonWidth, buttonHeight, Color.TRANSPARENT)
    private val mLetter = Text()

    var isEnabled = false
        set(value) {
            if (!value) {
                buttonRect.fill = Color.TRANSPARENT
                mLetter.stroke = Color.BLACK
            }
            else {
                buttonRect.fill = Color.GRAY.darker().darker()
                mLetter.stroke = trackListState.generalGray
            }
            field = value
        }

    override val mousePressEvent = EventHandler<MouseEvent> {
        animateObjectScale(1.0, 0.9, buttonRect, 100.0)
    }

    override val mouseReleaseEvent = EventHandler<MouseEvent> {
        animateObjectScale(0.9, 1.0, buttonRect, 80.0)
        if (isEnabled) {
            parentTrack.muteDisable()
        }
        else {
            parentTrack.muteEnable()
        }
    }
    init {
        mLetter.text = "M"
        mLetter.font = Font.font("Montserrat", FontWeight.THIN, 11.0)
        mLetter.stroke = trackListState.strokeColor
        mLetter.strokeWidth = trackListState.strokeSize
        mLetter.translateY = buttonOffsetY
        mLetter.translateX = buttonOffsetX
        mLetter.isMouseTransparent = true
        mLetter.viewOrder = viewOrderFlip - 0.32
        buttonRect.translateY = buttonOffsetY
        buttonRect.translateX = buttonOffsetX
        buttonRect.arcWidth = trackListState.arcSize
        buttonRect.arcHeight = trackListState.arcSize
        buttonRect.stroke = trackListState.strokeColor
        buttonRect.strokeWidth = trackListState.strokeSize
        buttonRect.viewOrder = viewOrderFlip - 0.31
        buttonRect.opacity = 0.75
        buttonRect.onMousePressed = mousePressEvent
        buttonRect.onMouseReleased = mouseReleaseEvent
    }

    override fun addChild(child: Widget) {
        /* has no children */
    }

    override fun addMeToScene(root: StackPane) {
        registerForBroadcasts()
        root.children.add(buttonRect)
        root.children.add(mLetter)
    }

    override fun removeMeFromScene(root: StackPane) {
        unregisterForBroadcasts()
        root.children.remove(buttonRect)
        root.children.remove(mLetter)
    }

    override fun respondToIndexChange(old: Double, new: Double) {
        buttonOffsetY = parentTrack.trackOffsetY + trackListState.verticalDistancesBetweenWidgets
        mLetter.translateY = buttonOffsetY
        buttonRect.translateY = buttonOffsetY
    }

    override fun respondToHeightChange(old: Double, new: Double) {
        ((new - old)/2.0).let {
            buttonRect.translateY -= it
            mLetter.translateY -= it
        }
    }

    override fun respondToWidthChange(old: Double, new: Double) {
        ((new - old)/2.0).let {
            buttonRect.translateX -= it
            mLetter.translateX -= it
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