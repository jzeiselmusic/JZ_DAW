package org.jzeisel.app_test.component.trackBar.tracks

import javafx.application.Platform
import javafx.scene.layout.StackPane
import javafx.scene.paint.Color
import javafx.scene.shape.Rectangle
import org.jzeisel.app_test.component.Widget
import org.jzeisel.app_test.component.trackBar.smallComponents.AddButton
import org.jzeisel.app_test.component.trackBar.smallComponents.InputEnableButton
import org.jzeisel.app_test.component.vuMeter.VUMeter

class MasterTrack(root: StackPane, override val parent: Widget)
    : Track(root, parent), Widget {

    override val children = mutableListOf<Widget>()
    override val trackOffsetY = trackList.getMasterOffsetY()
    override val addButton = AddButton(root, trackOffsetY, this)
    override val inputEnableButton = InputEnableButton(root, trackOffsetY, this)
    override val vuMeter = VUMeter(this)

    override fun addChild(child: Widget) {
        children.add(child)
    }

    override fun addMeToScene(root: StackPane) {
        setTrackRectangleProperties()
        root.children.add(trackRectangle)
        vuMeter.addMeToScene(root)
        addButton.addMeToScene(root)
        inputEnableButton.addMeToScene(root)
        addChild(inputEnableButton)
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