package org.jzeisel.app_test.component.trackBar.smallComponents

import javafx.application.Platform
import javafx.event.EventHandler
import javafx.scene.input.MouseButton
import javafx.scene.input.MouseEvent
import javafx.scene.layout.StackPane
import javafx.scene.paint.Color
import javafx.scene.shape.Line
import javafx.scene.shape.Rectangle
import org.jzeisel.app_test.component.trackBar.tracks.TrackList
import org.jzeisel.app_test.component.Widget

class AddButton(root: StackPane, yLocation: Double, override val parent: Widget?): Widget {
    private val buttonWidth = 20.0
    private val buttonHeight = 20.0
    private val buttonOffsetX = -(((parent!!.parent!!) as TrackList).stage.width / 2) + 30
    override val children = mutableListOf<Widget>()

    override fun addChild(child: Widget) {
        /* AddButton does not have any children */
    }

    private val buttonRect = Rectangle(buttonWidth, buttonHeight, Color.MEDIUMPURPLE.brighter())
    private val horizontalLine = Line(buttonOffsetX - buttonWidth / 4, yLocation,
                                        buttonOffsetX + buttonWidth / 4, yLocation)
    private val verticalLine = Line(buttonOffsetX, yLocation - buttonWidth / 4,
                                        buttonOffsetX, yLocation + buttonWidth / 4)

    private val mousePressEvent = EventHandler<MouseEvent> { mousePress() }
    private val mouseReleaseEvent = EventHandler<MouseEvent> {
                                        if (it.button == MouseButton.SECONDARY) {
                                            mouseReleaseRight()
                                        } else {
                                            mouseReleaseLeft()
                                            }
                                        }

    init {
        buttonRect.translateY = yLocation
        buttonRect.translateX = buttonOffsetX
        buttonRect.arcWidth = 5.0
        buttonRect.arcHeight = 5.0
        buttonRect.stroke = Color.BLACK
        buttonRect.strokeWidth = 1.6

        horizontalLine.translateX = buttonOffsetX
        horizontalLine.translateY = yLocation
        verticalLine.translateX = buttonOffsetX
        verticalLine.translateY = yLocation
        horizontalLine.strokeWidth = 1.6
        verticalLine.strokeWidth = 1.6

        buttonRect.onMousePressed = mousePressEvent
        buttonRect.onMouseReleased = mouseReleaseEvent
        horizontalLine.onMousePressed = mousePressEvent
        horizontalLine.onMouseReleased = mouseReleaseEvent
        verticalLine.onMousePressed = mousePressEvent
        verticalLine.onMouseReleased = mouseReleaseEvent

        (parent!!.parent as TrackList).stageWidthProperty
                    .addListener{_, old, new, -> updatePositionOfX(old as Double, new as Double)}
        (parent.parent as TrackList).stageHeightProperty
                    .addListener{_, old, new, -> updatePositionOfY(old as Double, new as Double)}
    }

    private fun updatePositionOfX(old: Double, new: Double) {
        buttonRect.translateX -= (new - old)/2.0
        horizontalLine.translateX -= (new - old)/ 2.0
        verticalLine.translateX -= (new - old)/2.0
    }

    private fun updatePositionOfY(old: Double, new: Double) {
        buttonRect.translateY -= (new - old)/2.0
        horizontalLine.translateY -= (new - old)/2.0
        verticalLine.translateY -= (new - old)/2.0
    }

    private fun addTrack() {
        (parent!!.parent as TrackList).addTrack(parent)
    }

    private fun removeTrack() {
        (parent!!.parent as TrackList).removeTrack(parent)
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
        root.children.add(buttonRect)
        root.children.add(horizontalLine)
        root.children.add(verticalLine)
    }

    override fun removeMeFromScene(root: StackPane) {
        Platform.runLater {
            root.children.remove(buttonRect)
            root.children.remove(horizontalLine)
            root.children.remove(verticalLine)
        }
    }
}