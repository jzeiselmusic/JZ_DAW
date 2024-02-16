package org.jzeisel.app_test.components.trackBar.tracks

import javafx.event.EventHandler
import javafx.scene.Cursor
import javafx.scene.input.KeyEvent
import javafx.scene.layout.StackPane
import javafx.scene.paint.Color
import javafx.scene.shape.Rectangle
import javafx.scene.shape.StrokeLineJoin
import javafx.scene.text.Text
import org.jzeisel.app_test.TrackListViewModel
import org.jzeisel.app_test.components.Widget
import org.jzeisel.app_test.components.trackBar.smallComponents.*
import org.jzeisel.app_test.components.vuMeter.VUMeter
import org.jzeisel.app_test.logger.Logger
import org.jzeisel.app_test.util.ObservableListener
import kotlin.math.abs

abstract class Track(val root: StackPane, parent: Widget) : ObservableListener<Double> {
    val TAG = "TrackInterface"
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
    val initialTrackWidth = trackListViewModel.stage.width
    val initialTrackHeight = trackListViewModel.trackHeight
    val trackColorNormal = Color.WHITESMOKE.darker().darker()
    val trackColorHL = Color.WHITESMOKE.darker()
    val trackRectangle = Rectangle(initialTrackWidth, initialTrackHeight, trackColorNormal)
    val trackDivider = Rectangle(3.0, initialTrackHeight, trackListViewModel.strokeColor)
    val labelDivider = Rectangle(1.5, initialTrackHeight, trackListViewModel.strokeColor)
    val trackLabel = Rectangle()
    val trackLabelNumber = Text("")
    abstract var trackOffsetY: Double
    abstract val addButton: AddButton
    abstract val vuMeter: VUMeter
    abstract val inputEnableButton: InputEnableButton
    abstract val inputSelectArrow: InputSelectArrow
    abstract val waveFormBox: WaveFormBox
    abstract val inputNameBox: InputNameBox
    abstract val volumeSlider: VolumeSlider
    abstract val name: String

    fun setTrackRectangleProperties() {
        trackRectangle.translateY = trackOffsetY
        trackRectangle.stroke = trackListViewModel.strokeColor
        trackRectangle.strokeWidth = 0.4
        trackRectangle.strokeLineJoin = StrokeLineJoin.MITER

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
        trackDivider.translateX = trackListViewModel.currentDividerOffset.getValue()
        trackDivider.cursor = Cursor.H_RESIZE

        trackDivider.onMouseDragged = EventHandler {
            trackDivider.translateX += it.x
            trackListViewModel.currentDividerOffset.setValueAndNotify(trackDivider.translateX)
        }

        labelDivider.translateY = trackOffsetY
        labelDivider.translateX = trackListViewModel.labelDividerOffset

        trackLabel.width = trackListViewModel.stageWidthProperty.value / 2.0 - abs(labelDivider.translateX)
        trackLabel.height = initialTrackHeight
        trackLabel.fill = trackListViewModel.generalGray
        trackLabel.translateY = trackOffsetY
        trackLabel.translateX = -trackListViewModel.stageWidthProperty.value / 2.0 + trackLabel.width / 2.0
        trackLabel.stroke = trackListViewModel.strokeColor
        trackLabel.strokeWidth = 0.4
        trackLabelNumber.translateY = trackOffsetY
        trackLabelNumber.translateX = trackLabel.translateX
    }

    abstract fun addTrack()
    abstract fun backspaceText()
    abstract fun characterText(character: KeyEvent)
}