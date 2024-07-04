package org.jzeisel.app_test.components.mixerComponents

import javafx.scene.layout.StackPane
import javafx.scene.paint.Color
import javafx.scene.shape.Rectangle
import javafx.scene.text.Font
import javafx.scene.text.FontWeight
import javafx.scene.text.Text
import javafx.scene.text.TextAlignment
import org.jzeisel.app_test.components.interfaces.widget.Widget
import org.jzeisel.app_test.util.Logger
import org.jzeisel.app_test.util.runLater
import org.jzeisel.app_test.util.viewOrderFlip

class TimeDisplay(
    stageWidth: Double,
    toolBarY: Double,
    toolBarHeight: Double): Widget {

    private val displayRectangle = Rectangle()
    private val timeText = Text()
    private val samplesText = Text()

    init {
        displayRectangle.translateX = 0.0
        displayRectangle.translateY = toolBarY
        displayRectangle.fill = Color.BLACK
        displayRectangle.opacity = 0.7
        displayRectangle.arcWidth = 5.0
        displayRectangle.arcHeight = 5.0
        displayRectangle.width = 220.0
        displayRectangle.height = toolBarHeight - 0.8
        displayRectangle.viewOrder = viewOrderFlip - 0.03

        timeText.text = "00:00:00"
        timeText.font = Font("Courier New", 20.0)
        timeText.translateY = displayRectangle.translateY
        timeText.fill = Color.WHITESMOKE
        timeText.viewOrder = viewOrderFlip - 0.04
        timeText.wrappingWidth = Text("00:00:00").apply {
            font = timeText.font
        }.boundsInLocal.width
        timeText.textAlignment = TextAlignment.LEFT
        timeText.translateX = -(displayRectangle.width/2.0 - timeText.boundsInLocal.width/2.0) + 10.0

        samplesText.text = "0000000"
        samplesText.font = Font("Courier New", 20.0)
        samplesText.translateY = displayRectangle.translateY
        samplesText.fill = Color.WHITESMOKE
        samplesText.viewOrder = viewOrderFlip - 0.04
        samplesText.wrappingWidth = Text("00000000").apply {
            font = samplesText.font
        }.boundsInLocal.width
        samplesText.textAlignment = TextAlignment.RIGHT
        samplesText.translateX = (displayRectangle.width/2.0 - samplesText.boundsInLocal.width/2.0) - 10
    }

    fun Int.digitCount(): Int = when {
        this == 0 -> 1
        this < 0 -> (-this).toString().length
        else -> this.toString().length
    }

    fun String.appendZeros(max: Int): String {
        return this.padStart(max, '0')
    }

    override fun addMeToScene(root: StackPane) {
        root.children.addAll(displayRectangle, timeText, samplesText)
    }

    override fun removeMeFromScene(root: StackPane) {
        root.children.removeAll(displayRectangle, timeText, samplesText)
    }

    fun updateStageWidth(new: Double) {

    }

    fun updateTranslateY(new: Double) {
        displayRectangle.translateY = new
        timeText.translateY = displayRectangle.translateY
        samplesText.translateY = displayRectangle.translateY
    }

    fun updateSamples(numSamples: Int, sampleRate: Int) {
        runLater {
            val numDigits = numSamples.digitCount()
            if (numDigits > 7) {
                samplesText.text = (9999999).toString()
            }
            else {
                val text = numSamples.toString()
                samplesText.text = text.appendZeros(7)
            }

            val samplesInAMinute = sampleRate * 60
            val samplesInAnHour = samplesInAMinute * 60

            if (numSamples > samplesInAnHour) {
                timeText.text = "59:59:99"
            }
            else {
                val numMinutes = (numSamples.toDouble() / samplesInAMinute.toDouble()).toInt()
                val numSeconds = ((numSamples.toDouble() - (numMinutes*samplesInAMinute)) / sampleRate.toDouble()).toInt()

                val numCentiseconds = ((numSamples.toDouble() - (numMinutes*samplesInAMinute) - (numSeconds*sampleRate)) / (sampleRate / 100)).toInt()
                val minutesString = numMinutes.toString().appendZeros(2)
                val secondsString = numSeconds.toString().appendZeros(2)
                val centiSecString = numCentiseconds.toString().appendZeros(2)
                timeText.text = "$minutesString:$secondsString:$centiSecString"
            }
        }
    }
}