package org.jzeisel.app_test.components.trackBar.smallComponents

import javafx.scene.layout.StackPane
import javafx.scene.paint.Color
import javafx.scene.shape.Circle
import javafx.scene.shape.Rectangle
import javafx.scene.shape.StrokeLineJoin
import org.jzeisel.app_test.components.TrackComponentWidget
import org.jzeisel.app_test.components.Widget
import org.jzeisel.app_test.components.trackBar.tracks.Track

class VolumeSlider(override val parent: Widget) : Widget, TrackComponentWidget {
    private val parentTrack = parent as Track
    private val trackListViewModel = parentTrack.trackListViewModel
    override val children = mutableListOf<Widget>()
    private val sliderBar = Rectangle(trackListViewModel.inputNameBoxWidth, 7.0, trackListViewModel.generalGray)
    private val sliderCircle = Circle(7.0, trackListViewModel.generalPurple)
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
        sliderBar.arcWidth = 7.0
        sliderBar.arcHeight = 7.0
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