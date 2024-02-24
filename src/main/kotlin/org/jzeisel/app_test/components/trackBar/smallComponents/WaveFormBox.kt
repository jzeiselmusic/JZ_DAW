package org.jzeisel.app_test.components.trackBar.smallComponents

import javafx.application.Platform
import javafx.event.EventHandler
import javafx.scene.layout.StackPane
import javafx.scene.paint.Color
import javafx.scene.shape.Rectangle
import javafx.scene.shape.StrokeLineJoin
import org.jzeisel.app_test.components.TrackComponentWidget
import org.jzeisel.app_test.components.Widget
import org.jzeisel.app_test.components.trackBar.tracks.MasterTrack
import org.jzeisel.app_test.components.trackBar.tracks.NormalTrack
import org.jzeisel.app_test.components.trackBar.tracks.Track
import org.jzeisel.app_test.util.Observable
import org.jzeisel.app_test.util.ObservableListener

class WaveFormBox(override val parent: Widget) :
    Widget, TrackComponentWidget, ObservableListener<Double> {
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
        trackRectangle.onMouseDragged = EventHandler {
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
                tick.stroke = color()
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

    override fun respondToChange(observable: Observable<*>, old: Double, new: Double) {
        when(observable) {
            trackListViewModel.currentDividerOffset -> respondToDividerShift(new)
            trackListViewModel.testStageWidth -> respondToWidthChange(old, new)
            trackListViewModel.testStageHeight -> respondToHeightChange(old, new)
            (parentTrack as NormalTrack).index -> respondToIndexChange(old, new)
        }
    }

    override fun registerForBroadcasts() {
        trackListViewModel.registerForDividerOffsetChanges(this)
        trackListViewModel.registerForWidthChanges(this)
        trackListViewModel.registerForHeightChanges(this)
        if (parentTrack is NormalTrack) {
            parentTrack.registerForIndexChanges(this)
        }
    }

    override fun unregisterForBroadcasts() {
        trackListViewModel.unregisterForHeightChanges(this)
        trackListViewModel.unregisterForDividerOffsetChanges(this)
        trackListViewModel.unregisterForWidthChanges(this)
        if (parentTrack is NormalTrack) {
            parentTrack.unregisterForIndexChanges(this)
        }
    }

    private fun color(): Color? = trackListViewModel.generalGray.darker()
    override fun addChild(child: Widget) {
    }

    override fun addMeToScene(root: StackPane) {
        registerForBroadcasts()
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
            unregisterForBroadcasts()
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

    override fun respondToHeightChange(old: Double, new: Double) {
        ((new - old) /2.0).let {
            trackRectangle.translateY -= it
            for (measureDivider in measureDividers) {
                measureDivider.translateY -= it
            }
            for (beat in beatDividers) {
                beat.translateY -= it
            }
            for (tick in ticksForMasterTrack) {
                tick.translateY -= it
            }
        }
    }

    override fun respondToWidthChange(old: Double, new: Double) {
        ((new - old)/2.0).let {
            trackRectangle.translateX -= it
            for (measureDivider in measureDividers) {
                measureDivider.translateX -= it
            }
            for (beat in beatDividers) {
                beat.translateX -= it
            }
            for (tick in ticksForMasterTrack) {
                tick.translateX -= it
            }
        }
    }

    override fun respondToIndexChange(old: Double, new: Double) {
        trackRectangle.translateY = parentTrack.trackOffsetY
        for (measureDivider in measureDividers) {
            measureDivider.translateY = parentTrack.trackOffsetY
        }
        for (tickDivider in beatDividers) {
            tickDivider.translateY = parentTrack.trackOffsetY
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