package org.jzeisel.app_test.components.vuMeter

import javafx.application.Platform
import javafx.scene.layout.StackPane
import javafx.scene.paint.Color
import javafx.scene.shape.Rectangle
import org.jzeisel.app_test.components.TrackComponentWidget
import org.jzeisel.app_test.components.Widget
import org.jzeisel.app_test.components.trackBar.tracks.NormalTrack
import org.jzeisel.app_test.components.trackBar.tracks.Track
import org.jzeisel.app_test.util.BroadcastType
import org.jzeisel.app_test.util.Observable
import org.jzeisel.app_test.util.ObservableListener

class VUBar(color: Color, private var barOffsetY: Double,
            override val parent: Widget)
    : Widget, TrackComponentWidget, ObservableListener<Double> {

    private val parentVUMeter = parent as VUMeter
    private val parentTrack = parentVUMeter.parent as Track
    private val barWidth = parentVUMeter.vuMeterWidth - 4.0
    private val barHeight = parentVUMeter.barHeight
    private var barOffsetX = parentVUMeter.vuMeterOffsetX
    private val barRectangle = Rectangle(barWidth, barHeight, color)

    override val children = mutableListOf<Widget>()

    override fun addChild(child: Widget) {
        /* VUBar will not have any children */
    }

    fun bringToFront() {
        barRectangle.toFront()
    }

    fun isVisible(t: Boolean) {
        barRectangle.isVisible = t
    }

    override fun addMeToScene(root: StackPane) {
        registerForBroadcasts()
        barRectangle.translateX = barOffsetX
        barRectangle.translateY = barOffsetY
        barRectangle.arcWidth = 0.5
        barRectangle.arcHeight = 0.5
        root.children.add(barRectangle)
    }

    override fun removeMeFromScene(root: StackPane) {
        Platform.runLater {
            unregisterForBroadcasts()
            root.children.remove(barRectangle)
        }
    }

    override fun respondToHeightChange(old: Double, new: Double) {
        ((new - old)/2.0).let {
            barOffsetY -= it
            barRectangle.translateY -= it
        }
    }

    override fun respondToWidthChange(old: Double, new: Double) {
        ((new - old)/2.0).let {
            barOffsetX -= it
            barRectangle.translateX -= it
        }
    }

    override fun respondToIndexChange(old: Double, new: Double) {
        val indexShift = new - old
        barOffsetY += parentVUMeter.trackListViewModel.trackHeight * indexShift
        barRectangle.translateY = barOffsetY
    }

    override fun respondToChange(broadcastType: BroadcastType, old: Double, new: Double) {
        when (broadcastType) {
            BroadcastType.STAGE_WIDTH -> respondToWidthChange(old, new)
            BroadcastType.STAGE_HEIGHT -> respondToHeightChange(old, new)
            BroadcastType.INDEX -> respondToIndexChange(old, new)
            BroadcastType.DIVIDER -> {}
        }
    }

    override fun registerForBroadcasts() {
        parentVUMeter.trackListViewModel.registerForWidthChanges(this)
        parentVUMeter.trackListViewModel.registerForHeightChanges(this)
        if (parentTrack is NormalTrack) {
            parentTrack.registerForIndexChanges(this)
        }
    }

    override fun unregisterForBroadcasts() {
        parentVUMeter.trackListViewModel.unregisterForWidthChanges(this)
        parentVUMeter.trackListViewModel.unregisterForHeightChanges(this)
        if (parentTrack is NormalTrack) {
            parentTrack.registerForIndexChanges(this)
        }
    }
}