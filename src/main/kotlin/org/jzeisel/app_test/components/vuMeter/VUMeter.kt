package org.jzeisel.app_test.components.vuMeter

import javafx.application.Platform
import javafx.scene.layout.StackPane
import javafx.scene.paint.Color
import javafx.scene.shape.Rectangle
import org.jzeisel.app_test.audio.AudioInputManager
import org.jzeisel.app_test.components.TrackComponentWidget
import org.jzeisel.app_test.components.Widget
import org.jzeisel.app_test.components.trackBar.tracks.Track
import org.jzeisel.app_test.logger.Logger

class VUMeter(override val parent: Widget): Widget, TrackComponentWidget {
    companion object {
        const val TAG = "VUMeter"
        const val LEVEL = 3
    }
    /* object that represents a single VUMeter */
    /* made of 2 rectangles and a set of numBars Bars */
    private val parentTrack = parent as Track
    private val trackListViewModel = parentTrack.trackListViewModel
    private val numBars = 20
    val vuMeterWidth = 20.0
    private var vuMeterHeight = parentTrack.initialTrackHeight / 1.75
    var vuMeterOffsetX = -(parentTrack.trackListViewModel.stage.width / 2.0) + trackListViewModel.vuMeterOffset
    private var vuMeterOffsetY = parentTrack.trackOffsetY
    private val bgColor = Color.GRAY.brighter()
    private val barSep = 0.0
    private val volumePerBar = 3000.0 / numBars

    private val vuMeterRectangle = Rectangle(vuMeterWidth, vuMeterHeight, bgColor)
    val barHeight = (vuMeterHeight - (barSep * numBars + 2)) / numBars

    override val children = mutableListOf<Widget>()

    init {
        Logger.debug(TAG, "instantiated: parent is ${parentTrack.name}", LEVEL)
        Logger.debug(TAG, "\t y-offset is $vuMeterOffsetY", LEVEL)
    }

    override fun addMeToScene(root: StackPane) {
        Logger.debug(TAG, "adding to scene: y-offset is ${parentTrack.trackOffsetY}", LEVEL)
        vuMeterRectangle.translateX = vuMeterOffsetX
        vuMeterRectangle.translateY = vuMeterOffsetY
        vuMeterRectangle.arcWidth = 5.0
        vuMeterRectangle.arcHeight = 5.0
        vuMeterRectangle.stroke = Color.BLACK
        vuMeterRectangle.strokeWidth = 1.5
        root.children.add(vuMeterRectangle)
        makeMeterBars(root)
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
        Platform.runLater {
            for (bar in children) {
                bar.addMeToScene(root)
                /* initially make invisible */
                (bar as VUBar).isVisible(false)
            }
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
        bar.bringToFront()
    }

    fun setBarsBasedOnAudio(audioInputManager: AudioInputManager, index: Int) {
        val mean = audioInputManager.dataStreams[index]
        Platform.runLater {
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

    override fun respondToOffsetYChange(old: Double, new: Double) {
        val newY = vuMeterRectangle.translateY + (new - old)
        vuMeterRectangle.translateY = newY
        vuMeterOffsetY = newY
        for (bar in children) {
            (bar as VUBar).updateOffsetY(
                    vuMeterOffsetY + ((vuMeterHeight / 2) - barSep - barHeight /2)
                            - (children.indexOf(bar) * (barHeight + barSep)))
        }
    }

    override fun respondToWidthChange(old: Double, new: Double) {
        val newX = vuMeterRectangle.translateX - (new - old)/2.0
        vuMeterRectangle.translateX = newX
        vuMeterOffsetX = newX
        for (bar in children) {
            (bar as VUBar).updateOffsetX(newX)
        }
    }
}