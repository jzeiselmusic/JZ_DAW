package org.jzeisel.app_test.components

import javafx.event.EventHandler
import javafx.scene.input.KeyEvent
import javafx.scene.layout.StackPane
import javafx.scene.paint.Color
import javafx.scene.shape.Rectangle
import javafx.scene.shape.StrokeLineJoin
import javafx.scene.text.Text
import org.jzeisel.app_test.viewmodel.TrackListViewModel
import org.jzeisel.app_test.components.trackComponents.*
import org.jzeisel.app_test.components.trackComponents.VUMeter
import org.jzeisel.app_test.stateflow.TrackListState
import org.jzeisel.app_test.util.ObservableListener
import org.jzeisel.app_test.util.viewOrderFlip
import kotlin.math.abs

abstract class Track(val root: StackPane, parent: Widget) : ObservableListener<Double> {
    val trackListViewModel = parent as TrackListViewModel
    val trackListState: TrackListState get() { return trackListViewModel._trackListStateFlow.state }
    val initialTrackWidth = trackListState.trackWidth
    val initialTrackHeight = trackListState.trackHeight
    val trackColorNormal = Color.WHITESMOKE.darker().darker()
    val trackColorHL = Color.WHITESMOKE.darker()
    val trackRectangle = Rectangle(trackListState.initialTrackDividerWidth, initialTrackHeight, trackColorNormal)
    val trackDivider = Rectangle(3.0, initialTrackHeight, trackListState.strokeColor)
    val labelDivider = Rectangle(1.5, initialTrackHeight, trackListState.strokeColor)
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
        trackRectangle.translateX = -trackListState.observableStageWidth.getValue() / 2.0 + trackListState.initialTrackDividerWidth / 2.0
        trackRectangle.stroke = trackListState.strokeColor
        trackRectangle.strokeWidth = 0.4
        trackRectangle.strokeLineJoin = StrokeLineJoin.MITER
        trackRectangle.viewOrder = viewOrderFlip - 0.3

        trackRectangle.onMousePressed = EventHandler {
            trackRectangle.opacity = 0.7
        }
        trackRectangle.onMouseReleased = EventHandler {
            trackRectangle.opacity = 1.0
        }

        trackDivider.translateY = trackOffsetY
        trackDivider.translateX = trackListState.currentDividerOffset.getValue()
        trackDivider.viewOrder = viewOrderFlip - 0.41

        labelDivider.translateY = trackOffsetY
        labelDivider.translateX = trackListState.labelDividerOffset
        labelDivider.viewOrder = viewOrderFlip - 0.44
        trackLabel.width = trackListState.stageWidthProperty.value / 2.0 - abs(labelDivider.translateX)
        trackLabel.height = initialTrackHeight
        trackLabel.fill = trackListState.generalGray
        trackLabel.translateY = trackOffsetY
        trackLabel.translateX = -trackListState.stageWidthProperty.value / 2.0 + trackLabel.width / 2.0
        trackLabel.stroke = trackListState.strokeColor
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