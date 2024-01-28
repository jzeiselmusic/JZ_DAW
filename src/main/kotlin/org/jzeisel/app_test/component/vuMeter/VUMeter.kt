package org.jzeisel.app_test.component.vuMeter

import javafx.application.Platform
import javafx.scene.layout.StackPane
import javafx.scene.paint.Color
import javafx.scene.shape.Rectangle
import org.jzeisel.app_test.audio.AudioProcessor
import org.jzeisel.app_test.component.trackBar.tracks.TrackList
import org.jzeisel.app_test.component.Widget
import org.jzeisel.app_test.component.trackBar.tracks.Track
import org.jzeisel.app_test.logger.Logger

class VUMeter(override val parent: Widget): Widget {
    companion object {
        const val TAG = "VUMeter"
        const val LEVEL = 3
    }
    /* object that represents a single VUMeter */
    /* made of 2 rectangles and a set of numBars Bars */
    private val numBars = 100
    private var isAudioEnabled = false
    val vuMeterWidth = 15.0
    private var vuMeterHeight = (parent as Track).trackHeight / 2.0
    var vuMeterOffsetX = -((parent.parent as TrackList).stage.width / 2.0) + 120.0
    private var vuMeterOffsetY = (parent as Track).trackOffsetY
    private val bgColor = Color.GRAY.brighter()
    private val barSep = 1.0
    private val volumePerBar = 3000.0 / numBars

    private val vuMeterRectangle = Rectangle(vuMeterWidth, vuMeterHeight, bgColor)
    val barHeight = (vuMeterHeight - (barSep * numBars + 2)) / numBars

    override val children = mutableListOf<Widget>()

    init {
        Logger.debug(TAG, "instantiated: parent is ${(parent as Track).name}", LEVEL)
        Logger.debug(TAG, "\t y-offset is $vuMeterOffsetY", LEVEL)
    }

    override fun addMeToScene(root: StackPane) {
        Logger.debug(TAG, "adding to scene: y-offset is ${(parent as Track).trackOffsetY}", LEVEL)
        vuMeterRectangle.translateX = vuMeterOffsetX
        vuMeterRectangle.translateY = vuMeterOffsetY
        vuMeterRectangle.arcWidth = 5.0
        vuMeterRectangle.arcHeight = 5.0
        vuMeterRectangle.stroke = Color.BLACK
        vuMeterRectangle.strokeWidth = 1.5
        (parent.parent as TrackList).stageWidthProperty
                .addListener{_, old, new, -> updatePositionOfX(old as Double, new as Double)}
        (parent.parent as TrackList).stageHeightProperty
                .addListener{_, old, new, -> updatePositionOfY(old as Double, new as Double)}
        root.children.add(vuMeterRectangle)
        makeMeterBars(root)
    }

    private fun updatePositionOfX(old: Double, new: Double) {
        val newX = vuMeterRectangle.translateX - (new - old)/2.0
        vuMeterRectangle.translateX = newX
        vuMeterOffsetX = newX
        for (bar in 0 until numBars) {
            (children[bar] as VUBar).updateOffsetX(newX)
        }
    }

    private fun updatePositionOfY(old: Double, new: Double) {
        val newY = vuMeterRectangle.translateY - (new - old)/2.0
        vuMeterRectangle.translateY = newY
        vuMeterOffsetY = newY
        for (bar in 0 until numBars) {
            (children[bar] as VUBar).updateOffsetY(
                    vuMeterOffsetY + ((vuMeterHeight / 2) - barSep - barHeight /2)
                        - (bar * (barHeight + barSep)))
        }
    }

    override fun removeMeFromScene(root: StackPane) {
        Platform.runLater {
            for (child in children) {
                child.removeMeFromScene(root)
            }
            children.clear()
            root.children.remove(vuMeterRectangle)
        }
    }

    private fun makeMeterBars(root: StackPane) {
        for (bar in 0 until numBars) {
            val color: Color
            if (bar <= numBars /2) {
                color = Color.GREEN.brighter()
            }
            else if (bar <= 3* numBars /4) {
                color = Color.YELLOW.brighter()
            }
            else color = Color.RED.brighter()
            addChild(VUBar(color, vuMeterOffsetY + ((vuMeterHeight / 2) - barSep - barHeight /2)
                                - (bar * (barHeight + barSep)), this))
        }
        for (bar in children) {
            bar.addMeToScene(root)
            /* initially make invisible */
            (bar as VUBar).isVisible(false)
        }
    }

    override fun addChild(child: Widget) {
        children.add(child)
    }

    private fun makeAllBarsInvisible() {
        for (child in children) {
            (child as VUBar).isVisible(false)
        }
    }

    private fun makeBarVisible(bar: VUBar) {
        bar.isVisible(true)
    }

    fun setBarsBasedOnAudio(audioUnit: AudioProcessor) {
        if (isAudioEnabled) {
            val mean = audioUnit.getMeanOfLastNSamples()
            makeAllBarsInvisible()
            for (i in 1..numBars) {
                if (mean >= volumePerBar * i) {
                    makeBarVisible(children[i - 1] as VUBar)
                } else {
                    break
                }
            }
        }
    }

    fun setAudioEnabled() {
        isAudioEnabled = true
    }

    fun setAudioDisabled() {
        isAudioEnabled = false
        makeAllBarsInvisible()
    }

}