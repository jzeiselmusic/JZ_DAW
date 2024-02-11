package org.jzeisel.app_test.components.trackBar.tracks

import javafx.application.Platform
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.scene.layout.StackPane
import org.jzeisel.app_test.components.TrackComponentWidget
import org.jzeisel.app_test.components.Widget
import org.jzeisel.app_test.components.trackBar.smallComponents.*
import org.jzeisel.app_test.components.vuMeter.VUMeter
import org.jzeisel.app_test.logger.Logger
import kotlin.properties.Delegates

class NormalTrack(root: StackPane, override val parent: Widget,
                  initialIndex: Int, progenitor: Track)
    : Track(root, parent), Widget {
    companion object {
        const val TAG = "NormalTrack"
        const val LEVEL = 1
    }
    override var name: String by Delegates.observable ((initialIndex + 1).toString()) {
        _, _, new ->
        trackLabelNumber.text = new
        if (!inputNameBox.nameSetByUser) inputNameBox.name = "Track $new"
    }

    var index = initialIndex

    override var trackOffsetY: Double by Delegates.observable(
                progenitor.trackOffsetY + trackListViewModel.trackHeight) {
        _, old, new ->
            trackRectangle.translateY = new
            trackDivider.translateY = new
            trackDivider.translateY = new
            labelDivider.translateY = new
            trackLabel.translateY = new
            trackLabelNumber.translateY = new
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
    override val children = mutableListOf<Widget>()
    init {
        setTrackRectangleProperties()
        trackLabelNumber.text = name
    }
    override val vuMeter = VUMeter(this)
    override val addButton = AddButton(this)
    override val inputEnableButton = InputEnableButton(this)
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

    fun respondToChangeInTrackList(old: List<Widget>, new: List<Widget>) {
        index = new.indexOf(this)
        trackOffsetY = trackListViewModel.masterOffsetY + (index+1)*initialTrackHeight
        name = (index+1).toString()
        Logger.debug(TAG, "track $name new index is $index", LEVEL)
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
        inputNameBox.addMeToScene(root)
        volumeSlider.addMeToScene(root)
        addChild(vuMeter)
        addChild(addButton)
        addChild(inputEnableButton)
        addChild(inputSelectArrow)
        addChild(waveFormBox)
        addChild(inputNameBox)
        addChild(volumeSlider)
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

    override fun addTrack() {
        trackListViewModel.addTrack(this)
    }

    override fun backspaceText() {
        inputNameBox.backspaceText()
    }

    override fun characterText(character: KeyEvent) {
        inputNameBox.characterText(character)
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