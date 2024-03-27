package org.jzeisel.app_test.components.trackComponents

import javafx.application.Platform
import javafx.scene.layout.StackPane
import javafx.scene.paint.Color
import javafx.scene.shape.Rectangle
import org.jzeisel.app_test.components.TrackComponentWidget
import org.jzeisel.app_test.components.Widget
import org.jzeisel.app_test.components.NormalTrack
import org.jzeisel.app_test.components.Track
import org.jzeisel.app_test.util.BroadcastType
import org.jzeisel.app_test.util.ObservableListener
import org.jzeisel.app_test.util.runLater
import org.jzeisel.app_test.util.viewOrderFlip

class VUMeter(override val parent: Widget)
    : Widget, TrackComponentWidget, ObservableListener<Double> {

    /* object that represents a single VUMeter */
    /* made of 2 rectangles and a set of numBars Bars */
    private val parentTrack = parent as Track
    val trackListViewModel = parentTrack.trackListViewModel
    private val trackListState = trackListViewModel._trackListStateFlow.state
    private val numBars = 20
    val vuMeterWidth = trackListState.vuMeterWidth
    private var vuMeterHeight = parentTrack.initialTrackHeight / 1.75
    var vuMeterOffsetX = -(parentTrack.trackListViewModel.stage.width / 2.0) + trackListState.vuMeterOffset
    private var vuMeterOffsetY = parentTrack.trackOffsetY
    private val bgColor = trackListState.generalGray
    private val barSep = 0.0
    private val volumePerBar = 3000.0 / numBars

    private val vuMeterRectangle = Rectangle(vuMeterWidth, vuMeterHeight, bgColor)
    val barHeight = (vuMeterHeight - (barSep * numBars + 2)) / numBars

    override val children = mutableListOf<Widget>()

    init {
        vuMeterRectangle.translateX = vuMeterOffsetX
        vuMeterRectangle.translateY = vuMeterOffsetY
        vuMeterRectangle.arcWidth = trackListState.arcSize
        vuMeterRectangle.arcHeight = trackListState.arcSize
        vuMeterRectangle.stroke = trackListState.strokeColor
        vuMeterRectangle.strokeWidth = trackListState.strokeSize
        vuMeterRectangle.viewOrder = viewOrderFlip - 0.31
    }

    override fun addMeToScene(root: StackPane) {
        registerForBroadcasts()
        root.children.add(vuMeterRectangle)
        makeMeterBars(root)
    }

    override fun removeMeFromScene(root: StackPane) {
        runLater {
            unregisterForBroadcasts()
            for (child in children) {
                child.removeMeFromScene(root)
            }
            children.clear()
            root.children.remove(vuMeterRectangle)
        }
    }

    private fun makeMeterBars(root: StackPane) {
        for (bar in 0 until numBars) {
            val color: Color
            if (bar <= numBars /2) {
                color = Color.GREEN.brighter()
            }
            else if (bar <= 3* numBars /4) {
                color = Color.YELLOW.brighter()
            }
            else color = Color.RED.brighter()
            addChild(
                VUBar(color, vuMeterOffsetY + ((vuMeterHeight / 2) - barSep - barHeight /2)
                                - (bar * (barHeight + barSep)), this)
            )
        }
        runLater {
            for (bar in children) {
                bar.addMeToScene(root)
                /* initially make invisible */
                (bar as VUBar).isVisible(false)
            }
        }
    }

    override fun addChild(child: Widget) {
        children.add(child)
    }

    private fun makeAllBarsInvisible() {
        for (child in children) {
            (child as VUBar).isVisible(false)
        }
    }

    private fun makeBarVisible(bar: VUBar) {
        bar.isVisible(true)
    }

    override fun respondToHeightChange(old: Double, new: Double) {
        ((new - old)/2.0).let {
            vuMeterRectangle.translateY -= it
            vuMeterOffsetY = vuMeterRectangle.translateY
        }
    }

    override fun respondToWidthChange(old: Double, new: Double) {
        ((new - old)/2.0).let {
            vuMeterRectangle.translateX -= it
            vuMeterOffsetX = vuMeterRectangle.translateX
        }
    }

    override fun respondToIndexChange(old: Double, new: Double) {
        vuMeterOffsetY = parentTrack.trackOffsetY
        vuMeterRectangle.translateY = vuMeterOffsetY
    }

    override fun respondToChange(broadcastType: BroadcastType, old: Double, new: Double) {
        when (broadcastType) {
            BroadcastType.STAGE_HEIGHT -> respondToHeightChange(old, new)
            BroadcastType.STAGE_WIDTH -> respondToWidthChange(old, new)
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
        trackListViewModel.unregisterForWidthChanges(this)
        trackListViewModel.unregisterForHeightChanges(this)
        if (parentTrack is NormalTrack) {
            parentTrack.registerForIndexChanges(this)
        }
    }
}