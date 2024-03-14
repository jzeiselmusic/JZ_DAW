package org.jzeisel.app_test.util

import javafx.animation.ScaleTransition
import javafx.scene.shape.Shape
import javafx.util.Duration

fun animateObjectScale(from: Double, to: Double, obj: Shape) {
    val animate = ScaleTransition(Duration.millis(100.0), obj)
    animate.fromX = from
    animate.fromY = from
    animate.toX = to
    animate.toY = to
    animate.cycleCount = 1
    animate.play()
}

const val viewOrderFlip = 5.0