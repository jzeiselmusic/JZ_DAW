package org.jzeisel.app_test.component.trackBar.tracks

import javafx.event.EventHandler
import javafx.scene.Cursor
import javafx.scene.input.MouseEvent
import javafx.scene.layout.StackPane
import javafx.scene.paint.Color
import javafx.scene.shape.Rectangle
import javafx.scene.shape.StrokeLineJoin
import org.jzeisel.app_test.component.Widget
import org.jzeisel.app_test.component.trackBar.smallComponents.AddButton
import org.jzeisel.app_test.component.trackBar.smallComponents.InputEnableButton
import org.jzeisel.app_test.component.trackBar.smallComponents.InputSelectArrow
import org.jzeisel.app_test.component.vuMeter.VUMeter

abstract class Track(val root: StackPane, parent: Widget) {
    /* a track component has the following elements:
        1. a vuMeter
        2. a waveform box
        3. a panning component
        4. a volume component
        5. mute and solo buttons
        5. an input selector dropbox
        6. a button to add a new track
        7. a recorder object to save audio input
           when the master wants to record
     */
    val trackListViewModel = parent as TrackListViewModel
    val trackWidth = trackListViewModel.stage.width
    val trackHeight = trackListViewModel.trackHeight
    val trackColorNormal = Color.WHITESMOKE.darker().darker()
    val trackColorHL = Color.WHITESMOKE.darker()
    val trackRectangle = Rectangle(trackWidth, trackHeight, trackColorNormal)
    val trackDivider = Rectangle(3.0, trackHeight, Color.BLACK)
    val initialDividerOffset = trackListViewModel.currentDividerOffset
    abstract var trackOffsetY: Double
    abstract val addButton: AddButton
    abstract val vuMeter: VUMeter
    // abstract val recorder: Recorder
    abstract val inputEnableButton: InputEnableButton
    abstract val inputSelectArrow: InputSelectArrow
    abstract val waveFormBox: WaveFormBox
    abstract val name: String

    fun setTrackRectangleProperties() {
        trackRectangle.translateY = trackOffsetY
        trackRectangle.stroke = Color.BLACK
        trackRectangle.strokeWidth = 1.0
        trackRectangle.strokeLineJoin = StrokeLineJoin.ROUND
        trackRectangle.onMouseEntered = EventHandler {
            trackRectangle.fill = trackColorHL
        }
        trackRectangle.onMouseExited = EventHandler {
            trackRectangle.fill = trackColorNormal
        }
        trackRectangle.onMousePressed = EventHandler {
            trackRectangle.opacity = 0.7
        }
        trackRectangle.onMouseReleased = EventHandler {
            trackRectangle.opacity = 1.0
        }

        trackDivider.translateY = trackOffsetY
        trackDivider.translateX = initialDividerOffset
        trackDivider.cursor = Cursor.H_RESIZE
    }
}