package org.jzeisel.app_test.components.trackBar.tracks

import javafx.application.Platform
import javafx.scene.layout.StackPane
import org.jzeisel.app_test.components.Widget
import org.jzeisel.app_test.components.trackBar.smallComponents.AddButton
import org.jzeisel.app_test.components.trackBar.smallComponents.InputEnableButton
import org.jzeisel.app_test.components.trackBar.smallComponents.InputSelectArrow
import org.jzeisel.app_test.components.vuMeter.VUMeter
import org.jzeisel.app_test.logger.Logger

class MasterTrack(root: StackPane, override val parent: Widget)
    : Track(root, parent), Widget {
    companion object {
        const val TAG = "MasterTrack"
        const val LEVEL = 1
    }
    override val name = "Master"

    override val children = mutableListOf<Widget>()
    override var trackOffsetY = trackListViewModel.masterOffsetY
    init {
        setTrackRectangleProperties()
        /* all tracks have the same width and height changes */
        trackListViewModel.stageWidthProperty.addListener { _, old, new ->
            val amtChange = (new as Double - old as Double) / 2.0
            trackRectangle.width = new
            trackDivider.translateX -= amtChange
            trackListViewModel.currentDividerOffset.setValue(trackDivider.translateX)
            trackLabel.translateX -= amtChange
            labelDivider.translateX -= amtChange
            trackListViewModel.labelDividerOffset = labelDivider.translateX
        }
        trackListViewModel.stageHeightProperty.addListener { _, old, new ->
            val amtChange = (new as Double - old as Double) / 2.0
            trackRectangle.translateY -= amtChange
            trackListViewModel.masterOffsetY = trackRectangle.translateY
            trackDivider.translateY -= amtChange
            trackLabel.translateY -= amtChange
            labelDivider.translateY -= amtChange
        }
        Logger.debug(TAG, "instantiated: y-offset $trackOffsetY", LEVEL)
    }
    override val addButton = AddButton(this)
    override val inputEnableButton = InputEnableButton(this)
    override val vuMeter = VUMeter(this)
    override val inputSelectArrow = InputSelectArrow(root, this)
    override val waveFormBox = WaveFormBox(this)

    override fun respondToChange(observable: Any, value: Double, grow: Boolean) {
        when (observable) {
            trackListViewModel.currentDividerOffset -> {
                trackDivider.translateX = value
                waveFormBox.respondToDividerShift(value)
            }
        }
    }

    override fun addChild(child: Widget) {
        children.add(child)
    }

    override fun addTrack() {
        trackListViewModel.addTrackFromMaster()
    }

    override fun addMeToScene(root: StackPane) {
        root.children.add(trackRectangle)
        root.children.add(trackDivider)
        root.children.add(trackLabel)
        root.children.add(labelDivider)
        vuMeter.addMeToScene(root)
        addButton.addMeToScene(root)
        waveFormBox.addMeToScene(root)
        addChild(vuMeter)
        addChild(addButton)
        addChild(waveFormBox)
    }

    override fun removeMeFromScene(root: StackPane) {
        Platform.runLater {
            for (child in children) {
                child.removeMeFromScene(root)
            }
            children.clear()
            root.children.remove(trackRectangle)
            root.children.remove(trackLabel)
            root.children.remove(labelDivider)
            root.children.remove(trackDivider)
        }
    }
}