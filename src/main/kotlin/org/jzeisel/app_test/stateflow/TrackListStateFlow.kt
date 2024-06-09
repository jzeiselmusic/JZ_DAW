package org.jzeisel.app_test.stateflow

import javafx.beans.property.ReadOnlyDoubleProperty
import javafx.scene.paint.Color
import org.jzeisel.app_test.components.Track
import org.jzeisel.app_test.error.PanicErrorMessage
import org.jzeisel.app_test.util.Observable

data class TrackListState(
    val stageWidthProperty: ReadOnlyDoubleProperty,
    val stageHeightProperty: ReadOnlyDoubleProperty,
    val trackHeight: Double = 100.0,
    val trackWidth: Double = stageWidthProperty.value,
    /* sizes */
    val separationDistance: Double = 45.0,
    val inputNameBoxWidth: Double = separationDistance * 2.0,
    val widgetSize: Double = 20.0,
    val vuMeterWidth: Double = widgetSize,
    val buttonSize: Double = widgetSize,
    val arcSize: Double = 5.0,
    val strokeSize: Double = 1.2,
    val verticalDistancesBetweenWidgets: Double = 15.0,
    val waveFormWidth: Double  = 5000.0,
    val initialTrackDividerWidth: Double = 350.0,
    val recordButtonWidth: Double = 12.5,
    /* colors */
    val strokeColor: Color = Color.BLACK,
    val generalPurple: Color = Color.MEDIUMPURPLE.darker(),
    val generalGray: Color = Color.GRAY.brighter(),
    val backgroundGray: Color = Color.DIMGREY.darker().darker(),
    /* initial offsets */
    val masterOffsetY: Double = -(stageHeightProperty.value / 2.0) + (trackHeight / 2.0) + 12.0,
    val addButtonOffset: Double = separationDistance,
    val recordButtonOffset: Double = addButtonOffset + 35.0,
    val inputButtonsOffset: Double = recordButtonOffset + 35.0,
    val soloButtonOffset: Double = inputButtonsOffset + 35.0,
    val muteButtonOffset: Double = inputButtonsOffset + 35.0,
    val inputNameBoxOffset: Double = soloButtonOffset + separationDistance + inputNameBoxWidth / 2.0 - vuMeterWidth / 2.0,
    val vuMeterOffset: Double  = inputNameBoxOffset + separationDistance + inputNameBoxWidth / 2.0 - vuMeterWidth / 2.0,
    // val recordButtonOffset: Double = vuMeterOffset + separationDistance + vuMeterWidth / 2.0 - recordButtonWidth / 2.0 - 6.0,
    val labelDividerOffset: Double = -stageWidthProperty.value / 2.0 + 20.0,
    val waveFormOffset: Double = 0.0,
    /* observable variables */
    val currentDividerOffset: Observable<Double> = Observable(-stageWidthProperty.value / 2.0 + initialTrackDividerWidth),
    val observableStageWidth: Observable<Double> = Observable(stageWidthProperty.value),
    val observableStageHeight: Observable<Double> = Observable(stageHeightProperty.value),
    val waveFormScrollDeltaX: Observable<Double> = Observable(0.0),
    val numTracks: Int = 0,
    val trackSelected: Track? = null,
    val panicErrorMessage: PanicErrorMessage? = null,

    val pixelsInABeat: Double = 25.0,
    val playBackStarted: Boolean = false,
    val cursorOffset: Double = 0.0, // pixel distance from start of track
    val savedCursorPositionOffset: Double = 0.0,

    val soloEngaged: Boolean = false
)

class TrackListStateFlow(stageWidthProperty: ReadOnlyDoubleProperty, stageHeightProperty: ReadOnlyDoubleProperty) {
    var state = TrackListState(stageWidthProperty, stageHeightProperty) // read only state
    /* state can be updated by using the "copy" method */

    val numTracks: Int get() { return state.numTracks + 1 }
    val totalHeightOfAllTracks: Double get() { return bottomOfTracks - topOfTracks }
    val topOfTracks: Double get() { return state.masterOffsetY - state.trackHeight / 2.0 }
    val bottomOfTracks: Double get() { return topOfTracks + state.trackHeight*numTracks }
    val waveFormTranslateX: Double get() { return state.waveFormWidth / 2.0 + state.currentDividerOffset.getValue() }
}