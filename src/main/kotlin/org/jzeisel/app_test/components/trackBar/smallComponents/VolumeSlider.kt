package org.jzeisel.app_test.components.trackBar.smallComponents

import javafx.animation.Timeline
import javafx.event.EventHandler
import javafx.scene.layout.StackPane
import javafx.scene.shape.Circle
import javafx.scene.shape.Rectangle
import javafx.scene.shape.StrokeLineJoin
import org.jzeisel.app_test.components.TrackComponentWidget
import org.jzeisel.app_test.components.Widget
import org.jzeisel.app_test.components.trackBar.tracks.Track
import org.jzeisel.app_test.logger.Logger


class VolumeSlider(override val parent: Widget) : Widget, TrackComponentWidget {
    companion object {
        const val TAG = "VolumeSlider"
    }
    private val parentTrack = parent as Track
    private val trackListViewModel = parentTrack.trackListViewModel
    override val children = mutableListOf<Widget>()
    private val sliderBar = Rectangle(trackListViewModel.inputNameBoxWidth, 6.0, trackListViewModel.generalGray)
    private val sliderCircle = Circle(6.0, trackListViewModel.generalPurple)
    init {
        sliderBar.translateY = parentTrack.trackOffsetY + trackListViewModel.verticalDistancesBetweenWidgets
        sliderCircle.translateY = parentTrack.trackOffsetY + trackListViewModel.verticalDistancesBetweenWidgets
        sliderBar.translateX = -(trackListViewModel.stage.width / 2) + trackListViewModel.inputNameBoxOffset
        sliderCircle.translateX = sliderBar.translateX + sliderBar.width / 4.0
        sliderCircle.stroke = trackListViewModel.strokeColor
        sliderCircle.strokeLineJoin = StrokeLineJoin.MITER
        sliderCircle.strokeWidth = trackListViewModel.strokeSize
        sliderBar.strokeWidth = trackListViewModel.strokeSize
        sliderBar.stroke = trackListViewModel.strokeColor
        sliderBar.strokeLineJoin = StrokeLineJoin.MITER
        sliderBar.arcWidth = 7.0
        sliderBar.arcHeight = 7.0

        sliderCircle.onMousePressed = EventHandler {
            sliderCircle.fill = trackListViewModel.generalPurple.brighter()
        }
        sliderCircle.onMouseReleased = EventHandler {
            sliderCircle.fill = trackListViewModel.generalPurple
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
            sliderCircle.fill = trackListViewModel.generalPurple.brighter()
            sliderCircle.translateX = finalX
        }
        sliderBar.onMouseReleased = EventHandler {
            sliderCircle.fill = trackListViewModel.generalPurple
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
    override fun addChild(child: Widget) {
    }

    override fun addMeToScene(root: StackPane) {
        root.children.add(sliderBar)
        root.children.add(sliderCircle)
    }

    override fun removeMeFromScene(root: StackPane) {
        root.children.remove(sliderCircle)
        root.children.remove(sliderBar)
    }

    override fun respondToOffsetYChange(old: Double, new: Double) {
        sliderCircle.translateY += new - old
        sliderBar.translateY += new - old
    }

    override fun respondToWidthChange(old: Double, new: Double) {
        sliderCircle.translateX -= (new - old)/2.0
        sliderBar.translateX -= (new - old)/2.0
    }
}