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
    init {
        trackRectangle.translateY = parentTrack.trackOffsetY
        trackRectangle.translateX = waveFormWidth / 2.0 + trackListViewModel.currentDividerOffset.getValue()
        trackRectangle.opacity = 0.8
        trackRectangle.stroke = trackListViewModel.strokeColor
        trackRectangle.strokeWidth = 0.5
        trackRectangle.strokeLineJoin = StrokeLineJoin.MITER
    }
    override fun addChild(child: Widget) {
    }

    override fun addMeToScene(root: StackPane) {
        root.children.add(trackRectangle)
    }

    override fun removeMeFromScene(root: StackPane) {
        Platform.runLater {
            root.children.remove(trackRectangle)
        }
    }

    override fun respondToOffsetYChange(old: Double, new: Double) {
        trackRectangle.translateY = new
    }

    override fun respondToWidthChange(old: Double, new: Double) {
        trackRectangle.translateX -= (new - old)/2.0
    }

    fun respondToDividerShift(newValue: Double) {
        trackRectangle.translateX  = waveFormWidth / 2.0 + newValue
        trackRectangle.toFront()
    }
}