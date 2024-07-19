package org.jzeisel.app_test.components.trackComponents

import javafx.event.EventHandler
import javafx.scene.layout.StackPane
import javafx.scene.shape.Circle
import javafx.scene.shape.Rectangle
import javafx.scene.shape.StrokeLineJoin
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jzeisel.app_test.components.interfaces.TrackElement
import org.jzeisel.app_test.components.interfaces.widget.Widget
import org.jzeisel.app_test.components.NormalTrack
import org.jzeisel.app_test.components.Track
import org.jzeisel.app_test.components.interfaces.WindowElement
import org.jzeisel.app_test.components.interfaces.widget.NodeWidget
import org.jzeisel.app_test.util.*

class VolumeSlider(override val parent: Widget)
    : NodeWidget, TrackElement, WindowElement {

    private val parentTrack = parent as Track
    private val trackListViewModel = parentTrack.trackListViewModel
    private val trackListState = trackListViewModel._trackListStateFlow.state
    override val children = mutableListOf<Widget>()
    private val sliderBar = Rectangle(trackListState.inputNameBoxWidth, 5.0, trackListState.generalGray)
    private val sliderCircle = Circle(6.0, trackListState.generalPurple)
    private var volumeDecibel = 0.0
    init {
        sliderBar.translateY = parentTrack.trackOffsetY + trackListState.verticalDistancesBetweenWidgets
        sliderCircle.translateY = parentTrack.trackOffsetY + trackListState.verticalDistancesBetweenWidgets
        sliderBar.translateX = -(trackListViewModel.stage.width / 2) + trackListState.inputNameBoxOffset
        sliderCircle.translateX = sliderBar.translateX + sliderBar.width / 4.0
        sliderCircle.stroke = trackListState.strokeColor
        sliderCircle.strokeLineJoin = StrokeLineJoin.MITER
        sliderCircle.strokeWidth = trackListState.strokeSize
        sliderBar.strokeWidth = trackListState.strokeSize
        sliderBar.stroke = trackListState.strokeColor
        sliderBar.strokeLineJoin = StrokeLineJoin.MITER
        sliderBar.arcWidth = 3.0
        sliderBar.arcHeight = 3.0
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
            val finalX = sliderCircle.translateX + it.x
            setTranslateX(finalX)
        }

        sliderBar.onMousePressed = EventHandler {
            /* it.x here represents distance from the left side of the slider */
            val finalX = sliderBar.translateX - sliderBar.width/2.0 + it.x
            sliderCircle.fill = trackListState.generalPurple.brighter()
            setTranslateX(finalX)
            animateObjectScale(1.0, 1.3, sliderCircle)
        }
        sliderBar.onMouseReleased = EventHandler {
            sliderCircle.fill = trackListState.generalPurple
            animateObjectScale(1.3, 1.0, sliderCircle)
        }
        sliderBar.onMouseDragged = EventHandler {
            val finalX = sliderBar.translateX - sliderBar.width/2.0 + it.x
            setTranslateX(finalX)
        }

        CoroutineScope(Dispatchers.Default).launch {
            delay(500)
            getDbFromCircleLocation()
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
        runLater {
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

    private fun setTranslateX(finalX: Double) {
        sliderCircle.translateX = finalX
        if (sliderCircle.translateX < (sliderBar.translateX - sliderBar.width/2.0)) {
            sliderCircle.translateX = sliderBar.translateX - sliderBar.width/2.0
        }
        if (sliderCircle.translateX > (sliderBar.translateX + sliderBar.width/2.0)) {
            sliderCircle.translateX = sliderBar.translateX + sliderBar.width/2.0
        }
        getDbFromCircleLocation()
    }

    private fun getDbFromCircleLocation() {
        val distanceFromStart = sliderCircle.translateX - (sliderBar.translateX - sliderBar.width/2.0)
        volumeDecibel = scaleNumberLogarithmic(distanceFromStart, -60.0, 20.0, 0.0, 90.0)
        trackListViewModel.setTrackVolume(parentTrack, volumeDecibel)
    }
}