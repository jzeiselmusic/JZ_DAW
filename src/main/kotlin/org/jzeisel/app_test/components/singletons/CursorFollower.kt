package org.jzeisel.app_test.components.singletons

import javafx.application.Platform
import javafx.scene.layout.StackPane
import javafx.scene.paint.Color
import javafx.scene.shape.Polygon
import javafx.scene.shape.Rectangle
import org.jzeisel.app_test.TrackListViewModel
import org.jzeisel.app_test.components.TrackComponentWidget
import org.jzeisel.app_test.util.BroadcastType
import org.jzeisel.app_test.util.ObservableListener
import org.jzeisel.app_test.util.viewOrderFlip

object CursorFollower: TrackComponentWidget, ObservableListener<Double> {

    private lateinit var trackListViewModel: TrackListViewModel
    var isShowing = false
    private var rectangleWidth = 1.8
    private const val zValCursor = viewOrderFlip - 0.13
    private const val zValCursorTriangle = viewOrderFlip - 0.14
    private val rectangleHeight: Double
        get() { return trackListViewModel.numTracks * trackListViewModel.trackHeight }

    private val rectangleTranslateY: Double
        get() { return trackListViewModel.masterOffsetY - trackListViewModel.trackHeight/2.0 + rectangleHeight /2.0 }

    private lateinit var cursorRectangle: Rectangle
    private lateinit var cursorPointer: Polygon
    private var currentOffsetX = 0.0

    fun addMeToScene(root: StackPane, offsetX: Double) {
        /* offset x is distance to the right from track divider offset */
        registerForBroadcasts()
        isShowing = true
        currentOffsetX = offsetX
        cursorRectangle = Rectangle(rectangleWidth, rectangleHeight, trackListViewModel.generalGray)
        cursorRectangle.opacity = 0.9
        cursorRectangle.translateY = rectangleTranslateY
        cursorRectangle.translateX = trackListViewModel.currentDividerOffset.getValue() + offsetX
        cursorRectangle.viewOrder = zValCursor
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
        cursorPointer.viewOrder = zValCursorTriangle
        root.children.add(cursorPointer)
    }

    fun removeMeFromScene(root: StackPane) {
        if (isShowing) {
            Platform.runLater {
                unregisterForBroadcasts()
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

    override fun respondToChange(broadcastType: BroadcastType, old: Double, new: Double) {
        if (isShowing) {
            when (broadcastType) {
                BroadcastType.DIVIDER -> {
                    cursorRectangle.translateX = trackListViewModel.currentDividerOffset.getValue() + currentOffsetX
                    cursorPointer.translateX = cursorRectangle.translateX
                }
                BroadcastType.STAGE_WIDTH -> { respondToWidthChange(old, new) }
                BroadcastType.STAGE_HEIGHT -> { respondToHeightChange(old, new) }
                BroadcastType.INDEX -> {}
                BroadcastType.SCROLL -> { respondToScrollChange(new) }
            }
        }
    }

    override fun registerForBroadcasts() {
        trackListViewModel.registerForDividerOffsetChanges(this)
        trackListViewModel.registerForWidthChanges(this)
        trackListViewModel.registerForHeightChanges(this)
        trackListViewModel.registerForScrollChanges(this)
    }

    override fun unregisterForBroadcasts() {
        trackListViewModel.unregisterForWidthChanges(this)
        trackListViewModel.unregisterForHeightChanges(this)
        trackListViewModel.unregisterForDividerOffsetChanges(this)
        trackListViewModel.unregisterForScrollChanges(this)
    }

    fun updateFromTrackList(root: StackPane) {
        if (isShowing) {
            Platform.runLater {
                cursorRectangle.height = rectangleHeight
                cursorRectangle.translateY = rectangleTranslateY
                cursorRectangle.translateX = trackListViewModel.currentDividerOffset.getValue() + currentOffsetX
                cursorPointer.translateX = cursorRectangle.translateX
            }
        }
    }

    fun initialize(trackListViewModel: TrackListViewModel){
        CursorFollower.trackListViewModel = trackListViewModel
    }

    override fun respondToHeightChange(old: Double, new: Double) {
        if (isShowing) {
            ((new - old) / 2.0).let {
                cursorRectangle.translateY -= it
                cursorPointer.translateY -= it
            }
        }
    }

    override fun respondToWidthChange(old: Double, new: Double) {
        if (isShowing) {
            ((new - old) / 2.0).let {
                cursorRectangle.translateX -= it
                cursorPointer.translateX -= it
            }
        }
    }

    override fun respondToIndexChange(old: Double, new: Double) {
        /* not necessary for cursor follower */
    }

    fun respondToScrollChange(deltaX: Double) {
        if (isShowing) {
            cursorRectangle.translateX -= deltaX
            cursorPointer.translateX -= deltaX
        }
    }
}