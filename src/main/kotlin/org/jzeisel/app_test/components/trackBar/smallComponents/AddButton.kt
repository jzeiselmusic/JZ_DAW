package org.jzeisel.app_test.components.trackBar.smallComponents

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
import org.jzeisel.app_test.components.trackBar.tracks.NormalTrack
import org.jzeisel.app_test.components.trackBar.tracks.Track
import org.jzeisel.app_test.logger.Logger
import org.jzeisel.app_test.util.Observable
import org.jzeisel.app_test.util.ObservableListener
import java.util.*

class AddButton(override val parent: Widget): Widget, TrackComponentWidget, ObservableListener<Double> {
    companion object {
        const val TAG = "AddButton"
        const val LEVEL = 3
    }
    private val parentTrack = parent as Track
    private val trackListViewModel = parentTrack.trackListViewModel
    private val buttonWidth = trackListViewModel.buttonSize
    private val buttonHeight = trackListViewModel.buttonSize
    private val buttonOffsetY = parentTrack.trackOffsetY
    private val buttonOffsetX = -(trackListViewModel.stage.width / 2) + trackListViewModel.addButtonOffset
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
        Logger.debug(TAG, "instantiated: parent is ${parentTrack.name}", LEVEL)
        Logger.debug(TAG, "\t y-offset is $buttonOffsetY", LEVEL)
        buttonRect.translateY = buttonOffsetY
        buttonRect.translateX = buttonOffsetX
        buttonRect.arcWidth = trackListViewModel.arcSize
        buttonRect.arcHeight = trackListViewModel.arcSize
        buttonRect.stroke = trackListViewModel.strokeColor
        buttonRect.strokeWidth = trackListViewModel.strokeSize

        horizontalLine.translateX = buttonOffsetX
        horizontalLine.translateY = buttonOffsetY
        verticalLine.translateX = buttonOffsetX
        verticalLine.translateY = buttonOffsetY
        horizontalLine.strokeWidth = 2.2
        verticalLine.strokeWidth = 2.2

        buttonRect.onMousePressed = mousePressEvent
        buttonRect.onMouseReleased = mouseReleaseEvent
        horizontalLine.onMousePressed = mousePressEvent
        horizontalLine.onMouseReleased = mouseReleaseEvent
        verticalLine.onMousePressed = mousePressEvent
        verticalLine.onMouseReleased = mouseReleaseEvent
    }

    override fun respondToChange(observable: Observable<*>, old: Double, new: Double) {
        when(observable) {
            trackListViewModel.testStageWidth -> respondToWidthChange(old, new)
            trackListViewModel.testStageHeight -> respondToHeightChange(old, new)
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
        Platform.runLater {
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

    override fun registerForBroadcasts() {
        trackListViewModel.registerForWidthChanges(this)
        trackListViewModel.registerForHeightChanges(this)
    }

    override fun unregisterForBroadcasts() {
        trackListViewModel.unregisterForWidthChanges(this)
        trackListViewModel.unregisterForHeightChanges(this)
    }
}