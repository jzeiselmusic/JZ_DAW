package org.jzeisel.app_test.component.trackBar.tracks

import javafx.application.Platform
import javafx.scene.layout.StackPane
import javafx.scene.paint.Color
import javafx.scene.shape.Polygon
import javafx.scene.shape.Rectangle
import javafx.scene.shape.StrokeLineJoin
import org.jzeisel.app_test.component.Widget
import org.jzeisel.app_test.component.trackBar.smallComponents.AddButton
import org.jzeisel.app_test.component.trackBar.smallComponents.InputEnableButton
import org.jzeisel.app_test.component.vuMeter.VUMeter
import org.jzeisel.app_test.logger.Logger

class NormalTrack(root: StackPane, override val parent: Widget, override val name: String)
    : Track(root, parent), Widget {
    companion object {
        const val TAG = "NormalTrack"
        const val LEVEL = 1
    }

    /* same as a master track except its y offset will change */
    override var trackOffsetY = trackList.masterOffsetY +
            trackList.getNumTracks() * trackList.trackHeight
    override val children = mutableListOf<Widget>()
    init {
        setTrackRectangleProperties()
        /* all tracks have the same width and height changes */
        trackList.stageWidthProperty.addListener { _, _, newWidth ->
            trackRectangle.width = newWidth as Double

        }
        trackList.stageHeightProperty.addListener { _, old, newHeight ->
            trackRectangle.translateY -= (newHeight as Double - old as Double) / 2.0
        }
        Logger.debug(TAG, "instantiated: y-offset $trackOffsetY", LEVEL)
    }
    override val vuMeter = VUMeter(this)
    override val addButton = AddButton(root, this)
    override val inputEnableButton = InputEnableButton(root, this)
    val inputSelectRectangle = Rectangle(15.0, 15.0, Color.MEDIUMPURPLE.brighter())
    val inputSelectArrow = Polygon(0.0, 0.0,
                                   8.0, 0.0,
                                   4.0, -4.0)
    init {
        trackList.stageWidthProperty.addListener { _, old, new ->
            inputSelectRectangle.translateX -= (new as Double - old as Double)/2.0
            inputSelectArrow.translateX -= (new as Double - old as Double)/2.0
        }
        trackList.stageHeightProperty.addListener {_, old, new ->
            inputSelectRectangle.translateY -= (new as Double - old as Double)/2.0
            inputSelectArrow.translateY -= (new as Double - old as Double)/2.0
        }
        inputSelectRectangle.translateX = -(trackRectangle.width/2.0) + 60.0
        inputSelectRectangle.translateY = trackOffsetY + 10.0
        inputSelectRectangle.arcWidth = 5.0
        inputSelectRectangle.arcHeight = 5.0
        inputSelectRectangle.stroke = Color.BLACK
        inputSelectRectangle.strokeWidth = 1.6

        inputSelectArrow.fill = Color.BLACK
        inputSelectArrow.translateX = -(trackRectangle.width/2.0) + 59.5
        inputSelectArrow.translateY = trackOffsetY + 10.0
        inputSelectArrow.rotate = 180.0
        inputSelectArrow.stroke = Color.BLACK
        inputSelectArrow.strokeWidth = 1.0
        inputSelectArrow.strokeLineJoin = StrokeLineJoin.ROUND
    }

    override fun addMeToScene(root: StackPane) {
        root.children.add(trackRectangle)
        root.children.add(inputSelectRectangle)
        root.children.add(inputSelectArrow)
        vuMeter.addMeToScene(root)
        addButton.addMeToScene(root)
        inputEnableButton.addMeToScene(root)
        addChild(vuMeter)
        addChild(addButton)
        addChild(inputEnableButton)
    }

    override fun addChild(child: Widget) {
        children.add(child)
    }

    override fun removeMeFromScene(root: StackPane) {
        Platform.runLater {
            for (child in children) {
                child.removeMeFromScene(root)
            }
            children.clear()
            root.children.remove(trackRectangle)
        }
    }

    /* a normal track is able to arm audio for recording.
       this means starting an audio stream to this track */
    fun audioInputEnable() {

    }

    fun audioInputDisable() {
    }
}