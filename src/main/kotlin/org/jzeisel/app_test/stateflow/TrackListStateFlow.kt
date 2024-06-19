package org.jzeisel.app_test.stateflow

import javafx.beans.property.ReadOnlyDoubleProperty
import javafx.scene.paint.Color
import org.jzeisel.app_test.components.NormalTrack
import org.jzeisel.app_test.components.Track
import org.jzeisel.app_test.components.trackComponents.WaveFormFile
import org.jzeisel.app_test.error.PanicErrorMessage
import org.jzeisel.app_test.util.Observable
import java.io.File

data class KeyState(
    val shiftPressed: Boolean,
    val textOpen: Boolean,
    val dropDownOpen: Boolean,
    val infoBoxOpen: Boolean,
    val filesHighlighted: FileHighlightGroup
)

data class FileHighlightGroup(
    val files: MutableSet<WaveFormFile> = mutableSetOf(),
    var pressed: Boolean,
    var hasMovedSinceLastPress: Boolean
)

data class PlaybackHighlightSection(
    var pixelStart: Double = 0.0,
    var pixelEnd: Double = 0.0,
    var isEnabled: Boolean = false,
    var loopEnabled: Boolean = false
)

data class TrackListState(
    val stageWidthProperty: ReadOnlyDoubleProperty,
    val stageHeightProperty: ReadOnlyDoubleProperty,
    val trackHeight: Double = 100.0,
    val trackWidth: Double = stageWidthProperty.value,
    /* sizes */
    val separationDistanceLarge: Double = 45.0,
    val separationDistanceSmall: Double = 37.0,
    val inputNameBoxWidth: Double = separationDistanceLarge * 2.0,
    val widgetSize: Double = 20.0,
    val vuMeterWidth: Double = widgetSize,
    val buttonSize: Double = widgetSize,
    val arcSize: Double = 5.0,
    val strokeSize: Double = 1.2,
    val verticalDistancesBetweenWidgets: Double = 15.0,
    val waveFormWidth: Double  = 5000.0,
    val initialTrackDividerWidth: Double = 320.0,
    val recordButtonWidth: Double = 12.5,
    /* colors */
    val strokeColor: Color = Color.BLACK,
    val generalPurple: Color = Color.MEDIUMPURPLE.darker(),
    val generalGray: Color = Color.GRAY.brighter(),
    val backgroundGray: Color = Color.DIMGREY.darker().darker(),
    /* initial offsets */
    val masterOffsetY: Double = -(stageHeightProperty.value / 2.0) + (trackHeight / 2.0) + 12.0,
    val addButtonOffset: Double = separationDistanceLarge,
    val recordButtonOffset: Double = addButtonOffset + separationDistanceLarge - widgetSize/2.0,
    val inputButtonsOffset: Double = recordButtonOffset + separationDistanceLarge - widgetSize/2.0,
    val soloButtonOffset: Double = inputButtonsOffset + separationDistanceSmall - widgetSize/2.0,
    val muteButtonOffset: Double = inputButtonsOffset + separationDistanceSmall - widgetSize/2.0,
    val inputNameBoxOffset: Double = soloButtonOffset + separationDistanceSmall + inputNameBoxWidth / 2.0 - vuMeterWidth / 2.0,
    val vuMeterOffset: Double  = inputNameBoxOffset + separationDistanceSmall + inputNameBoxWidth / 2.0 - vuMeterWidth / 2.0,
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
    val incrementSize: Double = pixelsInABeat / 2.0,
    val playBackStarted: Boolean = false,
    val cursorOffset: Double = 0.0, // pixel distance from start of track
    val savedCursorPositionOffset: Double = 0.0,

    val soloEngaged: Boolean = false,
    val shiftPressed: Boolean = false,
    val textOpen: Boolean = false,
    val dropDownOpen: Boolean = false,
    val infoBoxOpen: Boolean = false,
    val filesHighlighted: FileHighlightGroup = FileHighlightGroup(mutableSetOf(), false, false),

    val playbackHighlightSection: PlaybackHighlightSection = PlaybackHighlightSection()
)

class TrackListStateFlow(stageWidthProperty: ReadOnlyDoubleProperty, stageHeightProperty: ReadOnlyDoubleProperty) {
    var state = TrackListState(stageWidthProperty, stageHeightProperty) // read only state
    /* state shall be updated by using the "copy" method */
    val numTracks: Int get() { return state.numTracks + 1 }
    val totalHeightOfAllTracks: Double get() { return bottomOfTracks - topOfTracks }
    val topOfTracks: Double get() { return state.masterOffsetY - state.trackHeight / 2.0 }
    val bottomOfTracks: Double get() { return topOfTracks + state.trackHeight*numTracks }
    val waveFormTranslateX: Double get() { return state.waveFormWidth / 2.0 + state.currentDividerOffset.getValue() }
}