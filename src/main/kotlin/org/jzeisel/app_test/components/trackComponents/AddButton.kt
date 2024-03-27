package org.jzeisel.app_test.components.trackComponents

import javafx.application.Platform
import javafx.event.EventHandler
import javafx.scene.input.MouseButton
import javafx.scene.input.MouseEvent
import javafx.scene.layout.StackPane
import javafx.scene.paint.Color
import javafx.scene.shape.Line
import javafx.scene.shape.Rectangle
import org.jzeisel.app_test.components.TrackComponentWidget
import org.jzeisel.app_test.components.Widget
import org.jzeisel.app_test.components.NormalTrack
import org.jzeisel.app_test.components.Track
import org.jzeisel.app_test.util.*
import java.util.*

class AddButton(override val parent: Widget)
    : Widget, TrackComponentWidget, ObservableListener<Double> {

    private val parentTrack = parent as Track
    private val trackListViewModel = parentTrack.trackListViewModel
    private val trackListState = trackListViewModel._trackListStateFlow.state
    private val buttonWidth = trackListState.buttonSize
    private val buttonHeight = trackListState.buttonSize
    private var buttonOffsetY = parentTrack.trackOffsetY
    private val buttonOffsetX = -(trackListViewModel.stage.width / 2) + trackListState.addButtonOffset
    override val children = mutableListOf<Widget>()

    override fun addChild(child: Widget) {
    }

    private fun getRandomColor(): Color {
        val random = Random()
        val red = random.nextInt(256)
        val green = random.nextInt(256)
        val blue = random.nextInt(256)

        return Color.rgb(red, green, blue)
    }

    private val buttonRect = Rectangle(buttonWidth, buttonHeight, getRandomColor())
    private val horizontalLine = Line(buttonOffsetX - buttonWidth / 4, buttonOffsetY,
                                        buttonOffsetX + buttonWidth / 4, buttonOffsetY)
    private val verticalLine = Line(buttonOffsetX, buttonOffsetY - buttonWidth / 4,
                                        buttonOffsetX, buttonOffsetY + buttonWidth / 4)

    private val mousePressEvent = EventHandler<MouseEvent> { mousePress() }
    private val mouseReleaseEvent = EventHandler<MouseEvent> {
                                        if (it.button == MouseButton.SECONDARY) {
                                            mouseReleaseRight()
                                        } else {
                                            mouseReleaseLeft()
                                            }
                                        }

    init {
        buttonRect.translateY = buttonOffsetY
        buttonRect.translateX = buttonOffsetX
        buttonRect.arcWidth = trackListState.arcSize
        buttonRect.arcHeight = trackListState.arcSize
        buttonRect.stroke = trackListState.strokeColor
        buttonRect.strokeWidth = trackListState.strokeSize
        buttonRect.viewOrder = viewOrderFlip - 0.31
        horizontalLine.translateX = buttonOffsetX
        horizontalLine.translateY = buttonOffsetY
        verticalLine.translateX = buttonOffsetX
        verticalLine.translateY = buttonOffsetY
        horizontalLine.strokeWidth = 2.2
        verticalLine.strokeWidth = 2.2
        verticalLine.viewOrder = viewOrderFlip - 0.32
        horizontalLine.viewOrder = viewOrderFlip - 0.32
        buttonRect.onMousePressed = mousePressEvent
        buttonRect.onMouseReleased = mouseReleaseEvent
        horizontalLine.onMousePressed = mousePressEvent
        horizontalLine.onMouseReleased = mouseReleaseEvent
        verticalLine.onMousePressed = mousePressEvent
        verticalLine.onMouseReleased = mouseReleaseEvent
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

    private fun addTrack() {
        parentTrack.addTrack()
    }

    private fun removeTrack() {
        if (parent is NormalTrack)
        trackListViewModel.removeTrack(parent)
    }

    private fun mousePress() {
        buttonRect.opacity = 0.6
        horizontalLine.opacity = 0.4
        verticalLine.opacity = 0.4
    }

    private fun mouseReleaseLeft() {
        buttonRect.opacity = 1.0
        horizontalLine.opacity = 1.0
        verticalLine.opacity = 1.0
        addTrack()
    }

    private fun mouseReleaseRight() {
        buttonRect.opacity = 1.0
        horizontalLine.opacity = 1.0
        verticalLine.opacity = 1.0
        removeTrack()
    }

    override fun addMeToScene(root: StackPane) {
        registerForBroadcasts()
        root.children.add(buttonRect)
        root.children.add(horizontalLine)
        root.children.add(verticalLine)
    }

    override fun removeMeFromScene(root: StackPane) {
        runLater {
            unregisterForBroadcasts()
            root.children.remove(buttonRect)
            root.children.remove(horizontalLine)
            root.children.remove(verticalLine)
        }
    }

    override fun respondToHeightChange(old: Double, new: Double) {
        ((new - old)/2.0).let {
            buttonRect.translateY -= it
            horizontalLine.translateY -= it
            verticalLine.translateY -= it
        }
    }

    override fun respondToWidthChange(old: Double, new: Double) {
        ((new - old)/2.0).let {
            buttonRect.translateX -= it
            horizontalLine.translateX -= it
            verticalLine.translateX -= it
        }
    }

    override fun respondToIndexChange(old: Double, new: Double) {
        if (parentTrack is NormalTrack) {
            buttonOffsetY = parentTrack.trackOffsetY
            buttonRect.translateY = buttonOffsetY
            verticalLine.translateY = buttonOffsetY
            horizontalLine.translateY = buttonOffsetY
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