package org.jzeisel.app_test.components

interface TrackComponentWidget {
    fun respondToOffsetYChange(old: Double, new: Double)

    fun respondToWidthChange(old: Double, new: Double)
}