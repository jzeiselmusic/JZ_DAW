package org.jzeisel.app_test.components

import javafx.scene.input.KeyEvent
import javafx.scene.layout.StackPane
import javafx.scene.paint.Color
import javafx.scene.shape.Polygon
import javafx.scene.shape.Rectangle
import org.jzeisel.app_test.components.interfaces.widget.NodeWidget
import org.jzeisel.app_test.components.interfaces.widget.Widget
import org.jzeisel.app_test.error.AudioError
import org.jzeisel.app_test.components.trackComponents.*
import org.jzeisel.app_test.components.trackComponents.VUMeter
import org.jzeisel.app_test.util.*
import kotlin.properties.Delegates

class NormalTrack(root: StackPane, override val parent: Widget,
                  val trackId: Int, initialIndex: Int, progenitor: Track)
    : Track(root, parent), NodeWidget {

    override var name: String by Delegates.observable ((initialIndex + 1).toString()) {
        _, _, new ->
        trackLabelNumber.text = new
        if (!inputNameBox.nameSetByUser) inputNameBox.name = "Track $new"
    }

    var index = Observable(initialIndex.toDouble())
    override var trackOffsetY: Double = progenitor.trackOffsetY + trackListState.trackHeight
    var trackWidth: Double = initialTrackWidth
    override val children = mutableListOf<Widget>()
    init {
        setTrackRectangleProperties()
        trackLabelNumber.text = name
    }
    override val vuMeter = VUMeter(this)
    override val addButton = AddButton(this)
    override val inputEnableButton = InputEnableButton(this)
    override val soloButton = SoloButton(this)
    override val muteButton = MuteButton(this)

    val inputSelectArrow = InputSelectArrow(root, this)
    override val waveFormBox = WaveFormBox(this)
    override val inputNameBox = InputNameBox(root, this)
    override val volumeSlider = VolumeSlider(this)
    override val recordButton = RecordButton(this)
    var isRecording = false

    private val muteShader = Rectangle()
    private var muteShaderAdded = false

    var isMuted = false
        set(value) {
            if (value) setMuteShader()
            else removeMuteShader()
        }

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
        trackOffsetY = trackListState.masterOffsetY + (newIndex+1)*initialTrackHeight
        name = (newIndex+1).toString()
        index.setValueAndNotify(newIndex.toDouble(), BroadcastType.INDEX)
        (trackListState.masterOffsetY + trackListState.trackHeight*(newIndex+1)).let{
            trackRectangle.translateY = it
            trackDivider.translateY = it
            trackLabel.translateY = it
            labelDivider.translateY = it
            trackLabelNumber.translateY = it
            muteShader.translateY = it
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
        recordButton.addMeToScene(root)
        soloButton.addMeToScene(root)
        muteButton.addMeToScene(root)
        addChild(vuMeter)
        addChild(addButton)
        addChild(inputEnableButton)
        addChild(inputSelectArrow)
        addChild(waveFormBox)
        addChild(inputNameBox)
        addChild(volumeSlider)
        addChild(recordButton)
        addChild(soloButton)
        addChild(muteButton)
        isMuted = amIMuteMode()
    }

    private var inputEnabled = false
    private var recordEnabled = false

    override fun addChild(child: Widget) {
        children.add(child)
    }

    override fun removeMeFromScene(root: StackPane) {
        runLater {
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

    override fun respondToHeightChange(old: Double, new: Double) {
        trackOffsetY -= (new - old) / 2.0
        trackRectangle.translateY = trackOffsetY
        trackDivider.translateY = trackOffsetY
        trackDivider.translateY = trackOffsetY
        labelDivider.translateY = trackOffsetY
        trackLabel.translateY = trackOffsetY
        trackLabelNumber.translateY = trackOffsetY
        muteShader.translateY = trackOffsetY
    }

    override fun respondToWidthChange(old: Double, new: Double) {
        val amtChange = (new - old) / 2.0
        trackWidth = new
        trackRectangle.translateX -= amtChange
        trackDivider.translateX -= amtChange
        trackListState.currentDividerOffset.setValue(trackDivider.translateX)
        labelDivider.translateX -= amtChange
        trackListViewModel.updateLabelDividerOffset(labelDivider.translateX)
        trackLabel.translateX -= amtChange
        trackLabelNumber.translateX -= amtChange
        muteShader.translateX -= amtChange
    }

    fun registerForIndexChanges(listener: ObservableListener<Double>) {
        index.addListener(listener)
    }

    fun unregisterForIndexChanges(listener: ObservableListener<Double>) {
        index.removeListener(listener)
    }

    fun setVUMeterRunning(enabled: Boolean) {
        vuMeter.isVUMeterRunning = enabled
    }

    fun audioInputEnable() {
        trackListViewModel.setInputEnabled(this)
        vuMeter.isVUMeterRunning = true
        inputEnabled = true
        inputEnableButton.isEnabled = true
    }

    fun audioInputDisable() {
        inputEnableButton.isEnabled = false
        inputEnabled = false
        if (!recordEnabled) {
            vuMeter.isVUMeterRunning = false
            vuMeter.turnOffCurrentRMSReading()
        }
        trackListViewModel.setInputDisabled(this)
    }

    fun updateVUMeter(volume: Double) {
        vuMeter.setVUMeterCurrentRMS(volume)
    }

    override fun armRecording() {
        trackListViewModel.setArmRecording(this)
        recordButton.enabled = true
        vuMeter.isVUMeterRunning = true
        recordEnabled = true
    }

    override fun disarmRecording() {
        recordEnabled = false
        recordButton.enabled = false
        if (!inputEnabled) {
            vuMeter.isVUMeterRunning = false
            vuMeter.turnOffCurrentRMSReading()
        }
        trackListViewModel.setDisarmRecording(this)
    }

    fun startRecording(initialPixelOffset: Double, fileId: Int) {
        if (recordEnabled) {
            waveFormBox.startRecording(initialPixelOffset, fileId)
            isRecording = true
        }
    }

    fun stopRecording() {
        if (isRecording)
            waveFormBox.stopRecording()
        isRecording = false
    }

    fun deleteHighlightedFiles() {
        waveFormBox.deleteHighlightedFiles()
    }

    fun processBuffer(dbLevel: Double, pixels: Double) {
        if (isRecording)
            waveFormBox.processBuffer(dbLevel, pixels)
    }

    override fun soloEnable() {
        soloButton.isEnabled = true
        trackListViewModel.setSolo(true, this)
        isMuted = amIMuteMode()
    }

    override fun soloDisable() {
        soloButton.isEnabled = false
        trackListViewModel.setSolo(false, this)
        isMuted = amIMuteMode()
    }

    override fun muteEnable() {
        muteButton.isEnabled = true
        trackListViewModel.setMute(true, this)
        isMuted = amIMuteMode()
    }

    override fun muteDisable() {
        muteButton.isEnabled = false
        trackListViewModel.setMute(false, this)
        isMuted = amIMuteMode()
    }

    private fun amIMuteMode() : Boolean {
        return muteButton.isEnabled || (trackListState.soloEngaged && !soloButton.isEnabled)
    }

    @Synchronized
    private fun setMuteShader() {
        if (!muteShaderAdded) {
            muteShader.translateX = waveFormBox.trackRectangle.translateX
            muteShader.translateY = waveFormBox.trackRectangle.translateY
            muteShader.width = waveFormBox.trackRectangle.width
            muteShader.height = waveFormBox.trackRectangle.height
            muteShader.opacity = 0.4
            muteShader.isMouseTransparent = true
            muteShader.viewOrder = viewOrderFlip - 0.15
            root.children.add(muteShader)
            muteShaderAdded = true
        }
    }

    @Synchronized
    private fun removeMuteShader() {
        if (muteShaderAdded) {
            root.children.remove(muteShader)
            muteShaderAdded = false
        }
    }

    fun soloEngagedUpdated() {
        /* if this is sent out it means we have either transitioned from no solo tracks
            to one solo track or from one solo track to no solo tracks */
        isMuted = amIMuteMode()
    }
}