package org.jzeisel.app_test.component.vuMeter

import javafx.application.Platform
import javafx.scene.layout.StackPane
import javafx.scene.paint.Color
import javafx.scene.shape.Rectangle
import org.jzeisel.app_test.audio.AudioProcessor
import org.jzeisel.app_test.component.trackBar.tracks.TrackList
import org.jzeisel.app_test.component.Widget
import org.jzeisel.app_test.component.trackBar.tracks.Track

class VUMeter(override val parent: Widget): Widget {
    /* object that represents a single VUMeter */
    /* made of 2 rectangles and a set of numBars Bars */
    private val numBars = 15
    val width = 15.0
    private var height = (parent as Track).trackHeight / 2.0
    var offsetX = -((parent.parent!! as TrackList).stage.width / 2.0) + 120.0
    private var offsetY = (parent as Track).trackOffsetY
    private val color = Color.GRAY.brighter()
    private val barSep = 2.0
    private val volumePerBar = 4000 / numBars

    private val rectangleOne = Rectangle(width, height, color)
    val barHeight = (height - (barSep * numBars + 2)) / numBars

    override val children = mutableListOf<Widget>()

    override fun addMeToScene(root: StackPane) {
        rectangleOne.translateX = offsetX
        rectangleOne.translateY = offsetY
        rectangleOne.arcWidth = 5.0
        rectangleOne.arcHeight = 5.0
        rectangleOne.stroke = Color.BLACK
        rectangleOne.strokeWidth = 1.5
        (parent.parent as TrackList).stageWidthProperty
                .addListener{_, old, new, -> updatePositionOfX(old as Double, new as Double)}
        (parent.parent as TrackList).stageHeightProperty
                .addListener{_, old, new, -> updatePositionOfY(old as Double, new as Double)}
        root.children.add(rectangleOne)
        makeMeterBars(root)
    }

    private fun updatePositionOfX(old: Double, new: Double) {
        rectangleOne.translateX -= (new - old)/2.0
    }

    private fun updatePositionOfY(old: Double, new: Double) {
        rectangleOne.translateY -= (new - old)/2.0
    }

    override fun removeMeFromScene(root: StackPane) {
        Platform.runLater {
            for (child in children) {
                child.removeMeFromScene(root)
            }
            children.clear()
            root.children.remove(rectangleOne)
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
            addChild(VUBar(color, ((height / 2) - barSep - barHeight /2)
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
        val mean = audioUnit.getMeanOfLastNSamples()
        makeAllBarsInvisible()
        for (i in 1 ..numBars) {
            if (mean >= volumePerBar*i) {
                makeBarVisible(children[i-1] as VUBar)
            }
            else {
                break
            }
        }
    }

}