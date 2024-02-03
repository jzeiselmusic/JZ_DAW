package org.jzeisel.app_test.components.trackBar.tracks

import javafx.application.Platform
import javafx.scene.layout.StackPane
import org.jzeisel.app_test.components.Widget
import org.jzeisel.app_test.components.trackBar.smallComponents.AddButton
import org.jzeisel.app_test.components.trackBar.smallComponents.InputEnableButton
import org.jzeisel.app_test.components.trackBar.smallComponents.InputSelectArrow
import org.jzeisel.app_test.components.vuMeter.VUMeter
import org.jzeisel.app_test.logger.Logger

class NormalTrack(root: StackPane, override val parent: Widget, override val name: String)
    : Track(root, parent), Widget {
    companion object {
        const val TAG = "NormalTrack"
        const val LEVEL = 1
    }

    /* same as a master track except its y offset will change */
    override var trackOffsetY = trackListViewModel.masterOffsetY +
            trackListViewModel.getNumTracks() * trackListViewModel.trackHeight
    override val children = mutableListOf<Widget>()
    init {
        setTrackRectangleProperties()
        trackLabelNumber.text = (name.toInt() + 1).toString()
        /* all tracks have the same width and height changes */
        trackListViewModel.stageWidthProperty.addListener { _, old, new ->
            val amtChange = (new as Double - old as Double) / 2.0
            trackRectangle.width = new
            trackDivider.translateX -= amtChange
            trackListViewModel.currentDividerOffset.setValue(trackDivider.translateX)
            labelDivider.translateX -= amtChange
            trackListViewModel.labelDividerOffset = labelDivider.translateX
            trackLabel.translateX -= amtChange
            trackLabelNumber.translateX -= amtChange
        }
        trackListViewModel.stageHeightProperty.addListener { _, old, new ->
            val amtChange = (new as Double - old as Double) / 2.0
            trackRectangle.translateY -= amtChange
            trackDivider.translateY -= amtChange
            labelDivider.translateY -= amtChange
            trackLabel.translateY -= amtChange
            trackOffsetY = trackRectangle.translateY
            trackLabelNumber.translateY -= amtChange
        }
        Logger.debug(TAG, "instantiated: y-offset $trackOffsetY", LEVEL)
    }
    override val vuMeter = VUMeter(this)
    override val addButton = AddButton(this)
    override val inputEnableButton = InputEnableButton(this)
    override val inputSelectArrow = InputSelectArrow(root, this)
    override val waveFormBox = WaveFormBox(this)
    override fun respondToChange(observable: Any, value: Double, grow: Boolean) {
        when (observable) {
            trackListViewModel.currentDividerOffset -> {
                trackDivider.translateX = value
                waveFormBox.respondToDividerShift(value)
            }
            trackListViewModel.children -> {
                if (grow)
                    Logger.debug(TAG, "track $name notified: track added at $value", LEVEL)
                else
                    Logger.debug(TAG, "track $name notified: track removed at $value", LEVEL)
            }
        }
    }

    override fun addMeToScene(root: StackPane) {
        root.children.add(trackRectangle)
        root.children.add(trackDivider)
        root.children.add(trackLabel)
        root.children.add(labelDivider)
        root.children.add(trackLabelNumber)
        vuMeter.addMeToScene(root)
        addButton.addMeToScene(root)
        inputEnableButton.addMeToScene(root)
        inputSelectArrow.addMeToScene(root)
        waveFormBox.addMeToScene(root)
        addChild(vuMeter)
        addChild(addButton)
        addChild(inputEnableButton)
        addChild(inputSelectArrow)
        addChild(waveFormBox)
    }

    var audioInputIndex: Int? = null
    var audioInputEnabled = false

    override fun addChild(child: Widget) {
        children.add(child)
    }

    override fun removeMeFromScene(root: StackPane) {
        Platform.runLater {
            for (child in children) {
                child.removeMeFromScene(root)
            }
            children.clear()
            root.children.remove(trackLabelNumber)
            root.children.remove(trackDivider)
            root.children.remove(trackRectangle)
            root.children.remove(trackLabel)
            root.children.remove(labelDivider)
        }
    }

    fun audioInputEnable(): Boolean {
        val result = trackListViewModel.setTrackEnabled(this)
        if (result) {
            audioInputEnabled = true
            return true
        }
        else {
            return false
        }
    }

    fun audioInputDisable() {
        trackListViewModel.setTrackDisabled(this)
        audioInputEnabled = false
    }

    fun setAudioInputIndex(index: Int) {
        audioInputIndex = index
        trackListViewModel.setTrackAudioInput(index, this)
    }

    fun startGettingDataForVuMeter() {
    }
}