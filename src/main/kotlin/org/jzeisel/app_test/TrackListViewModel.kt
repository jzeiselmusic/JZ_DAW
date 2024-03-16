package org.jzeisel.app_test

import javafx.animation.PauseTransition
import javafx.application.Platform
import javafx.beans.property.ReadOnlyDoubleProperty
import javafx.scene.input.KeyEvent
import javafx.scene.layout.StackPane
import javafx.stage.Stage
import javafx.util.Duration
import org.jzeisel.app_test.audio.AudioInputManager
import org.jzeisel.app_test.components.Widget
import org.jzeisel.app_test.components.singletons.CursorFollower
import org.jzeisel.app_test.components.MasterTrack
import org.jzeisel.app_test.components.NormalTrack
import org.jzeisel.app_test.components.Track
import org.jzeisel.app_test.components.singletons.VerticalScrollBar
import org.jzeisel.app_test.components.singletons.VerticalScrollBar.saturateAt
import org.jzeisel.app_test.stateflow.TrackListStateFlow
import org.jzeisel.app_test.util.BroadcastType
import org.jzeisel.app_test.util.ObservableListener
import kotlin.properties.Delegates

class TrackListViewModel(val root: StackPane, val stage: Stage, extraPane: StackPane): Widget {
    val stageWidthProperty: ReadOnlyDoubleProperty = stage.widthProperty()
    val stageHeightProperty: ReadOnlyDoubleProperty = stage.heightProperty()

    override val parent: Widget? = null
    /* all TrackList children will be NormalTracks */
    override var children : MutableList<Widget> by Delegates.observable(mutableListOf()) {_, old, new ->
        _trackListStateFlow.state = _trackListStateFlow.state.copy(numChildren = new.size)
        for (child in children) {
            val t = child as NormalTrack
            t.respondToChangeInTrackList(old, new)
        }
        CursorFollower.updateFromTrackList(root)
    }
    /*      *****      */
    val audioInputManager = AudioInputManager(this)

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

    override fun addChild(child: Widget) {
        children = children.toMutableList().apply {
            add((child as NormalTrack).index.getValue().toInt(), child)
        }
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
                (child as NormalTrack).index.getValue().toInt() + 1, child as Track
        )
        newTrack.addMeToScene(root)
        addChild(newTrack)
    }

    fun addTrackFromMaster() {
        val newTrack = NormalTrack(root, this, 0, masterTrack)
        newTrack.addMeToScene(root)
        addChild(newTrack)
    }

    fun removeTrack(child: Widget) {
        /* same comment as above */
        Platform.runLater {
            child.removeMeFromScene(root)
            children = children.toMutableList().apply {
                remove(child)
            }
        }
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
            VerticalScrollBar.addMeToScene()
            val delay = PauseTransition(Duration.millis(200.0));
            delay.setOnFinished {
                VerticalScrollBar.removeMeFromScene()
            }
            delay.play()
        }
    }

    private fun moveVerticalScrollBar(deltaY: Double, amountOfRoom: Double) {
        showVerticalScrollBar()
        VerticalScrollBar.moveScrollBar(deltaY, amountOfRoom)
    }

    fun scrollSceneVertically(deltaY: Double) {
        val newTranslate = (root.translateY + deltaY)
        val amountOfRoom = (_trackListStateFlow.totalHeightOfAllTracks - _trackListStateFlow.state.observableStageHeight.getValue()).saturateAt(0.0, null)
        if (amountOfRoom < 1.0) root.translateY = newTranslate.saturateAt(-amountOfRoom, 0.0)
        else root.translateY = newTranslate.saturateAt(-amountOfRoom - 30.0, 0.0)
        moveVerticalScrollBar(deltaY, amountOfRoom)
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

    fun setTrackAudioInput(index: Int, child: Widget) {
    }

    fun setTrackEnabled(child: Widget): Boolean {
        return true
    }

    fun setTrackDisabled(child: Widget) {
    }
}