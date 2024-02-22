package org.jzeisel.app_test.components

interface TrackComponentWidget {
    fun respondToHeightChange(old: Double, new: Double)

    fun respondToWidthChange(old: Double, new: Double)
}