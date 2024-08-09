package org.jzeisel.app_test.stateflow

import javafx.beans.property.ReadOnlyDoubleProperty

data class MixerState(
    val paneWidthProperty: ReadOnlyDoubleProperty,
    val paneHeightProperty: ReadOnlyDoubleProperty,

    val numFaders: Int = 0
)

class MixerStateFlow(
    paneWidthProperty: ReadOnlyDoubleProperty,
    paneHeightProperty: ReadOnlyDoubleProperty) {

    var state = MixerState(paneWidthProperty, paneHeightProperty)
    val numNormalFaders: Int get() { return state.numFaders + 1 }
}