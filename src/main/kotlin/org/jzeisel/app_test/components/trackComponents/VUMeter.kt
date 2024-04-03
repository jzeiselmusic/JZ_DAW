package org.jzeisel.app_test.components.trackComponents

import javafx.animation.ParallelTransition
import javafx.animation.PathTransition
import javafx.animation.ScaleTransition
import javafx.scene.layout.StackPane
import javafx.scene.paint.Color
import javafx.scene.shape.MoveTo
import javafx.scene.shape.Path
import javafx.scene.shape.Rectangle
import javafx.util.Duration
import org.jzeisel.app_test.components.TrackComponentWidget
import org.jzeisel.app_test.components.Widget
import org.jzeisel.app_test.components.NormalTrack
import org.jzeisel.app_test.components.Track
import org.jzeisel.app_test.util.*

class VUMeter(override val parent: Widget)
    : Widget, TrackComponentWidget, ObservableListener<Double> {

    private val parentTrack = parent as Track
    val trackListViewModel = parentTrack.trackListViewModel
    private val trackListState = trackListViewModel._trackListStateFlow.state
    val vuMeterWidth = trackListState.vuMeterWidth
    private var vuMeterHeight = parentTrack.initialTrackHeight / 1.75
    var vuMeterOffsetX = -(parentTrack.trackListViewModel.stage.width / 2.0) + trackListState.vuMeterOffset
    private var vuMeterOffsetY = parentTrack.trackOffsetY
    private val bgColor = trackListState.generalGray
    val volumeMinimumHeight = 1.0
    val volumeMaximumHeight = vuMeterHeight - 4.0
    val volumeRectangleY: Double get() { return vuMeterOffsetY + vuMeterRectangle.height / 2.0 - 2.5 }
    private val vuMeterRectangle = Rectangle(vuMeterWidth, vuMeterHeight, bgColor)
    private val volumeRectangle = Rectangle(vuMeterWidth - 4.0, volumeMinimumHeight, Color.GREEN)
    override val children = mutableListOf<Widget>()
    var currentVolume: Double = -100.0
    lateinit var parallelTransition: ParallelTransition
    var isVUMeterRunning = false

    init {
        vuMeterRectangle.translateX = vuMeterOffsetX
        vuMeterRectangle.translateY = vuMeterOffsetY
        vuMeterRectangle.arcWidth = trackListState.arcSize
        vuMeterRectangle.arcHeight = trackListState.arcSize
        vuMeterRectangle.stroke = trackListState.strokeColor
        vuMeterRectangle.strokeWidth = trackListState.strokeSize
        vuMeterRectangle.viewOrder = viewOrderFlip - 0.31
        vuMeterRectangle.isMouseTransparent = true

        volumeRectangle.translateX = vuMeterOffsetX - 0.5
        volumeRectangle.translateY = volumeRectangleY
        volumeRectangle.arcWidth = trackListState.arcSize
        volumeRectangle.arcHeight = trackListState.arcSize
        volumeRectangle.viewOrder = viewOrderFlip - 0.31
        volumeRectangle.isMouseTransparent = true
    }

    override fun addMeToScene(root: StackPane) {
        registerForBroadcasts()
        root.children.add(vuMeterRectangle)
        root.children.add(volumeRectangle)
    }

    override fun removeMeFromScene(root: StackPane) {
        runLater {
            unregisterForBroadcasts()
            root.children.remove(vuMeterRectangle)
            root.children.remove(volumeRectangle)
        }
    }

    override fun addChild(child: Widget) {
        children.add(child)
    }

    override fun respondToHeightChange(old: Double, new: Double) {
        ((new - old)/2.0).let {
            vuMeterRectangle.translateY -= it
            volumeRectangle.translateY -= it
            vuMeterOffsetY = vuMeterRectangle.translateY
        }
    }

    override fun respondToWidthChange(old: Double, new: Double) {
        ((new - old)/2.0).let {
            vuMeterRectangle.translateX -= it
            volumeRectangle.translateX -= it
            vuMeterOffsetX = vuMeterRectangle.translateX
        }
    }

    override fun respondToIndexChange(old: Double, new: Double) {
        vuMeterOffsetY = parentTrack.trackOffsetY
        vuMeterRectangle.translateY = vuMeterOffsetY
        volumeRectangle.translateY = volumeRectangleY
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

    fun setVUMeterCurrentRMS(volume: Double) {
        if (isVUMeterRunning) {
            currentVolume = volume
            runLater {
                val oldHeight = volumeRectangle.height
                val newHeight = scaleNumber(volume, volumeMaximumHeight, volumeMinimumHeight)
                volumeRectangle.translateY += (oldHeight - newHeight) / 2.0
                volumeRectangle.height = newHeight
            }
        }
    }

    fun turnOffCurrentRMSReading() {
        currentVolume = -100.0
        runLater {
            volumeRectangle.height = volumeMinimumHeight
            volumeRectangle.translateY = volumeRectangleY
        }
    }
}