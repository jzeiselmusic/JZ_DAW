package org.jzeisel.app_test.components.trackBar.tracks

import javafx.application.Platform
import javafx.scene.input.KeyEvent
import javafx.scene.layout.StackPane
import javafx.scene.shape.Rectangle
import org.jzeisel.app_test.TrackListViewModel
import org.jzeisel.app_test.components.TrackComponentWidget
import org.jzeisel.app_test.components.Widget
import org.jzeisel.app_test.components.trackBar.smallComponents.*
import org.jzeisel.app_test.components.vuMeter.VUMeter
import org.jzeisel.app_test.logger.Logger
import kotlin.properties.Delegates

class MasterTrack(root: StackPane, override val parent: Widget)
    : Track(root, parent), Widget {
    companion object {
        const val TAG = "MasterTrack"
        const val LEVEL = 1
    }
    override val name = "Master"
    override val children = mutableListOf<Widget>()

    private val headerBar = Rectangle(trackListViewModel.trackWidth, 12.0, trackListViewModel.generalGray.darker().darker())
    init {
        headerBar.translateY = trackListViewModel.masterOffsetY - trackRectangle.height/2.0 - 6.0
        headerBar.arcWidth = 5.0
        headerBar.arcHeight = 5.0
        headerBar.stroke = trackListViewModel.strokeColor
        headerBar.strokeWidth = 0.5
    }
    override var trackOffsetY: Double by Delegates.observable(
                    trackListViewModel.masterOffsetY) {
        _, old, new ->
        trackRectangle.translateY = new
        headerBar.translateY = new  - trackRectangle.height/2.0 - 6.0
        trackDivider.translateY = new
        trackDivider.translateY = new
        labelDivider.translateY = new
        trackLabel.translateY = new
        trackLabelNumber.translateY = new
        trackListViewModel.masterOffsetY = new
        for (child in children) {
            val c = child as TrackComponentWidget
            c.respondToOffsetYChange(old, new)
        }
    }
    var trackWidth: Double by Delegates.observable(initialTrackWidth) {
        _, old, new ->
        val amtChange = (new - old) / 2.0
        trackRectangle.width = new
        trackRectangle.width = new
        headerBar.width = new
        trackDivider.translateX -= amtChange
        trackListViewModel.currentDividerOffset.setValue(trackDivider.translateX)
        labelDivider.translateX -= amtChange
        trackListViewModel.labelDividerOffset = labelDivider.translateX
        trackLabel.translateX -= amtChange
        trackLabelNumber.translateX -= amtChange
        for (child in children) {
            val c = child as TrackComponentWidget
            c.respondToWidthChange(old, new)
        }
    }
    init {
        setTrackRectangleProperties()
    }
    override val addButton = AddButton(this)
    override val inputEnableButton = InputEnableButton(this)
    override val vuMeter = VUMeter(this)
    override val inputSelectArrow = InputSelectArrow(root, this)
    override val waveFormBox = WaveFormBox(this)
    override val inputNameBox = InputNameBox(root, this)
    override val volumeSlider = VolumeSlider(this)

    override fun respondToChange(observable: Any, value: Double) {
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

    override fun backspaceText() {
        inputNameBox.backspaceText()
    }

    override fun characterText(character: KeyEvent) {
        inputNameBox.characterText(character)
    }

    override fun addMeToScene(root: StackPane) {
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