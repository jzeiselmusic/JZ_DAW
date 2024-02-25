package org.jzeisel.app_test.components

import javafx.application.Platform
import javafx.scene.input.KeyEvent
import javafx.scene.layout.StackPane
import org.jzeisel.app_test.components.trackComponents.*
import org.jzeisel.app_test.components.trackComponents.VUMeter
import org.jzeisel.app_test.util.BroadcastType
import org.jzeisel.app_test.util.Observable
import org.jzeisel.app_test.util.ObservableListener
import kotlin.properties.Delegates

class NormalTrack(root: StackPane, override val parent: Widget,
                  initialIndex: Int, progenitor: Track
)
    : Track(root, parent), Widget {

    override var name: String by Delegates.observable ((initialIndex + 1).toString()) {
        _, _, new ->
        trackLabelNumber.text = new
        if (!inputNameBox.nameSetByUser) inputNameBox.name = "Track $new"
    }

    var index = Observable(initialIndex.toDouble())
    override var trackOffsetY: Double = progenitor.trackOffsetY + trackListViewModel.trackHeight
    var trackWidth: Double = initialTrackWidth
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

    override fun respondToChange(broadcastType: BroadcastType, old: Double, new: Double) {
        when (broadcastType) {
            BroadcastType.DIVIDER -> {
                trackDivider.translateX = new
            }
            BroadcastType.STAGE_WIDTH -> {
                val amtChange = (new - old) / 2.0
                trackWidth = new
                trackRectangle.width = new
                trackRectangle.width = new
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
                trackDivider.translateY = trackOffsetY
                trackDivider.translateY = trackOffsetY
                labelDivider.translateY = trackOffsetY
                trackLabel.translateY = trackOffsetY
                trackLabelNumber.translateY = trackOffsetY
            }
            BroadcastType.INDEX -> {}
        }
    }

    override fun registerForBroadcasts() {
        trackListViewModel.registerForWidthChanges(this)
        trackListViewModel.registerForHeightChanges(this)
        trackListViewModel.registerForDividerOffsetChanges(this)
    }

    override fun unregisterForBroadcasts() {
        trackListViewModel.unregisterForHeightChanges(this)
        trackListViewModel.unregisterForWidthChanges(this)
        trackListViewModel.unregisterForDividerOffsetChanges(this)
    }

    fun respondToChangeInTrackList(old: List<Widget>, new: List<Widget>) {
        val newIndex = new.indexOf(this)
        trackOffsetY = trackListViewModel.masterOffsetY + (newIndex+1)*initialTrackHeight
        name = (newIndex+1).toString()
        index.setValueAndNotify(newIndex.toDouble(), BroadcastType.INDEX)
        (trackListViewModel.masterOffsetY + trackListViewModel.trackHeight*(newIndex+1)).let{
            trackRectangle.translateY = it
            trackDivider.translateY = it
            trackLabel.translateY = it
            labelDivider.translateY = it
            trackLabelNumber.translateY = it
        }
    }

    override fun addMeToScene(root: StackPane) {
        registerForBroadcasts()
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
            unregisterForBroadcasts()
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

    fun registerForIndexChanges(listener: ObservableListener<Double>) {
        index.addListener(listener)
    }

    fun unregisterForIndexChanges(listener: ObservableListener<Double>) {
        index.removeListener(listener)
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