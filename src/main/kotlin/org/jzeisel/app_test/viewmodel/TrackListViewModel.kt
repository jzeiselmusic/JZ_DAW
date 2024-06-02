package org.jzeisel.app_test.viewmodel

import javafx.animation.PauseTransition
import javafx.beans.property.ReadOnlyDoubleProperty
import javafx.scene.input.KeyEvent
import javafx.scene.layout.StackPane
import javafx.stage.Stage
import javafx.util.Duration
import org.jzeisel.app_test.error.AudioError
import org.jzeisel.app_test.audio.viewmodel.AudioViewModel
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
import org.jzeisel.app_test.util.*
import kotlin.math.log10
import kotlin.properties.Delegates
import kotlin.random.Random

class TrackListViewModel(val root: StackPane,
                         val stage: Stage, extraPane: StackPane): NodeWidget {

    lateinit var audioViewModel: AudioViewModel
    val stageWidthProperty: ReadOnlyDoubleProperty = stage.widthProperty()
    val stageHeightProperty: ReadOnlyDoubleProperty = stage.heightProperty()

    override val parent: Widget? = null
    /* all TrackList children will be NormalTracks */
    override var children : MutableList<Widget> by Delegates.observable(mutableListOf()) { _, old, new ->
        _trackListStateFlow.state = _trackListStateFlow.state.copy(numTracks = new.size)
        for (child in children) {
            val t = child as NormalTrack
            t.respondToChangeInTrackList(old, new)
        }
        CursorFollower.updateFromTrackList(root)
    }
    var listOfTrackIds: MutableList<Int> = mutableListOf()
    var listOfFileIds: MutableList<Int> = mutableListOf()
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

    fun findNewTrackId(): Int {
        var newTrackId = Random.nextInt()
        while (listOfTrackIds.contains(newTrackId)) {
            newTrackId = Random.nextInt()
        }
        listOfTrackIds.add(newTrackId)
        return newTrackId
    }

    fun addTrack(child: Widget) {
        /* tell this function which child called it */
        /* if called by index -1, then called by master */
        val trackId = findNewTrackId()
        val newTrack = NormalTrack(root, this, trackId,
                (child as NormalTrack).index.getValue().toInt() + 1, child as Track)
        newTrack.addMeToScene(root)
        addChild(newTrack)
        audioViewModel.addTrack(trackId)
    }

    fun addTrackFromMaster() {
        val trackId = findNewTrackId()
        val newTrack = NormalTrack(root, this, trackId, 0, masterTrack)
        newTrack.addMeToScene(root)
        addChild(newTrack)
        audioViewModel.addTrack(trackId)
    }

    fun onAudioSamplesProcessed(numSamples: Int) {
        if (_trackListStateFlow.state.playBackStarted) {
            val pixelsToMove = samplesToPixels(numSamples, audioViewModel.tempo, audioViewModel.sampleRate, _trackListStateFlow.state.pixelsInABeat)
            children.forEach {
                val track = it as NormalTrack
                if (track.isRecording) {
                    val dbLevel = 20 * log10(audioViewModel.getRmsVolumeInputStream(track.trackId))
                    it.processBuffer(dbLevel, numSamples)
                }
            }
            CursorFollower.moveLocationForward(pixelsToMove)
        }
    }

    fun deletePressed() {
        children.forEach {
            (it as NormalTrack).deleteHighlightedFiles()
        }
    }

    fun spacePressed() {
        if (!_trackListStateFlow.state.playBackStarted) {
            val currentPositionPixels = _trackListStateFlow.state.cursorOffset
            val currentPositionSamples = audioViewModel.cursorOffsetSamples

            audioViewModel.saveCurrentCursorOffsetSamples(currentPositionSamples)
            _trackListStateFlow.state = _trackListStateFlow.state.copy(playBackStarted = true, savedCursorPositionOffset = currentPositionPixels)
            var newFileId = Random.nextInt()
            while (newFileId in listOfFileIds) {
                newFileId = Random.nextInt()
            }
            children.forEach {
                (it as NormalTrack).startRecording(currentPositionPixels, it.trackId.xor(newFileId))
                it.setVUMeterRunning(true)
            }
            audioViewModel.startPlayback(newFileId)

        }
        else {
            _trackListStateFlow.state = _trackListStateFlow.state.copy(playBackStarted = false)
            audioViewModel.stopPlayback()
            children.forEach {
                (it as NormalTrack).stopRecording()
                it.setVUMeterRunning(false)
            }
            val savedPositionPixels = _trackListStateFlow.state.savedCursorPositionOffset
            CursorFollower.updateLocation(savedPositionPixels)
            audioViewModel.resetCursorOffsetSamples()
        }
    }

    fun updateCursorOffsetFromWaveformStart(value: Double) {
        _trackListStateFlow.state = _trackListStateFlow.state.copy(cursorOffset = value)
        val sampleOffsetFromStart = pixelsToSamples(
            value,
            audioViewModel.tempo,
            audioViewModel.sampleRate,
            _trackListStateFlow.state.pixelsInABeat
        )
        audioViewModel.updateCursorOffsetSamples(sampleOffsetFromStart)
    }

    fun removeTrack(child: Widget) {
        /* same comment as above */
        val trackId = (child as NormalTrack).trackId
        runLater {
            child.removeMeFromScene(root)
            listOfTrackIds.remove(trackId)
            children = children.toMutableList().apply {
                remove(child)
            }
        }
        runLater(500.0) {
            audioViewModel.removeTrack(trackId)
        }
    }

    fun deleteAudioFile(trackId: Int, fileId: Int) {
        Logger.debug(javaClass.simpleName, "deleting file", 3)
        audioViewModel.deleteFile(trackId, fileId)
        listOfFileIds.remove(fileId)
    }

    fun moveFile(moveDirection: MoveDirection, sourceTrackId: Int, sourceFileId: Int) {
        /* - find track above or below this one.
           - move file out of source track wavebox child list
           - change source file parent
           - put file into destination track wavebox child list
           - tell audio engine to move file
       */
        val sourceTrack = children.filter{ (it as NormalTrack).trackId == sourceTrackId }[0] as NormalTrack
        val sourceTrackIndex = sourceTrack.index.getValue().toInt()
        if (moveDirection == MoveDirection.UP && sourceTrackIndex != 0) {
            /* move file up */
            val destTrack = children.filter { (it as NormalTrack).index.getValue().toInt() == sourceTrackIndex - 1}[0] as NormalTrack
            val fileToMove = sourceTrack.waveFormBox.popFile(sourceFileId)
            destTrack.waveFormBox.addAlreadyExistingFile(fileToMove)
            fileToMove.moveFile(MoveDirection.UP)
            audioViewModel.moveFile(destTrack.trackId, sourceTrack.trackId, sourceFileId)
        }
        if (moveDirection == MoveDirection.DOWN && (sourceTrackIndex != _trackListStateFlow.state.numTracks - 1)) {
            /* move file down */
            val destTrack = children.filter { (it as NormalTrack).index.getValue().toInt() == sourceTrackIndex + 1 }[0] as NormalTrack
            val fileToMove = sourceTrack.waveFormBox.popFile(sourceFileId)
            destTrack.waveFormBox.addAlreadyExistingFile(fileToMove)
            fileToMove.moveFile(MoveDirection.DOWN)
            audioViewModel.moveFile(destTrack.trackId, sourceTrack.trackId, sourceFileId)
        }
    }

    fun setTrackDeviceAndChannel(trackId: Int, deviceIndex: Int, channelIndex: Int) {
        audioViewModel.setTrackDeviceAndChannel(trackId, deviceIndex, channelIndex)
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

    fun broadcastMousePress() {
        for (child in children) {
            val track = child as NormalTrack
            track.waveFormBox.unclickAllFiles()
        }
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
        CursorFollower.updateLocation(translateX)
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

    fun setInputEnabled(child: Widget) {
        audioViewModel.enableInputForTrack((child as NormalTrack).trackId)
    }

    fun setInputDisabled(child: Widget) {
        audioViewModel.disableInputForTrack((child as NormalTrack).trackId)
    }

    fun onPlaybackError(error: AudioError) {
    }

    fun updateTrackRMSVolume(volume: Double, trackId: Int) {
        children.firstOrNull { (it as NormalTrack).trackId == trackId }?.let {
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

    fun getTrackInputDeviceIndex(trackId: Int): Int {
        return audioViewModel.getTrackInputDeviceIndex(trackId)
    }

    fun setArmRecording(child: Widget) {
        val id = (child as NormalTrack).trackId
        audioViewModel.armRecording(id)
    }

    fun setDisarmRecording(child: Widget) {
        val id = (child as NormalTrack).trackId
        audioViewModel.disarmRecording(id)
    }

    fun updateFileOffset(newSampleOffset: Int, fileId: Int, trackId: Int) {
        audioViewModel.updateTrackOffset(trackId, fileId, newSampleOffset)
    }
}