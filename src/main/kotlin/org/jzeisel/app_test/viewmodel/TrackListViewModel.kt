package org.jzeisel.app_test.viewmodel

import javafx.animation.PauseTransition
import javafx.beans.property.ReadOnlyDoubleProperty
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.scene.input.MouseEvent
import javafx.scene.layout.StackPane
import javafx.stage.Stage
import javafx.util.Duration
import org.jzeisel.app_test.error.AudioError
import org.jzeisel.app_test.audio.viewmodel.AudioViewModel
import org.jzeisel.app_test.components.Background
import org.jzeisel.app_test.components.interfaces.widget.Widget
import org.jzeisel.app_test.components.singletons.CursorFollower
import org.jzeisel.app_test.components.MasterTrack
import org.jzeisel.app_test.components.NormalTrack
import org.jzeisel.app_test.components.Track
import org.jzeisel.app_test.components.interfaces.widget.NodeWidget
import org.jzeisel.app_test.components.singletons.VerticalScrollBar
import org.jzeisel.app_test.components.singletons.VerticalScrollBar.saturateAt
import org.jzeisel.app_test.components.trackComponents.WaveFormBox
import org.jzeisel.app_test.components.trackComponents.WaveFormFile
import org.jzeisel.app_test.error.ErrorType
import org.jzeisel.app_test.error.PanicErrorMessage
import org.jzeisel.app_test.stateflow.KeyState
import org.jzeisel.app_test.stateflow.TrackListStateFlow
import org.jzeisel.app_test.util.*
import kotlin.math.log10
import kotlin.math.roundToInt
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
    private val backgroundRect = Background(this)
    init {
        backgroundRect.addMeToScene(root)
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

    private fun findTrackByLargestIndex() : NormalTrack {
        return children.map { it as NormalTrack }.sortedBy { it.index.getValue() }.last()
    }

    fun addTrackFromDoubleClick() {
        if (children.isEmpty()) {
            addTrackFromMaster()
            return
        }
        val trackId = findNewTrackId()
        val track = findTrackByLargestIndex()
        val newTrack = NormalTrack(root, this, trackId, track.index.getValue().toInt() + 1, track as Track)
        newTrack.addMeToScene(root)
        addChild(newTrack)
        audioViewModel.addTrack(trackId)
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
            val startingOffsetX = CursorFollower.currentOffsetX
            val pixelsToMove = samplesToPixels(numSamples, audioViewModel.tempo, audioViewModel.sampleRate, _trackListStateFlow.state.pixelsInABeat)
            CursorFollower.moveLocationForward(pixelsToMove)
            children.forEach {
                val track = it as NormalTrack
                if (track.isRecording) {
                    val dbLevel = 20 * log10(audioViewModel.getRmsVolumeInputStream(track.trackId))
                    it.processBuffer(dbLevel, pixelsToMove)
                }
            }
            if (_trackListStateFlow.state.playbackHighlightSection.isEnabled) {
                val realOffsetX = startingOffsetX + pixelsToMove
                if (realOffsetX >= _trackListStateFlow.state.playbackHighlightSection.pixelEndOffset) {
                    spacePressed()
                }
            }
        }
    }

    private fun deletePressed() {
        children.forEach {
            (it as NormalTrack).deleteHighlightedFiles()
        }
    }

    fun fileXShifted(pixelsMoved: Double) {
        children.forEach { track ->
            (track as NormalTrack).waveFormBox.children.forEach { file ->
                if ((file as WaveFormFile).isHighlighted) {
                    file.moveFileX(pixelsMoved)
                    _trackListStateFlow.state.filesHighlighted.hasMovedSinceLastPress = true
                }
            }
        }
    }

    fun fileYShifted(direction: MoveDirection) {
        /* first make sure all the files in question are able to move */
        var ableToMove = true
        children.forEach { track ->
            (track as NormalTrack).waveFormBox.children.forEach { file ->
                if ((file as WaveFormFile).isHighlighted) {
                    if (!ableToMoveY(direction, track.trackId, file.fileId)) {
                        ableToMove = false
                    }
                }
            }
        }
        if (ableToMove) {
            _trackListStateFlow.state.filesHighlighted.files.forEach {
                if (it.isHighlighted) {
                    moveFileToNewTrack(direction, ((it.mutableParent as WaveFormBox).parentTrack as NormalTrack).trackId, it.fileId)
                    _trackListStateFlow.state.filesHighlighted.hasMovedSinceLastPress = true
                }
            }
        }
    }

    fun updateHighlightSection(totalDistanceX: Double, lastMousePressLocationX: Double) {
        val numIncrementsHighlight = (totalDistanceX / _trackListStateFlow.state.incrementSize).roundToInt()
        val sign = if (numIncrementsHighlight > 0) 1 else -1
        masterTrack.waveFormBox.updateHighlightSection(numIncrementsHighlight, sign, lastMousePressLocationX)
        children.forEach {
            (it as NormalTrack).waveFormBox.updateHighlightSection(numIncrementsHighlight, sign, lastMousePressLocationX)
        }
        val lastMousePressLocationOffsetX = lastMousePressLocationX - _trackListStateFlow.state.currentDividerOffset.getValue() + _trackListStateFlow.state.waveFormOffset
        _trackListStateFlow.state.playbackHighlightSection.isEnabled = numIncrementsHighlight != 0
        if (_trackListStateFlow.state.playbackHighlightSection.isEnabled) {
            _trackListStateFlow.state.playbackHighlightSection.pixelStartOffset =
                                                    if (sign == 1) lastMousePressLocationOffsetX
                                                    else lastMousePressLocationOffsetX + numIncrementsHighlight*_trackListStateFlow.state.incrementSize
            _trackListStateFlow.state.playbackHighlightSection.pixelEndOffset =
                                                    if (sign == 1) lastMousePressLocationOffsetX + numIncrementsHighlight*_trackListStateFlow.state.incrementSize
                                                    else lastMousePressLocationOffsetX
            _trackListStateFlow.state.playbackHighlightSection.loopEnabled = true
        }
        else {
            _trackListStateFlow.state.playbackHighlightSection.pixelStartOffset = CursorFollower.currentOffsetX
            _trackListStateFlow.state.playbackHighlightSection.pixelEndOffset = CursorFollower.currentOffsetX
        }
    }

    private fun spacePressed() {
        if (!_trackListStateFlow.state.playBackStarted) {
            if (_trackListStateFlow.state.playbackHighlightSection.isEnabled) {
                val offsetX = _trackListStateFlow.state.playbackHighlightSection.pixelStartOffset
                CursorFollower.updateLocation(offsetX)
                audioViewModel.updateCursorOffsetSamples(
                    pixelsToSamples(
                        offsetX,
                        audioViewModel.tempo,
                        audioViewModel.sampleRate,
                        _trackListStateFlow.state.pixelsInABeat
                    )
                )
                _trackListStateFlow.state = _trackListStateFlow.state.copy(cursorOffset = offsetX)
            }
            val currentPositionPixels = _trackListStateFlow.state.cursorOffset
            val currentPositionSamples = audioViewModel.cursorOffsetSamples
            audioViewModel.saveCurrentCursorOffsetSamples(currentPositionSamples)
            _trackListStateFlow.state = _trackListStateFlow.state.copy(
                playBackStarted = true,
                savedCursorPositionOffset = currentPositionPixels
            )
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
                it.vuMeter.setVUMeterCurrentLogRMS(-100.0)
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
        audioViewModel.deleteFile(trackId, fileId)
        listOfFileIds.remove(fileId)
    }

    private fun ableToMoveY(moveDirection: MoveDirection, sourceTrackId: Int, sourceFileId: Int): Boolean {
        val sourceTrack = children.filter{ (it as NormalTrack).trackId == sourceTrackId }[0] as NormalTrack
        val sourceTrackIndex = sourceTrack.index.getValue().toInt()
        if (
            (moveDirection == MoveDirection.UP && sourceTrackIndex != 0) ||
            (moveDirection == MoveDirection.DOWN && (sourceTrackIndex != _trackListStateFlow.state.numTracks - 1))) {
            return true
        }
        else {
            return false
        }
    }

    fun moveFileToNewTrack(moveDirection: MoveDirection, sourceTrackId: Int, sourceFileId: Int) {
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
            fileToMove.moveFileY(MoveDirection.UP)
            audioViewModel.moveFile(destTrack.trackId, sourceTrack.trackId, sourceFileId)
        }
        if (moveDirection == MoveDirection.DOWN && (sourceTrackIndex != _trackListStateFlow.state.numTracks - 1)) {
            /* move file down */
            val destTrack = children.filter { (it as NormalTrack).index.getValue().toInt() == sourceTrackIndex + 1 }[0] as NormalTrack
            val fileToMove = sourceTrack.waveFormBox.popFile(sourceFileId)
            destTrack.waveFormBox.addAlreadyExistingFile(fileToMove)
            fileToMove.moveFileY(MoveDirection.DOWN)
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

    fun exitTextFields() {
        children.forEach {
            (it as NormalTrack).inputNameBox.exitTextField(root)
        }
        masterTrack.inputNameBox.exitTextField(root)
    }

    fun removeDropDownBoxes() {
        children.forEach {
            (it as NormalTrack).inputSelectArrow.removeDropDownBox(root)
        }
    }

    fun unclickAllFiles() {
        children.forEach {
            (it as NormalTrack).waveFormBox.unclickAllFiles()
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

    fun updateOutputRMSVolume(volume: Double) {
        masterTrack.updateVUMeter(volume)
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

    fun armAllTracksForRecording() {
        children.forEach {
            (it as NormalTrack).armRecording()
        }
    }

    fun disarmAllTracksForRecording() {
        children.forEach {
            (it as NormalTrack).disarmRecording()
        }
    }

    fun setSolo(enabled: Boolean, track: NormalTrack) {
        audioViewModel.setSolo(enabled, track.trackId)
        var solo = false
        children.forEach {
            if ((it as NormalTrack).soloButton.isEnabled) {
                solo = true
                return@forEach
            }
        }
        if (solo != _trackListStateFlow.state.soloEngaged) {
            _trackListStateFlow.state = _trackListStateFlow.state.copy(soloEngaged = solo)
            children.forEach {
                (it as NormalTrack).soloEngagedUpdated()
            }
        }
    }

    fun setMute(enabled: Boolean, track: NormalTrack) {
        audioViewModel.setMute(enabled, track.trackId)
    }

    fun soloAllTracks(enable: Boolean) {
        children.forEach {
            when (enable) {
                true -> (it as NormalTrack).soloEnable()
                false -> (it as NormalTrack).soloDisable()
            }
        }
    }

    fun muteAllTracks(enable: Boolean) {
        children.forEach {
            when (enable) {
                true -> (it as NormalTrack).muteEnable()
                false -> (it as NormalTrack).muteDisable()
            }
        }
    }

    private fun highlightAllTracks() {
        children.forEach {
            (it as NormalTrack).waveFormBox.children.forEach {file->
                (file as WaveFormFile).clickFile()
            }
        }
    }

    fun shiftPressed() {
        _trackListStateFlow.state = _trackListStateFlow.state.copy(shiftPressed = true)
    }

    fun shiftReleased() {
        _trackListStateFlow.state = _trackListStateFlow.state.copy(shiftPressed = false)
    }

    fun dropDownBoxOpened() {
        _trackListStateFlow.state = _trackListStateFlow.state.copy(dropDownOpen = true)
    }

    fun dropDownBoxClosed() {
        _trackListStateFlow.state = _trackListStateFlow.state.copy(dropDownOpen = false)
    }

    fun textFieldOpen() {
        _trackListStateFlow.state = _trackListStateFlow.state.copy(textOpen = true)
    }

    fun textFieldClosed() {
        _trackListStateFlow.state = _trackListStateFlow.state.copy(textOpen = false)
    }

    fun filePressed(file: Widget) {
        if (_trackListStateFlow.state.filesHighlighted.files.size <= 1) {
            /* highlight group does not exist */
            if (!_trackListStateFlow.state.shiftPressed) {
                unclickAllFiles()
            }
            (file as WaveFormFile).clickFile()
        }
        else {
            /* highlight group exists */
            if (_trackListStateFlow.state.filesHighlighted.files.contains(file as WaveFormFile)) {
                increaseBrightnessOnHighlightGroup()
                _trackListStateFlow.state.filesHighlighted.pressed = true
                _trackListStateFlow.state.filesHighlighted.hasMovedSinceLastPress = false
            }
            else {
                if (!_trackListStateFlow.state.shiftPressed)
                    unclickAllFiles()
                file.clickFile()
            }
        }
    }

    fun fileReleased(file: Widget) {
        if (_trackListStateFlow.state.filesHighlighted.files.size <= 1) {
            /* highlight group does not exist */
            /* do nothing */
        }
        else {
            /* highlight group exists */
            if (_trackListStateFlow.state.filesHighlighted.hasMovedSinceLastPress) {
                decreaseBrightnessOnHighlightGroup()
                _trackListStateFlow.state.filesHighlighted.pressed = false
            }
            else {
                if (!_trackListStateFlow.state.shiftPressed) {
                    if (_trackListStateFlow.state.filesHighlighted.files.contains(file as WaveFormFile)) {
                        unclickAllFiles()
                        file.clickFile()
                    }
                }
            }
        }
    }

    fun increaseBrightnessOnHighlightGroup() {
        _trackListStateFlow.state.filesHighlighted.files.forEach {
            it.toggleBrightness(true)
        }
    }

    fun decreaseBrightnessOnHighlightGroup() {
        _trackListStateFlow.state.filesHighlighted.files.forEach {
            it.toggleBrightness(false)
        }
    }

    fun addToFilesHighlighted(file: Widget) {
        _trackListStateFlow.state.filesHighlighted.files.add(file as WaveFormFile)
    }

    fun removeFromFilesHighlighted(file: Widget) {
        _trackListStateFlow.state.filesHighlighted.files.remove(file as WaveFormFile)
    }

    fun mouseClicked() {
        if (_trackListStateFlow.state.textOpen) {
            exitTextFields()
        }
        else if (_trackListStateFlow.state.dropDownOpen) {
            removeDropDownBoxes()
        }
        else if (_trackListStateFlow.state.infoBoxOpen) {

        }
    }

    private fun printState() {
        val currentState =
            KeyState(
                shiftPressed = _trackListStateFlow.state.shiftPressed,
                textOpen = _trackListStateFlow.state.textOpen,
                dropDownOpen = _trackListStateFlow.state.dropDownOpen,
                infoBoxOpen = _trackListStateFlow.state.infoBoxOpen,
                filesHighlighted = _trackListStateFlow.state.filesHighlighted)
        Logger.debug(javaClass.simpleName, currentState.toString(), 4)
    }

    fun keyPressed(event: KeyEvent) {
        if (_trackListStateFlow.state.textOpen) {
            if (event.code == KeyCode.BACK_SPACE) {
                broadcastBackSpace()
            }
            else if ((event.code.isLetterKey || event.code.isWhitespaceKey || event.code.isDigitKey)
                && event.code != KeyCode.ENTER) {
                broadcastCharacter(event)
            }
            else if (event.code == KeyCode.ENTER) {
                mouseClicked()
            }
        }
        else if (_trackListStateFlow.state.infoBoxOpen) {
            if (event.code == KeyCode.ENTER) {
                mouseClicked()
            }
        }
        else if (_trackListStateFlow.state.dropDownOpen) {
            if (event.code == KeyCode.ENTER) {
                mouseClicked()
            }
        }
        else {
            if (event.code == KeyCode.SHIFT) {
                shiftPressed()
            }
            else if (event.code == KeyCode.DELETE || event.code == KeyCode.BACK_SPACE) {
                deletePressed()
            }
            else if (event.code == KeyCode.SPACE) {
                spacePressed()
            }
            else if (event.code == KeyCode.A && event.isShortcutDown) {
                highlightAllTracks()
            }
        }
    }

    fun keyReleased(event: KeyEvent) {
        if (event.code == KeyCode.SHIFT) {
            shiftReleased()
        }
    }
}