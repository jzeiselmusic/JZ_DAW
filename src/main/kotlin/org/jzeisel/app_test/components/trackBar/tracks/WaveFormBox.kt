package org.jzeisel.app_test.components.trackBar.tracks

import javafx.application.Platform
import javafx.scene.layout.StackPane
import javafx.scene.paint.Color
import javafx.scene.shape.Rectangle
import javafx.scene.shape.StrokeLineJoin
import org.jzeisel.app_test.components.Widget

class WaveFormBox(override val parent: Widget) : Widget {
    override val children: MutableList<Widget> = mutableListOf()
    val parentTrack = parent as Track
    val waveFormWidth = 2000.0

    val trackRectangle = Rectangle(waveFormWidth,
                                   parentTrack.trackHeight,
                                   Color.MEDIUMPURPLE.darker())
    init {
        trackRectangle.translateY = parentTrack.trackOffsetY
        trackRectangle.translateX = waveFormWidth / 2.0 + parentTrack.initialDividerOffset.getValue()
        trackRectangle.opacity = 0.80
        trackRectangle.stroke = Color.BLACK
        trackRectangle.strokeWidth = 0.5
        trackRectangle.strokeLineJoin = StrokeLineJoin.ROUND

        parentTrack.trackListViewModel.stageWidthProperty.addListener { _, old, new ->
            trackRectangle.translateX -= (new as Double - old as Double)/2.0
        }
        parentTrack.trackListViewModel.stageHeightProperty.addListener { _, old, new ->
            trackRectangle.translateY -= (new as Double - old as Double)/2.0
        }
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

    fun respondToDividerShift(newValue: Double) {
        trackRectangle.translateX  = waveFormWidth / 2.0 + newValue
        trackRectangle.toFront()
    }
}