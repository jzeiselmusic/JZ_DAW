package org.jzeisel.app_test.components.trackComponents

import javafx.application.Platform
import javafx.event.EventHandler
import javafx.scene.layout.StackPane
import javafx.scene.shape.Circle
import javafx.scene.shape.Rectangle
import javafx.scene.shape.StrokeLineJoin
import org.jzeisel.app_test.components.TrackComponentWidget
import org.jzeisel.app_test.components.Widget
import org.jzeisel.app_test.components.NormalTrack
import org.jzeisel.app_test.components.Track
import org.jzeisel.app_test.util.BroadcastType
import org.jzeisel.app_test.util.ObservableListener
import org.jzeisel.app_test.util.animateObjectScale
import org.jzeisel.app_test.util.viewOrderFlip

class VolumeSlider(override val parent: Widget)
    : Widget, TrackComponentWidget, ObservableListener<Double> {

    private val parentTrack = parent as Track
    private val trackListViewModel = parentTrack.trackListViewModel
    private val trackListState = trackListViewModel._trackListStateFlow.state
    override val children = mutableListOf<Widget>()
    private val sliderBar = Rectangle(trackListState.inputNameBoxWidth, 5.0, trackListState.generalGray)
    private val sliderCircle = Circle(6.0, trackListState.generalPurple)
    init {
        sliderBar.translateY = parentTrack.trackOffsetY + trackListState.verticalDistancesBetweenWidgets
        sliderCircle.translateY = parentTrack.trackOffsetY + trackListState.verticalDistancesBetweenWidgets
        sliderBar.translateX = -(trackListViewModel.stage.width / 2) + trackListState.inputNameBoxOffset
        sliderCircle.translateX = sliderBar.translateX + sliderBar.width / 4.0
        sliderCircle.stroke = trackListState.strokeColor
        sliderCircle.strokeLineJoin = StrokeLineJoin.MITER
        sliderCircle.strokeWidth = trackListState.strokeSize - 0.3
        sliderBar.strokeWidth = trackListState.strokeSize
        sliderBar.stroke = trackListState.strokeColor
        sliderBar.strokeLineJoin = StrokeLineJoin.MITER
        sliderBar.arcWidth = 7.0
        sliderBar.arcHeight = 7.0
        sliderBar.viewOrder = viewOrderFlip - 0.31
        sliderCircle.viewOrder = viewOrderFlip - 0.32

        sliderCircle.onMousePressed = EventHandler {
            sliderCircle.fill = trackListState.generalPurple.brighter()
            animateObjectScale(1.0, 1.3, sliderCircle)
        }
        sliderCircle.onMouseReleased = EventHandler {
            sliderCircle.fill = trackListState.generalPurple
            animateObjectScale(1.3, 1.0, sliderCircle)
        }
        sliderCircle.onMouseDragged = EventHandler {
            sliderCircle.translateX += it.x
            if (sliderCircle.translateX < (sliderBar.translateX - sliderBar.width/2.0)) {
                sliderCircle.translateX = sliderBar.translateX - sliderBar.width/2.0
            }
            if (sliderCircle.translateX > (sliderBar.translateX + sliderBar.width/2.0)) {
                sliderCircle.translateX = sliderBar.translateX + sliderBar.width/2.0
            }
        }

        sliderBar.onMousePressed = EventHandler {
            /* it.x here represents distance from the left side of the slider */
            val finalX = sliderBar.translateX - sliderBar.width/2.0 + it.x
            sliderCircle.fill = trackListState.generalPurple.brighter()
            sliderCircle.translateX = finalX
            animateObjectScale(1.0, 1.3, sliderCircle)
        }
        sliderBar.onMouseReleased = EventHandler {
            sliderCircle.fill = trackListState.generalPurple
            animateObjectScale(1.3, 1.0, sliderCircle)
        }
        sliderBar.onMouseDragged = EventHandler {
            val finalX = sliderBar.translateX - sliderBar.width/2.0 + it.x
            sliderCircle.translateX = finalX
            if (sliderCircle.translateX < (sliderBar.translateX - sliderBar.width/2.0)) {
                sliderCircle.translateX = sliderBar.translateX - sliderBar.width/2.0
            }
            if (sliderCircle.translateX > (sliderBar.translateX + sliderBar.width/2.0)) {
                sliderCircle.translateX = sliderBar.translateX + sliderBar.width/2.0
            }
        }
    }

    override fun respondToChange(broadcastType: BroadcastType, old: Double, new: Double) {
        when (broadcastType) {
            BroadcastType.STAGE_WIDTH -> respondToWidthChange(old, new)
            BroadcastType.STAGE_HEIGHT -> respondToHeightChange(old, new)
            BroadcastType.INDEX -> respondToIndexChange(old, new)
            BroadcastType.DIVIDER -> {}
            BroadcastType.SCROLL -> {}
        }
    }

    override fun registerForBroadcasts() {
        trackListViewModel.registerForHeightChanges(this)
        trackListViewModel.registerForWidthChanges(this)
        if (parentTrack is NormalTrack) {
            parentTrack.registerForIndexChanges(this)
        }
    }

    override fun unregisterForBroadcasts() {
        trackListViewModel.unregisterForHeightChanges(this)
        trackListViewModel.unregisterForWidthChanges(this)
        if (parentTrack is NormalTrack) {
            parentTrack.unregisterForIndexChanges(this)
        }
    }

    override fun addChild(child: Widget) {
    }

    override fun addMeToScene(root: StackPane) {
        registerForBroadcasts()
        root.children.add(sliderBar)
        root.children.add(sliderCircle)
    }

    override fun removeMeFromScene(root: StackPane) {
        Platform.runLater {
            unregisterForBroadcasts()
            root.children.remove(sliderCircle)
            root.children.remove(sliderBar)
        }
    }

    override fun respondToHeightChange(old: Double, new: Double) {
        ((new - old)/2.0).let {
            sliderCircle.translateY -= it
            sliderBar.translateY -= it
        }
    }

    override fun respondToWidthChange(old: Double, new: Double) {
        ((new - old)/2.0).let {
            sliderCircle.translateX -= it
            sliderBar.translateX -= it
        }
    }

    override fun respondToIndexChange(old: Double, new: Double) {
        (parentTrack.trackOffsetY + trackListState.verticalDistancesBetweenWidgets).let {
            sliderBar.translateY = it
            sliderCircle.translateY = it
        }
    }
}