package org.jzeisel.app_test.util

import javafx.animation.ParallelTransition
import javafx.animation.PathTransition
import javafx.animation.PauseTransition
import javafx.animation.ScaleTransition
import javafx.application.Platform
import javafx.scene.Node
import javafx.scene.shape.MoveTo
import javafx.scene.shape.Path
import javafx.scene.shape.Rectangle
import javafx.scene.shape.Shape
import javafx.util.Duration
import kotlinx.coroutines.delay
import kotlin.math.roundToInt


fun animateObjectScale(from: Double, to: Double, obj: Shape, duration: Double = 100.0) {
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

suspend fun loop(delayMillis: Long, action: () -> Unit) {
    while(true) {
        delay(delayMillis)
        action()
    }
}

const val viewOrderFlip = 5.0

fun scaleNumber(input: Double, max: Double, min: Double): Double {
    val minVolume = -80.0
    val scaledValue: Double
    val scaledInput = input.coerceIn(-80.0..0.0)
    scaledValue = min + (scaledInput - minVolume) * (max - min) / (-minVolume)
    return scaledValue
}

fun quantizeNumber(value: Double, startingOffsetX: Double, increment: Double): Double {
    val adjustedValue = value - startingOffsetX
    val quantizedValue = increment * (adjustedValue / increment).roundToInt()
    return startingOffsetX + quantizedValue
}