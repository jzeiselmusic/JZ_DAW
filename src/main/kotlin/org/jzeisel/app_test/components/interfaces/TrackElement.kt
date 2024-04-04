package org.jzeisel.app_test.components.interfaces

import org.jzeisel.app_test.util.ObservableListener

interface TrackElement: ObservableListener<Double> {
    fun respondToIndexChange(old: Double, new: Double)
}