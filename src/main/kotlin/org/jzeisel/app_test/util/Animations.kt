package org.jzeisel.app_test.util

import javafx.animation.PauseTransition
import javafx.animation.ScaleTransition
import javafx.application.Platform
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

fun runLater(millis: Double = 0.0, r: Runnable) {
    val delay = PauseTransition(Duration.millis(millis))
    Platform.runLater {
        delay.setOnFinished { r.run() }
        delay.play()
    }
}

const val viewOrderFlip = 5.0