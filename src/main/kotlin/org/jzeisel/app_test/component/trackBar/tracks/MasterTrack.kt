package org.jzeisel.app_test.component.trackBar.tracks

import javafx.application.Platform
import javafx.scene.layout.StackPane
import javafx.scene.paint.Color
import javafx.scene.shape.Rectangle
import org.jzeisel.app_test.component.Widget
import org.jzeisel.app_test.component.trackBar.smallComponents.AddButton
import org.jzeisel.app_test.component.trackBar.smallComponents.InputEnableButton
import org.jzeisel.app_test.component.trackBar.smallComponents.InputSelectArrow
import org.jzeisel.app_test.component.vuMeter.VUMeter
import org.jzeisel.app_test.logger.Logger

class MasterTrack(root: StackPane, override val parent: Widget)
    : Track(root, parent), Widget {
    companion object {
        const val TAG = "MasterTrack"
        const val LEVEL = 1
    }
    override val name = "Master"
    override val children = mutableListOf<Widget>()
    override var trackOffsetY = trackList.masterOffsetY
    init {
        setTrackRectangleProperties()
        /* all tracks have the same width and height changes */
        trackList.stageWidthProperty.addListener { _, _, newWidth ->
            trackRectangle.width = newWidth as Double
        }
        trackList.stageHeightProperty.addListener { _, old, newHeight ->
            trackRectangle.translateY -= (newHeight as Double - old as Double) / 2.0
            trackList.masterOffsetY = trackRectangle.translateY
        }
        Logger.debug(TAG, "instantiated: y-offset $trackOffsetY", LEVEL)
    }
    override val addButton = AddButton(this)
    override val inputEnableButton = InputEnableButton(this)
    override val vuMeter = VUMeter(this)
    override val inputSelectArrow = InputSelectArrow(root, this)

    override fun addChild(child: Widget) {
        children.add(child)
    }

    override fun addMeToScene(root: StackPane) {
        root.children.add(trackRectangle)
        vuMeter.addMeToScene(root)
        addButton.addMeToScene(root)
        addChild(vuMeter)
        addChild(addButton)
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
}