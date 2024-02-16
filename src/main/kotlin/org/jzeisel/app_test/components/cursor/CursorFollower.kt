package org.jzeisel.app_test.components.cursor

import javafx.application.Platform
import javafx.scene.layout.StackPane
import javafx.scene.shape.Rectangle
import org.jzeisel.app_test.TrackListViewModel

object CursorFollower {
    /* the cursor follower should appear when the user hovers over the waveform box
       then it follows the mouse until the mouse exits the waveform box.

       The cursor follower should be the height of all the tracks in the scene together */
    private lateinit var trackListViewModel: TrackListViewModel
    var isShowing = false
    private var rectangleWidth = 0.8
    private val rectangleHeight: Double
        get() { return (trackListViewModel.numChildren + 1) * trackListViewModel.trackHeight }

    private val rectangleTranslateY: Double
        get() { return trackListViewModel.masterOffsetY - trackListViewModel.trackHeight/2.0 + rectangleHeight/2.0 }

    private lateinit var cursorRectangle: Rectangle

    fun addMeToScene(root: StackPane, offsetX: Double) {
        /* offset x is distance to the right from trac divider offset */
        isShowing = true
        cursorRectangle = Rectangle(rectangleWidth, rectangleHeight, trackListViewModel.strokeColor.brighter().brighter())
        cursorRectangle.opacity = 0.8
        cursorRectangle.translateY = rectangleTranslateY
        cursorRectangle.translateX = trackListViewModel.currentDividerOffset.getValue() + offsetX
        cursorRectangle.toFront()
        root.children.add(cursorRectangle)
    }

    fun removeMeFromScene(root: StackPane) {
        if (isShowing) {
            Platform.runLater {
                root.children.remove(cursorRectangle)
                isShowing = false
            }
        }
    }

    fun updateLocation(offsetX: Double) {
        if (isShowing) {
            Platform.runLater {
                cursorRectangle.translateX = trackListViewModel.currentDividerOffset.getValue() + offsetX
            }
        }
    }

    fun initialize(trackListViewModel: TrackListViewModel){
        this.trackListViewModel = trackListViewModel
    }
}