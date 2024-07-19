package org.jzeisel.app_test.util

import javafx.animation.FillTransition
import javafx.animation.Interpolator
import javafx.animation.PauseTransition
import javafx.animation.ScaleTransition
import javafx.application.Platform
import javafx.event.EventHandler
import javafx.scene.paint.Color
import javafx.scene.shape.Shape
import javafx.util.Duration
import kotlinx.coroutines.delay
import java.lang.Math.pow
import kotlin.math.*
import kotlin.time.toDuration


fun animateObjectScale(from: Double, to: Double, obj: Shape, duration: Double = 100.0) {
    val animate = ScaleTransition(Duration.millis(duration), obj)
    animate.fromX = from
    animate.fromY = from
    animate.toX = to
    animate.toY = to
    animate.cycleCount = 1
    animate.interpolator = Interpolator.EASE_IN
    animate.play()
}

fun animateObjectColor(from: Color, to: Color, obj: Shape, duration: Double = 100.0) {
    var animate = FillTransition(Duration.millis(5.0), obj)
    animate.fromValue = from
    animate.toValue = to
    animate.cycleCount = 1
    animate.interpolator = Interpolator.EASE_IN
    animate.onFinished = EventHandler {
        animate = FillTransition(Duration.millis(duration), obj)
        animate.fromValue = to
        animate.toValue = from
        animate.cycleCount = 1
        animate.interpolator = Interpolator.EASE_IN
        animate.play()
    }
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

fun scaleNumber(input: Double, minOuput: Double, maxOuput: Double, minInput: Double, maxInput: Double): Double {
    val scaledInput = input.coerceIn(minInput..maxInput)
    val scaledValue = minOuput + (scaledInput - minInput) * (maxOuput - minOuput) / (maxInput - minInput)
    return scaledValue
}

fun scaleNumberLogarithmic(input: Double, minOutput: Double, maxOutput: Double, minInput: Double, maxInput: Double, base: Double = 10.0): Double {
    val scaledInput = input.coerceIn(minInput..maxInput)

    val normalizedInput = (scaledInput - minInput) / (maxInput - minInput)
    val logMin = minOutput
    val logMax = maxOutput
    val logScaled = logMin + normalizedInput * (logMax - logMin)
    return logScaled
}

fun combineRmsVolumes(vararg volumes: Double): Double {
    return sqrt(volumes.sumOf { it.pow(2) } / volumes.size)
}

fun quantizeNumber(value: Double, startingOffsetX: Double, increment: Double): Double {
    val adjustedValue = value - startingOffsetX
    val quantizedValue = increment * (adjustedValue / increment).roundToInt()
    return startingOffsetX + quantizedValue
}

fun samplesToPixels(samples: Int, tempo: Double, sampleRate: Int, pixelsPerBeat: Double): Double {
    val secondsInABeat = 1.0 / (tempo * (1.0 / 60.0))
    val pixels = samples * pixelsPerBeat * (1.0 / secondsInABeat) * (1.0 / sampleRate)
    return pixels
}

fun pixelsToSamples(pixels: Double, tempo: Double, sampleRate: Int, pixelsPerBeat: Double): Int {
    val secondsInABeat = 1.0 / (tempo * (1.0 / 60.0))
    val samples = pixels / (pixelsPerBeat * (1.0 / secondsInABeat) * (1.0 / sampleRate))
    return samples.roundToInt()
}

fun envelopeFollower(input: Double, attack: Double, release: Double, prevValue: Double): Double {
    val attackAlpha: Double = 1.0 - Math.E.pow(-1.0 / (attack * 10.0))
    val releaseAlpha: Double = 1.0 - Math.E.pow(-1.0 / (release * 10.0))
    val envelope =
        if (input > prevValue)
            attackAlpha * input + (1 - attackAlpha) * prevValue
        else
            releaseAlpha * input + (1 - releaseAlpha) * prevValue
    return envelope
}