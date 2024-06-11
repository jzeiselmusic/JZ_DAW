package org.jzeisel.app_test.components

import javafx.scene.input.KeyEvent
import javafx.scene.layout.StackPane
import javafx.scene.shape.Rectangle
import org.jzeisel.app_test.components.interfaces.widget.NodeWidget
import org.jzeisel.app_test.components.interfaces.widget.Widget
import org.jzeisel.app_test.components.trackComponents.*
import org.jzeisel.app_test.components.trackComponents.VUMeter
import org.jzeisel.app_test.util.BroadcastType
import org.jzeisel.app_test.util.runLater

class MasterTrack(root: StackPane, override val parent: Widget)
    : Track(root, parent), NodeWidget {

    override val name = "Master"
    override val children = mutableListOf<Widget>()
    override var trackOffsetY: Double = trackListState.masterOffsetY
    var trackWidth: Double = initialTrackWidth
    private val headerBar = Rectangle(trackListState.trackWidth, 12.0, trackListState.generalGray.darker().darker())
    init {
        setTrackRectangleProperties()
        headerBar.translateY = trackListState.masterOffsetY - trackRectangle.height/2.0 - 6.0
        headerBar.arcWidth = 5.0
        headerBar.arcHeight = 5.0
        headerBar.stroke = trackListState.strokeColor
        headerBar.strokeWidth = 0.5
    }
    override val addButton = AddButton(this)
    override val inputEnableButton = InputEnableButton(this)
    override val vuMeter = VUMeter(this)
    override val waveFormBox = WaveFormBox(this)
    override val inputNameBox = InputNameBox(root, this)
    override val volumeSlider = VolumeSlider(this)
    override val soloButton = SoloButton(this)
    override val muteButton = MuteButton(this)
    override val recordButton = RecordButton(this)

    override fun respondToChange(broadcastType: BroadcastType, old: Double, new: Double) {
        when (broadcastType) {
            BroadcastType.DIVIDER -> {}
            BroadcastType.STAGE_WIDTH -> { respondToWidthChange(old, new) }
            BroadcastType.STAGE_HEIGHT -> { respondToHeightChange(old, new) }
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

    override fun respondToHeightChange(old: Double, new: Double) {
        trackOffsetY -= (new - old) / 2.0
        trackRectangle.translateY = trackOffsetY
        headerBar.translateY = trackOffsetY  - trackRectangle.height/2.0 - 6.0
        trackDivider.translateY = trackOffsetY
        trackDivider.translateY = trackOffsetY
        labelDivider.translateY = trackOffsetY
        trackLabel.translateY = trackOffsetY
        trackLabelNumber.translateY = trackOffsetY
        trackListViewModel.updateMasterOffsetY(trackOffsetY)
    }

    override fun respondToWidthChange(old: Double, new: Double) {
        val amtChange = (new - old) / 2.0
        trackWidth = new
        trackRectangle.translateX -= amtChange
        headerBar.width = new
        trackDivider.translateX -= amtChange
        trackListState.currentDividerOffset.setValue(trackDivider.translateX)
        labelDivider.translateX -= amtChange
        trackListViewModel.updateLabelDividerOffset(labelDivider.translateX)
        trackLabel.translateX -= amtChange
        trackLabelNumber.translateX -= amtChange
    }

    override fun armRecording() {
        recordButton.enabled = true
        trackListViewModel.armAllTracksForRecording()
    }

    override fun disarmRecording() {
        recordButton.enabled = false
        trackListViewModel.disarmAllTracksForRecording()
    }

    override fun soloEnable() {
        soloButton.isEnabled = true
        trackListViewModel.soloAllTracks(true)
    }

    override fun soloDisable() {
        soloButton.isEnabled = false
        trackListViewModel.soloAllTracks(false)
    }

    override fun muteEnable() {
        muteButton.isEnabled = true
        trackListViewModel.muteAllTracks(true)
    }

    override fun muteDisable() {
        muteButton.isEnabled = false
        trackListViewModel.muteAllTracks(false)
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
        soloButton.addMeToScene(root)
        muteButton.addMeToScene(root)
        recordButton.addMeToScene(root)
        addChild(vuMeter)
        addChild(addButton)
        addChild(waveFormBox)
        addChild(inputNameBox)
        addChild(volumeSlider)
        addChild(soloButton)
        addChild(muteButton)
        addChild(recordButton)
        vuMeter.isVUMeterRunning = true
    }

    fun updateVUMeter(volume: Double) {
        vuMeter.setVUMeterCurrentLogRMS(volume)
    }
    override fun removeMeFromScene(root: StackPane) {
        runLater {
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