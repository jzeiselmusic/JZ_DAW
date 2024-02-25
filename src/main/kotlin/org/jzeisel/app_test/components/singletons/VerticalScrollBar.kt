package org.jzeisel.app_test.components.singletons

import javafx.application.Platform
import javafx.scene.layout.StackPane
import javafx.scene.paint.Color
import javafx.scene.shape.Rectangle
import org.jzeisel.app_test.TrackListViewModel
import org.jzeisel.app_test.components.TrackComponentWidget
import org.jzeisel.app_test.util.BroadcastType
import org.jzeisel.app_test.util.ObservableListener

object VerticalScrollBar: TrackComponentWidget, ObservableListener<Double> {
    private lateinit var trackListViewModel: TrackListViewModel
    private lateinit var scrollRectangle: Rectangle
    var isShowing = false

    private val stageHeight: Double get() { return trackListViewModel.observableStageHeight.getValue() }
    private val stageWidth: Double get() { return trackListViewModel.observableStageWidth.getValue() }
    fun initialize(trackListViewModel: TrackListViewModel){
        this.trackListViewModel = trackListViewModel
    }

    private fun Double.saturateAt(max: Double, min: Double): Double {
        if (this > max) return max
        else if (this < min) return min
        else return this
    }

    fun addMeToScene(root: StackPane) {
        var percentVisible = (stageHeight / trackListViewModel.totalHeightOfAllTracks).saturateAt(1.0, 0.0)
        val barHeight = percentVisible * (stageHeight - 20.0)
        scrollRectangle = Rectangle(8.0, barHeight, Color.DARKGRAY.darker())
        scrollRectangle.translateX = stageWidth / 2.0 - 10.0
        scrollRectangle.translateY = -stageHeight/2.0 + barHeight/2.0 + 10.0
        scrollRectangle.opacity = 0.8
        scrollRectangle.arcWidth = 5.0
        scrollRectangle.arcHeight = 5.0
        scrollRectangle.toFront()
        root.children.add(scrollRectangle)
        registerForBroadcasts()
        isShowing = true
    }

    fun removeMeFromScene(root: StackPane) {
        if (isShowing) {
            Platform.runLater {
                unregisterForBroadcasts()
                root.children.remove(scrollRectangle)
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