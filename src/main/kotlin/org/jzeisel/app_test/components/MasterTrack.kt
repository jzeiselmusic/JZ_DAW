package org.jzeisel.app_test.components

import javafx.application.Platform
import javafx.scene.input.KeyEvent
import javafx.scene.layout.StackPane
import javafx.scene.shape.Rectangle
import org.jzeisel.app_test.components.trackComponents.*
import org.jzeisel.app_test.components.trackComponents.VUMeter
import org.jzeisel.app_test.util.BroadcastType

class MasterTrack(root: StackPane, override val parent: Widget)
    : Track(root, parent), Widget {

    override val name = "Master"
    override val children = mutableListOf<Widget>()
    override var trackOffsetY: Double = trackListViewModel.masterOffsetY
    var trackWidth: Double = initialTrackWidth
    private val headerBar = Rectangle(trackListViewModel.trackWidth, 12.0, trackListViewModel.generalGray.darker().darker())
    init {
        setTrackRectangleProperties()
        headerBar.translateY = trackListViewModel.masterOffsetY - trackRectangle.height/2.0 - 6.0
        headerBar.arcWidth = 5.0
        headerBar.arcHeight = 5.0
        headerBar.stroke = trackListViewModel.strokeColor
        headerBar.strokeWidth = 0.5
    }
    override val addButton = AddButton(this)
    override val inputEnableButton = InputEnableButton(this)
    override val vuMeter = VUMeter(this)
    override val inputSelectArrow = InputSelectArrow(root, this)
    override val waveFormBox = WaveFormBox(this)
    override val inputNameBox = InputNameBox(root, this)
    override val volumeSlider = VolumeSlider(this)

    override fun respondToChange(broadcastType: BroadcastType, old: Double, new: Double) {
        when (broadcastType) {
            BroadcastType.DIVIDER -> {
                trackDivider.translateX = new
                waveFormBox.respondToDividerShift(new)
            }
            BroadcastType.STAGE_WIDTH -> {
                val amtChange = (new - old) / 2.0
                trackWidth = new
                trackRectangle.translateX -= amtChange
                headerBar.width = new
                trackDivider.translateX -= amtChange
                trackListViewModel.currentDividerOffset.setValue(trackDivider.translateX)
                labelDivider.translateX -= amtChange
                trackListViewModel.labelDividerOffset = labelDivider.translateX
                trackLabel.translateX -= amtChange
                trackLabelNumber.translateX -= amtChange
            }
            BroadcastType.STAGE_HEIGHT -> {
                trackOffsetY -= (new - old) / 2.0
                trackRectangle.translateY = trackOffsetY
                headerBar.translateY = trackOffsetY  - trackRectangle.height/2.0 - 6.0
                trackDivider.translateY = trackOffsetY
                trackDivider.translateY = trackOffsetY
                labelDivider.translateY = trackOffsetY
                trackLabel.translateY = trackOffsetY
                trackLabelNumber.translateY = trackOffsetY
                trackListViewModel.masterOffsetY = trackOffsetY
            }
            BroadcastType.INDEX -> {}
            BroadcastType.SCROLL -> {}
        }
    }

    override fun registerForBroadcasts() {
        trackListViewModel.registerForHeightChanges(this)
        trackListViewModel.registerForWidthChanges(this)
        trackListViewModel.registerForDividerOffsetChanges(this)
    }

    override fun unregisterForBroadcasts() {
        trackListViewModel.unregisterForWidthChanges(this)
        trackListViewModel.unregisterForHeightChanges(this)
        trackListViewModel.unregisterForDividerOffsetChanges(this)
    }

    override fun addChild(child: Widget) {
        children.add(child)
    }

    override fun addTrack() {
        trackListViewModel.addTrackFromMaster()
    }

    override fun backspaceText() {
        inputNameBox.backspaceText()
    }

    override fun characterText(character: KeyEvent) {
        inputNameBox.characterText(character)
    }

    override fun addMeToScene(root: StackPane) {
        registerForBroadcasts()
        root.children.add(trackRectangle)
        root.children.add(headerBar)
        root.children.add(trackDivider)
        root.children.add(trackLabel)
        root.children.add(labelDivider)
        vuMeter.addMeToScene(root)
        addButton.addMeToScene(root)
        waveFormBox.addMeToScene(root)
        inputNameBox.addMeToScene(root)
        volumeSlider.addMeToScene(root)
        addChild(vuMeter)
        addChild(addButton)
        addChild(waveFormBox)
        addChild(inputNameBox)
        addChild(volumeSlider)
    }

    override fun removeMeFromScene(root: StackPane) {
        Platform.runLater {
            unregisterForBroadcasts()
            for (child in children) {
                child.removeMeFromScene(root)
            }
            children.clear()
            root.children.remove(trackRectangle)
            root.children.remove(headerBar)
            root.children.remove(trackLabel)
            root.children.remove(labelDivider)
            root.children.remove(trackDivider)
        }
    }
}