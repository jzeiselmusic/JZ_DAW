package org.jzeisel.app_test.components.singletons

import javafx.application.Platform
import javafx.scene.layout.StackPane
import javafx.scene.paint.Color
import javafx.scene.shape.Rectangle
import org.jzeisel.app_test.viewmodel.TrackListViewModel
import org.jzeisel.app_test.components.TrackComponentWidget
import org.jzeisel.app_test.stateflow.TrackListState
import org.jzeisel.app_test.stateflow.TrackListStateFlow
import org.jzeisel.app_test.util.BroadcastType
import org.jzeisel.app_test.util.ObservableListener
import org.jzeisel.app_test.util.viewOrderFlip

object VerticalScrollBar: TrackComponentWidget, ObservableListener<Double> {
    private lateinit var trackListViewModel: TrackListViewModel
    private lateinit var trackListState: TrackListState
    private lateinit var trackListFlow: TrackListStateFlow
    private lateinit var scrollRectangle: Rectangle
    private lateinit var scrollRectangleStackPane : StackPane
    var isShowing = false
    private val stageHeight: Double get() { return trackListState.observableStageHeight.getValue() }
    private val stageWidth: Double get() { return trackListState.observableStageWidth.getValue() }
    val percentVisible: Double get() {
        return (stageHeight/ trackListFlow.totalHeightOfAllTracks).saturateAt(0.0, 1.0)
    }
    private val barHeight: Double get() { return percentVisible * stageHeight - 45.0 }
    private var currentOffsetFromTop: Double = 0.0
    private val roomAtBottom: Double get() { return stageHeight - barHeight - 45.0 }
    fun initialize(trackListViewModel: TrackListViewModel, pane: StackPane){
        this.trackListViewModel = trackListViewModel
        trackListState = trackListViewModel._trackListStateFlow.state
        trackListFlow = trackListViewModel._trackListStateFlow
        scrollRectangleStackPane = pane
    }

    fun Double.saturateAt(min: Double, max: Double?): Double {
        if (max != null) {
            if (this > max) return max
            else if (this < min) return min
        } else {
            if (this < min) return min
            else return this
        }
        return this
    }

    fun addMeToScene() {
        scrollRectangle = Rectangle(8.0, barHeight, Color.DARKGRAY.darker())
        scrollRectangle.translateX = stageWidth / 2.0 - 12.0
        scrollRectangle.translateY = -stageHeight/2.0 + barHeight/2.0 + 23.0 + currentOffsetFromTop
        scrollRectangle.opacity = 0.85
        scrollRectangle.arcWidth = 5.0
        scrollRectangle.arcHeight = 5.0
        scrollRectangle.viewOrder = viewOrderFlip - 0.55
        scrollRectangleStackPane.children.add(scrollRectangle)
        registerForBroadcasts()
        isShowing = true
    }

    fun removeMeFromScene() {
        if (isShowing) {
            Platform.runLater {
                unregisterForBroadcasts()
                scrollRectangleStackPane.children.remove(scrollRectangle)
                isShowing = false
            }
        }
    }

    override fun respondToHeightChange(old: Double, new: Double) {
        if (isShowing) {
            ((new - old)/2.0).let {
                scrollRectangle.translateY -= it
            }
        }
    }

    override fun respondToWidthChange(old: Double, new: Double) {
        if (isShowing) {
            ((new - old)/2.0).let {
                scrollRectangle.translateX += it
            }
        }
    }

    override fun respondToIndexChange(old: Double, new: Double) {}

    override fun respondToChange(broadcastType: BroadcastType, old: Double, new: Double) {
        when (broadcastType) {
            BroadcastType.STAGE_WIDTH -> respondToWidthChange(old, new)
            BroadcastType.STAGE_HEIGHT -> respondToHeightChange(old, new)
            BroadcastType.DIVIDER -> {}
            BroadcastType.INDEX -> {}
            BroadcastType.SCROLL -> {}
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

    fun moveScrollBar(deltaY: Double, amountOfRoom: Double) {
        val myDeltaY: Double
        if (amountOfRoom < 1.0) myDeltaY = deltaY
        else myDeltaY = roomAtBottom * (deltaY/amountOfRoom)
        currentOffsetFromTop = (currentOffsetFromTop - myDeltaY).saturateAt(0.0, roomAtBottom)
        scrollRectangle.translateY = -stageHeight/2.0 + barHeight/2.0 + 23.0 + currentOffsetFromTop
    }
}