package org.jzeisel.app_test.components.cursor

import javafx.application.Platform
import javafx.scene.layout.StackPane
import javafx.scene.paint.Color
import javafx.scene.shape.Polygon
import javafx.scene.shape.Rectangle
import org.jzeisel.app_test.TrackListViewModel
import org.jzeisel.app_test.components.TrackComponentWidget
import org.jzeisel.app_test.logger.Logger
import org.jzeisel.app_test.util.ObservableListener

object CursorFollower: TrackComponentWidget, ObservableListener<Double> {
    const val TAG = "CursorFollower"
    /* the cursor follower should appear when the user hovers over the waveform box
       then it follows the mouse until the mouse exits the waveform box.

       The cursor follower should be the height of all the tracks in the scene together */
    private lateinit var trackListViewModel: TrackListViewModel
    var isShowing = false
    private var rectangleWidth = 1.8
    private val rectangleHeight: Double
        get() { return (trackListViewModel.numChildren + 1) * trackListViewModel.trackHeight }

    private val rectangleTranslateY: Double
        get() { return trackListViewModel.masterOffsetY - trackListViewModel.trackHeight/2.0 + rectangleHeight/2.0 }

    private lateinit var cursorRectangle: Rectangle
    private lateinit var cursorPointer: Polygon
    private var currentOffsetX = 0.0

    fun addMeToScene(root: StackPane, offsetX: Double) {
        /* offset x is distance to the right from trac divider offset */
        isShowing = true
        currentOffsetX = offsetX
        cursorRectangle = Rectangle(rectangleWidth, rectangleHeight, trackListViewModel.generalGray)
        cursorRectangle.opacity = 0.9
        cursorRectangle.translateY = rectangleTranslateY
        cursorRectangle.translateX = trackListViewModel.currentDividerOffset.getValue() + offsetX
        cursorRectangle.toFront()
        root.children.add(cursorRectangle)

        cursorPointer = Polygon(0.0, 0.0,
                                12.0, 0.0,
                                6.0, -8.0)
        cursorPointer.fill = Color.LIGHTGOLDENRODYELLOW
        cursorPointer.stroke = trackListViewModel.strokeColor
        cursorPointer.strokeWidth = 1.5
        cursorPointer.translateY =  cursorRectangle.translateY -
                                    cursorRectangle.height/2.0 +
                                    cursorPointer.layoutBounds.height/2.0 - 1.0
        cursorPointer.translateX = cursorRectangle.translateX
        cursorPointer.rotate = 180.0
        cursorPointer.toFront()
        root.children.add(cursorPointer)
    }

    fun removeMeFromScene(root: StackPane) {
        if (isShowing) {
            Platform.runLater {
                root.children.remove(cursorRectangle)
                root.children.remove(cursorPointer)
                isShowing = false
            }
        }
    }

    fun updateLocation(offsetX: Double) {
        if (isShowing) {
            Platform.runLater {
                currentOffsetX = if (offsetX < 0.0) 0.0 else offsetX
                cursorRectangle.translateX = trackListViewModel.currentDividerOffset.getValue() + currentOffsetX
                cursorPointer.translateX = cursorRectangle.translateX
            }
        }
    }

    fun updateFromTrackList(root: StackPane) {
        Platform.runLater {
            cursorRectangle.height = rectangleHeight
            cursorRectangle.translateY = rectangleTranslateY
            cursorRectangle.translateX = trackListViewModel.currentDividerOffset.getValue() + currentOffsetX
            cursorRectangle.toFront()
            cursorPointer.translateX = cursorRectangle.translateX
            cursorPointer.toFront()
        }
    }

    fun initialize(trackListViewModel: TrackListViewModel){
        this.trackListViewModel = trackListViewModel
    }

    override fun respondToOffsetYChange(old: Double, new: Double) {}
    fun respondToHeightChange(old: Double, new: Double) {
        if (isShowing) {
            val change = (new - old) / 2.0
            cursorRectangle.translateY -= change
            cursorPointer.translateY -= change
        }
    }

    override fun respondToWidthChange(old: Double, new: Double) {
        if (isShowing) {
            val change = (new - old) / 2.0
            cursorRectangle.translateX -= change
            cursorPointer.translateX -= change
        }
    }

    override fun respondToChange(observable: Any, value: Double) {
        if (isShowing) {
            when (observable) {
                trackListViewModel.currentDividerOffset -> {
                    cursorRectangle.translateX = trackListViewModel.currentDividerOffset.getValue() + currentOffsetX
                    cursorPointer.translateX = cursorRectangle.translateX
                }
            }
        }
    }
}