package org.jzeisel.app_test.components.interfaces

import org.jzeisel.app_test.util.ObservableListener

interface WaveElement: ObservableListener<Double> {
    fun respondToScrollChange(deltaX: Double)
}