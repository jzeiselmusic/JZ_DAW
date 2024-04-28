package org.jzeisel.app_test.viewmodel

import javafx.animation.PauseTransition
import javafx.beans.property.ReadOnlyDoubleProperty
import javafx.scene.Cursor
import javafx.scene.input.KeyEvent
import javafx.scene.layout.StackPane
import javafx.stage.Stage
import javafx.util.Duration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jzeisel.app_test.error.AudioError
import org.jzeisel.app_test.audio.viewmodel.AudioViewModel
import org.jzeisel.app_test.audio.whenIs
import org.jzeisel.app_test.audio.whenNot
import org.jzeisel.app_test.components.interfaces.widget.Widget
import org.jzeisel.app_test.components.singletons.CursorFollower
import org.jzeisel.app_test.components.MasterTrack
import org.jzeisel.app_test.components.NormalTrack
import org.jzeisel.app_test.components.Track
import org.jzeisel.app_test.components.interfaces.widget.NodeWidget
import org.jzeisel.app_test.components.singletons.VerticalScrollBar
import org.jzeisel.app_test.components.singletons.VerticalScrollBar.saturateAt
import org.jzeisel.app_test.error.ErrorType
import org.jzeisel.app_test.error.PanicErrorMessage
import org.jzeisel.app_test.stateflow.TrackListStateFlow
import org.jzeisel.app_test.util.BroadcastType
import org.jzeisel.app_test.util.Logger
import org.jzeisel.app_test.util.ObservableListener
import org.jzeisel.app_test.util.runLater
import kotlin.properties.Delegates

class TrackListViewModel(val root: StackPane,
                         val stage: Stage, extraPane: StackPane): NodeWidget {

    lateinit var audioViewModel: AudioViewModel
    val stageWidthProperty: ReadOnlyDoubleProperty = stage.widthProperty()
    val stageHeightProperty: ReadOnlyDoubleProperty = stage.heightProperty()

    override val parent: Widget? = null
    /* all TrackList children will be NormalTracks */
    override var children : MutableList<Widget> by Delegates.observable(mutableListOf()) { _, old, new ->
        _trackListStateFlow.state = _trackListStateFlow.state.copy(numChildren = new.size)
        for (child in children) {
            val t = child as NormalTrack
            t.respondToChangeInTrackList(old, new)
        }
        CursorFollower.updateFromTrackList(root)
    }
    /*      *****      */

    val _trackListStateFlow = TrackListStateFlow(stageWidthProperty, stageHeightProperty)

    private val masterTrack: MasterTrack = MasterTrack(root,this)
    init {
        CursorFollower.initialize(this)
        VerticalScrollBar.initialize(this, extraPane)
        stageWidthProperty.addListener { _, _, new ->
            _trackListStateFlow.state = _trackListStateFlow.state.copy(trackWidth = new as Double)
            _trackListStateFlow.state.observableStageWidth.setValueAndNotify(new, BroadcastType.STAGE_WIDTH)
        }
        stageHeightProperty.addListener {_, _, new ->
            _trackListStateFlow.state.observableStageHeight.setValueAndNotify(new as Double, BroadcastType.STAGE_HEIGHT)
        }
    }

    fun addAudioEngine(model: AudioViewModel) {
        audioViewModel = model
        audioViewModel.initialize()
    }

    override fun addChild(child: Widget) {
        children = children.toMutableList().apply {
            add((child as NormalTrack).index.getValue().toInt(), child)
        }
    }

    fun getCopyOfTracks(): List<NormalTrack> {
        return children.map { it as NormalTrack }
    }

    override fun addMeToScene(root: StackPane) {
        /* TrackList does not need to be added to scene */
        masterTrack.addMeToScene(root)
    }

    override fun removeMeFromScene(root: StackPane) {
        /* TrackList should never be removed */
    }

    fun addTrack(child: Widget) {
        /* tell this function which child called it */
        /* if called by index -1, then called by master */
        val newTrack = NormalTrack(root, this,
                (child as NormalTrack).index.getValue().toInt() + 1, child as Track)
        newTrack.addMeToScene(root)
        addChild(newTrack)
        audioViewModel.addTrack(newTrack.index.getValue().toInt())
    }

    fun addTrackFromMaster() {
        val newTrack = NormalTrack(root, this, 0, masterTrack)
        newTrack.addMeToScene(root)
        addChild(newTrack)
        audioViewModel.addTrack(newTrack.index.getValue().toInt())
    }

    fun onAudioSamplesProcessed(numSamples: Int) {
        if (_trackListStateFlow.state.playBackStarted) {
            val secondsInABeat = 1.0 / (audioViewModel.tempo * (1.0 / 60.0))
            val pixelsToMove = numSamples * _trackListStateFlow.state.pixelsInABeat * (1.0 / secondsInABeat) * (1.0 / audioViewModel.sampleRate)
            CursorFollower.moveLocationForward(pixelsToMove)
        }
    }

    fun spacePressed() {
        if (!_trackListStateFlow.state.playBackStarted) {
            _trackListStateFlow.state = _trackListStateFlow.state.copy(playBackStarted = true)
            audioViewModel.startPlayback()
        }
        else {
            _trackListStateFlow.state = _trackListStateFlow.state.copy(playBackStarted = false)
            audioViewModel.stopPlayback()
        }
    }

    fun removeTrack(child: Widget) {
        /* same comment as above */
        runLater {
            val trackIndex = (child as NormalTrack).index
            audioViewModel.removeTrack(trackIndex.getValue().toInt())
            child.removeMeFromScene(root)
            children = children.toMutableList().apply {
                remove(child)
            }
        }
    }

    fun setTrackDeviceAndChannel(trackIndex: Int, deviceIndex: Int, channelIndex: Int) {
        audioViewModel.setTrackDeviceAndChannel(trackIndex, deviceIndex, channelIndex)
    }

    fun updateWaveFormOffset(new: Double) {
        _trackListStateFlow.state = _trackListStateFlow.state.copy(waveFormOffset = new)
    }

    fun updateLabelDividerOffset(new: Double) {
        _trackListStateFlow.state = _trackListStateFlow.state.copy(labelDividerOffset = new)
    }

    fun updateMasterOffsetY(new: Double) {
        _trackListStateFlow.state = _trackListStateFlow.state.copy(masterOffsetY = new)
    }

    fun onWaveFormBoxScroll(deltaX: Double) {
        _trackListStateFlow.state.waveFormScrollDeltaX.setValueAndNotify(deltaX, BroadcastType.SCROLL)
    }

    fun broadcastMouseClick(root: StackPane) {
        for (child in children) {
            val track = child as NormalTrack
            track.inputSelectArrow.removeDropDownBox(root)
            track.inputNameBox.exitTextField(root)
        }
        masterTrack.inputNameBox.exitTextField(root)
    }

    fun broadcastBackSpace() {
        for (child in children) {
            val track = child as Track
            track.backspaceText()
        }
        masterTrack.backspaceText()
    }

    fun broadcastCharacter(character: KeyEvent) {
        for (child in children) {
            val track = child as Track
            track.characterText(character)
        }
        masterTrack.characterText(character)
    }

    fun broadcastMouseClickOnWaveFormBox(translateX: Double) {
        if (!CursorFollower.isShowing) {
            CursorFollower.addMeToScene(root, translateX)
        }
        else {
            CursorFollower.updateLocation(translateX)
        }
    }

    private fun showVerticalScrollBar() {
        if (!VerticalScrollBar.isShowing) {
            VerticalScrollBar.addMeToScene(root)
            val delay = PauseTransition(Duration.millis(200.0));
            delay.setOnFinished {
                VerticalScrollBar.removeMeFromScene(root)
            }
            delay.play()
        }
    }

    private fun moveVerticalScrollBar(deltaY: Double, amountOfRoom: Double) {
        _trackListStateFlow.state.panicErrorMessage?.let {} ?: run {
            showVerticalScrollBar()
            VerticalScrollBar.moveScrollBar(deltaY, amountOfRoom)
        }
    }

    fun scrollSceneVertically(deltaY: Double) {
        _trackListStateFlow.state.panicErrorMessage?.let {} ?: run {
            val newTranslate = (root.translateY + deltaY)
            val amountOfRoom = (_trackListStateFlow.totalHeightOfAllTracks - _trackListStateFlow.state.observableStageHeight.getValue()).saturateAt(0.0, null)
            if (amountOfRoom < 1.0) root.translateY = newTranslate.saturateAt(-amountOfRoom, 0.0)
            else root.translateY = newTranslate.saturateAt(-amountOfRoom - 30.0, 0.0)
            moveVerticalScrollBar(deltaY, amountOfRoom)
        }
    }

    fun registerForWidthChanges(listener: ObservableListener<Double>) {
        _trackListStateFlow.state.observableStageWidth.addListener(listener)
    }

    fun unregisterForWidthChanges(listener: ObservableListener<Double>) {
        _trackListStateFlow.state.observableStageWidth.removeListener(listener)
    }

    fun registerForHeightChanges(listener: ObservableListener<Double>) {
        _trackListStateFlow.state.observableStageHeight.addListener(listener)
    }

    fun unregisterForHeightChanges(listener: ObservableListener<Double>) {
        _trackListStateFlow.state.observableStageHeight.removeListener(listener)
    }

    fun registerForDividerOffsetChanges(listener: ObservableListener<Double>) {
        _trackListStateFlow.state.currentDividerOffset.addListener(listener)
    }

    fun unregisterForDividerOffsetChanges(listener: ObservableListener<Double>) {
        _trackListStateFlow.state.currentDividerOffset.removeListener(listener)
    }

    fun registerForScrollChanges(listener: ObservableListener<Double>) {
        _trackListStateFlow.state.waveFormScrollDeltaX.addListener(listener)
    }

    fun unregisterForScrollChanges(listener: ObservableListener<Double>) {
        _trackListStateFlow.state.waveFormScrollDeltaX.removeListener(listener)
    }

    fun setTrackEnabled(child: Widget) {
        (child as NormalTrack).let {
            it.audioInputEnabled = true
            it.enableVUMeterRunning()
        }
        val err = audioViewModel.startInputStream(child.index.getValue().toInt())
        err.whenNot(AudioError.SoundIoErrorNone) {
            child.audioInputEnabled = false
            createAudioErrorMessage(it)
        }
    }

    fun setTrackDisabled(child: Widget) {
        audioViewModel.stopInputStream((child as? NormalTrack)?.index!!.getValue().toInt())
    }

    fun onPlaybackError(error: AudioError) {
    }

    fun updateTrackRMSVolume(volume: Double, trackIndex: Int) {
        children.firstOrNull { (it as NormalTrack).index.getValue().toInt() == trackIndex }?.let {
            (it as NormalTrack).updateVUMeter(volume)
        }
    }

    fun createAudioErrorMessage(error: AudioError) {
        val errorMessage = PanicErrorMessage(ErrorType.AudioEngineError,
            _trackListStateFlow.state, this, error.readable)
        errorMessage.addMeToScene(root)
        _trackListStateFlow.state = _trackListStateFlow.state.copy(panicErrorMessage = errorMessage)
    }

    fun removeErrorMessage() {
        _trackListStateFlow.state.panicErrorMessage?.let {
            it.removeMeFromScene(root)
            _trackListStateFlow.state = _trackListStateFlow.state.copy(panicErrorMessage = null)
        }
    }

    fun getDefaultInputIndex(): Int {
        return audioViewModel.defaultInputIndex
    }

    fun getTrackInputDeviceIndex(trackIndex: Int): Int {
        return audioViewModel.getTrackInputDeviceIndex(trackIndex)
    }
}