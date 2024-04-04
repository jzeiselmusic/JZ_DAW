package org.jzeisel.app_test.components.interfaces

import org.jzeisel.app_test.util.ObservableListener

interface WindowElement: ObservableListener<Double> {
    fun respondToHeightChange(old: Double, new: Double)
    fun respondToWidthChange(old: Double, new: Double)
}