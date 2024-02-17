package org.jzeisel.app_test.components.trackBar.tracks

import javafx.application.Platform
import javafx.event.EventHandler
import javafx.scene.layout.StackPane
import javafx.scene.shape.Rectangle
import javafx.scene.shape.StrokeLineJoin
import org.jzeisel.app_test.components.TrackComponentWidget
import org.jzeisel.app_test.components.Widget
import org.jzeisel.app_test.logger.Logger

class WaveFormBox(override val parent: Widget) : Widget, TrackComponentWidget {
    companion object {
        const val TAG = "WaveFormBox"
    }
    override val children: MutableList<Widget> = mutableListOf()
    val parentTrack = parent as Track
    val trackListViewModel = parentTrack.trackListViewModel
    val waveFormWidth = 5000.0
    val trackRectangle = Rectangle(waveFormWidth,
                                   parentTrack.initialTrackHeight,
                                   trackListViewModel.generalPurple)

    val measureDividers = mutableListOf<Rectangle>()
    val beatDividers = mutableListOf<Rectangle>()
    val ticksForMasterTrack = mutableListOf<Rectangle>()

    init {
        trackRectangle.translateY = parentTrack.trackOffsetY
        trackRectangle.translateX = waveFormWidth / 2.0 + trackListViewModel.currentDividerOffset.getValue()
        trackRectangle.opacity = 0.8
        trackRectangle.stroke = trackListViewModel.strokeColor
        trackRectangle.strokeWidth = 0.5
        trackRectangle.strokeLineJoin = StrokeLineJoin.MITER

        trackRectangle.onMousePressed = EventHandler {
            trackListViewModel.broadcastMouseClickOnWaveFormBox(it.x)
        }

        for (i in 0..(waveFormWidth/100).toInt()) {
            val measure = Rectangle(1.0, trackListViewModel.trackHeight, trackListViewModel.strokeColor)
            measure.opacity = 0.6
            measure.strokeWidth = 0.8
            measure.translateY = parentTrack.trackOffsetY
            measure.translateX = trackRectangle.translateX - waveFormWidth/2.0 + i*100.0
            measure.toBack()
            measure.isMouseTransparent = true
            measureDividers.add(measure)
            if (parentTrack is MasterTrack) {
                val tick = Rectangle(1.2, 5.0, trackListViewModel.generalGray.darker())
                tick.opacity = 1.0
                tick.strokeWidth = 0.8
                tick.arcHeight = 2.0
                tick.arcWidth = 2.0
                tick.stroke = trackListViewModel.generalGray.darker()
                tick.translateY = measure.translateY - trackListViewModel.trackHeight/2.0 + tick.height/2.0 + 1.0
                tick.translateX = measure.translateX
                tick.isMouseTransparent = true
                ticksForMasterTrack.add(tick)
            }

            for (j in 1..3) {
                val beat = Rectangle(0.4, trackListViewModel.trackHeight, trackListViewModel.strokeColor)
                beat.opacity = 0.5
                beat.strokeWidth = 0.2
                beat.translateY = parentTrack.trackOffsetY
                beat.translateX = measure.translateX + (j * 100.0/4.0)
                beat.toBack()
                beat.isMouseTransparent = true
                beatDividers.add(beat)
                if (parentTrack is MasterTrack) {
                    val tick = Rectangle(1.0, 3.0, trackListViewModel.generalGray.darker())
                    tick.opacity = 1.0
                    tick.strokeWidth = 0.4
                    tick.arcHeight = 2.0
                    tick.arcWidth = 2.0
                    tick.stroke = trackListViewModel.generalGray
                    tick.translateY = measure.translateY - trackListViewModel.trackHeight/2.0 + tick.height/2.0 + 1.0
                    tick.translateX = beat.translateX
                    tick.isMouseTransparent = true
                    ticksForMasterTrack.add(tick)
                }
            }
        }
    }
    override fun addChild(child: Widget) {
    }

    override fun addMeToScene(root: StackPane) {
        root.children.add(trackRectangle)
        for (measureDivider in measureDividers) {
            root.children.add(measureDivider)
        }
        for (tickDivider in beatDividers) {
            root.children.add(tickDivider)
        }
        for (beatTick in ticksForMasterTrack) {
            root.children.add(beatTick)
        }
    }

    override fun removeMeFromScene(root: StackPane) {
        Platform.runLater {
            root.children.remove(trackRectangle)
            for (measureDivider in measureDividers) {
                root.children.remove(measureDivider)
            }
            for (tickDivider in beatDividers) {
                root.children.remove(tickDivider)
            }
            for (beatTick in ticksForMasterTrack) {
                root.children.remove(beatTick)
            }
        }
    }

    override fun respondToOffsetYChange(old: Double, new: Double) {
        trackRectangle.translateY = new
        for (measureDivider in measureDividers) {
            measureDivider.translateY = new
        }
        for (beat in beatDividers) {
            beat.translateY = new
        }
        for (tick in ticksForMasterTrack) {
            tick.translateY = new - trackListViewModel.trackHeight/2.0 + tick.height/2.0 + 1.0
        }
    }

    override fun respondToWidthChange(old: Double, new: Double) {
        val change = (new - old)/2.0
        trackRectangle.translateX -= change
        for (measureDivider in measureDividers) {
            measureDivider.translateX -= change
        }
        for (beat in beatDividers) {
            beat.translateX -= change
        }
        for (tick in ticksForMasterTrack) {
            tick.translateX -= change
        }
    }

    fun respondToDividerShift(newValue: Double) {
        val oldTranslate = trackRectangle.translateX
        trackRectangle.translateX = waveFormWidth / 2.0 + newValue
        val change = trackRectangle.translateX - oldTranslate
        for (measureDivider in measureDividers) {
            measureDivider.translateX += change
        }
        for (beat in beatDividers) {
            beat.translateX += change
        }
        for (tick in ticksForMasterTrack) {
            tick.translateX += change
        }
    }
}