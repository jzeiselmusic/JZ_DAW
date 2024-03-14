package org.jzeisel.app_test.components

import javafx.event.EventHandler
import javafx.scene.Cursor
import javafx.scene.input.KeyEvent
import javafx.scene.layout.StackPane
import javafx.scene.paint.Color
import javafx.scene.shape.Rectangle
import javafx.scene.shape.StrokeLineJoin
import javafx.scene.text.Text
import org.jzeisel.app_test.TrackListViewModel
import org.jzeisel.app_test.components.trackComponents.*
import org.jzeisel.app_test.components.trackComponents.VUMeter
import org.jzeisel.app_test.util.BroadcastType
import org.jzeisel.app_test.util.ObservableListener
import org.jzeisel.app_test.util.viewOrderFlip
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
    val trackRectangle = Rectangle(trackListViewModel.initialTrackDividerWidth, initialTrackHeight, trackColorNormal)
    val trackDivider = Rectangle(3.0, initialTrackHeight, trackListViewModel.strokeColor)
    val labelDivider = Rectangle(1.5, initialTrackHeight, trackListViewModel.strokeColor)
    val trackLabel = Rectangle()
    val trackLabelNumber = Text("")
    var isSelected = false
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
        trackRectangle.translateX = -trackListViewModel.observableStageWidth.getValue() / 2.0 + trackListViewModel.initialTrackDividerWidth / 2.0
        trackRectangle.stroke = trackListViewModel.strokeColor
        trackRectangle.strokeWidth = 0.4
        trackRectangle.strokeLineJoin = StrokeLineJoin.MITER
        trackRectangle.viewOrder = viewOrderFlip - 0.3

        trackRectangle.onMouseEntered = EventHandler {
            if (!isSelected)
            trackRectangle.fill = trackColorHL
        }
        trackRectangle.onMouseExited = EventHandler {
            if (!isSelected)
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
        trackDivider.viewOrder = viewOrderFlip - 0.41
        trackDivider.onMouseDragged = EventHandler {
            trackDivider.translateX += it.x
            trackListViewModel.currentDividerOffset.setValueAndNotify(trackDivider.translateX, BroadcastType.DIVIDER)
        }

        labelDivider.translateY = trackOffsetY
        labelDivider.translateX = trackListViewModel.labelDividerOffset
        labelDivider.viewOrder = viewOrderFlip - 0.44
        trackLabel.width = trackListViewModel.stageWidthProperty.value / 2.0 - abs(labelDivider.translateX)
        trackLabel.height = initialTrackHeight
        trackLabel.fill = trackListViewModel.generalGray
        trackLabel.translateY = trackOffsetY
        trackLabel.translateX = -trackListViewModel.stageWidthProperty.value / 2.0 + trackLabel.width / 2.0
        trackLabel.stroke = trackListViewModel.strokeColor
        trackLabel.strokeWidth = 0.4
        trackLabel.viewOrder = viewOrderFlip - 0.42
        trackLabelNumber.translateY = trackOffsetY
        trackLabelNumber.translateX = trackLabel.translateX
        trackLabelNumber.viewOrder = viewOrderFlip - 0.43
    }

    abstract fun addTrack()
    abstract fun backspaceText()
    abstract fun characterText(character: KeyEvent)

    fun setSelected() {
        isSelected = true
        trackRectangle.fill = Color.MEDIUMPURPLE.brighter().brighter().brighter()
    }
}