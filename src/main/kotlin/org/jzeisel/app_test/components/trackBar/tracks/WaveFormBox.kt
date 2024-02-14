package org.jzeisel.app_test.components.trackBar.tracks

import javafx.application.Platform
import javafx.scene.layout.StackPane
import javafx.scene.paint.Color
import javafx.scene.shape.Rectangle
import javafx.scene.shape.StrokeLineJoin
import org.jzeisel.app_test.components.TrackComponentWidget
import org.jzeisel.app_test.components.Widget

class WaveFormBox(override val parent: Widget) : Widget, TrackComponentWidget {
    override val children: MutableList<Widget> = mutableListOf()
    val parentTrack = parent as Track
    val trackListViewModel = parentTrack.trackListViewModel
    val waveFormWidth = 5000.0
    val trackRectangle = Rectangle(waveFormWidth,
                                   parentTrack.initialTrackHeight,
                                   trackListViewModel.generalPurple)
    val measureDividers = mutableListOf<Rectangle>()

    init {
        trackRectangle.translateY = parentTrack.trackOffsetY
        trackRectangle.translateX = waveFormWidth / 2.0 + trackListViewModel.currentDividerOffset.getValue()
        trackRectangle.opacity = 0.8
        trackRectangle.stroke = trackListViewModel.strokeColor
        trackRectangle.strokeWidth = 0.5
        trackRectangle.strokeLineJoin = StrokeLineJoin.MITER

        for (i in 0..(waveFormWidth/100).toInt()) {
            val measure = Rectangle(1.0, trackListViewModel.trackHeight, trackListViewModel.strokeColor)
            measure.opacity = 0.6
            measure.strokeWidth = 1.0
            measure.translateY = parentTrack.trackOffsetY
            measure.translateX = trackRectangle.translateX - waveFormWidth/2.0 + i*100.0
            measure.toBack()
            measureDividers.add(measure)
        }
    }
    override fun addChild(child: Widget) {
    }

    override fun addMeToScene(root: StackPane) {
        root.children.add(trackRectangle)
        for (measureDivider in measureDividers) {
            root.children.add(measureDivider)
        }
    }

    override fun removeMeFromScene(root: StackPane) {
        Platform.runLater {
            root.children.remove(trackRectangle)
        }
    }

    override fun respondToOffsetYChange(old: Double, new: Double) {
        trackRectangle.translateY = new
        for (measureDivider in measureDividers) {
            measureDivider.translateY = new
        }
    }

    override fun respondToWidthChange(old: Double, new: Double) {
        val change = (new - old)/2.0
        trackRectangle.translateX -= change
        for (measureDivider in measureDividers) {
            measureDivider.translateX -= change
        }
    }

    fun respondToDividerShift(newValue: Double) {
        val oldTranslate = trackRectangle.translateX
        trackRectangle.translateX = waveFormWidth / 2.0 + newValue
        val change = trackRectangle.translateX - oldTranslate
        for (measureDivider in measureDividers) {
            measureDivider.translateX += change
        }
    }
}