package org.jzeisel.app_test

import javafx.scene.paint.Color
import javafx.application.Platform
import javafx.beans.property.ReadOnlyDoubleProperty
import javafx.scene.input.KeyEvent
import javafx.scene.layout.StackPane
import javafx.stage.Stage
import org.jzeisel.app_test.audio.AudioInputManager
import org.jzeisel.app_test.components.Widget
import org.jzeisel.app_test.components.singletons.CursorFollower
import org.jzeisel.app_test.components.MasterTrack
import org.jzeisel.app_test.components.NormalTrack
import org.jzeisel.app_test.components.Track
import org.jzeisel.app_test.components.singletons.VerticalScrollBar
import org.jzeisel.app_test.util.BroadcastType
import org.jzeisel.app_test.util.Observable
import org.jzeisel.app_test.util.ObservableListener
import kotlin.properties.Delegates

class TrackListViewModel(val root: StackPane, val stage: Stage): Widget {
    val stageWidthProperty: ReadOnlyDoubleProperty = stage.widthProperty()
    val stageHeightProperty: ReadOnlyDoubleProperty = stage.heightProperty()

    var trackHeight: Double = 100.0
    var trackWidth: Double = stageWidthProperty.value
    /* sizes */
    val separationDistance = 45.0
    val inputNameBoxWidth = separationDistance*2.0
    val widgetSize = 20.0
    val vuMeterWidth = widgetSize
    val buttonSize = widgetSize
    val arcSize = 5.0
    val strokeSize = 1.2
    var verticalDistancesBetweenWidgets = 15.0
    /* colors */
    val strokeColor = Color.BLACK
    val generalPurple = Color.MEDIUMPURPLE.darker()
    val generalGray = Color.GRAY.brighter()
    val backgroundGray = Color.DIMGREY.darker().darker()
    /* initial offsets */
    var masterOffsetY = -(stage.height / 2.0) + (trackHeight / 2.0) + 12.0
    /* these are currently distance from left-hand size of stage */
    val addButtonOffset = separationDistance
    val inputButtonsOffset = addButtonOffset + 30.0
    val inputNameBoxOffset = inputButtonsOffset + separationDistance + inputNameBoxWidth / 2.0 - vuMeterWidth / 2.0
    val vuMeterOffset = inputNameBoxOffset + separationDistance + inputNameBoxWidth / 2.0 - vuMeterWidth / 2.0
    var labelDividerOffset = -stageWidthProperty.value / 2.0 + 20.0

    /* observable variables */
    var currentDividerOffset = Observable<Double>(-stageWidthProperty.value / 2.0 + 310.0)
    var testStageWidth = Observable<Double>(stageWidthProperty.value)
    var testStageHeight = Observable<Double>(stageHeightProperty.value)
    /*      *****      */
    val audioInputManager = AudioInputManager(this)

    private val masterTrack: MasterTrack = MasterTrack(root,this)

    private val cursorFollower = CursorFollower
    private val verticalScrollBar = VerticalScrollBar

    private var trackSelected: Track? = null

    init {
        cursorFollower.initialize(this)
        verticalScrollBar.initialize(this)
        stageWidthProperty.addListener { _, _, new ->
            testStageWidth.setValueAndNotify(new as Double, BroadcastType.STAGE_WIDTH)
            trackWidth = new
        }
        stageHeightProperty.addListener {_, _, new ->
            testStageHeight.setValueAndNotify(new as Double, BroadcastType.STAGE_HEIGHT)
        }
    }

    override val parent: Widget? = null
    /* all TrackList children will be NormalTracks */
    override var children : MutableList<Widget> by Delegates.observable(mutableListOf()) {
        _, old, new ->
        for (child in children) {
            val t = child as NormalTrack
            t.respondToChangeInTrackList(old, new)
        }
        cursorFollower.updateFromTrackList(root)
    }

    val numChildren: Int
        get() = children.size

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

    fun broadcastMouseClick(root: StackPane) {
        for (child in children) {
            val track = child as NormalTrack
            track.inputSelectArrow.removeDropDownBox(root)
            track.inputNameBox.exitTextField(root)
        }
        masterTrack.inputNameBox.exitTextField(root)
        showVerticalScrollBar()
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
        if (!cursorFollower.isShowing) {
            cursorFollower.addMeToScene(root, translateX)
        }
        else {
            cursorFollower.updateLocation(translateX)
        }
    }

    fun setTrackSelected(track: Track) {
        trackSelected = track
        track.setSelected()
    }

    fun showVerticalScrollBar() {
        verticalScrollBar.addMeToScene(root)
    }

    fun registerForWidthChanges(listener: ObservableListener<Double>) {
        testStageWidth.addListener(listener)
    }

    fun unregisterForWidthChanges(listener: ObservableListener<Double>) {
        testStageWidth.removeListener(listener)
    }

    fun registerForHeightChanges(listener: ObservableListener<Double>) {
        testStageHeight.addListener(listener)
    }

    fun unregisterForHeightChanges(listener: ObservableListener<Double>) {
        testStageHeight.removeListener(listener)
    }

    fun registerForDividerOffsetChanges(listener: ObservableListener<Double>) {
        currentDividerOffset.addListener(listener)
    }

    fun unregisterForDividerOffsetChanges(listener: ObservableListener<Double>) {
        currentDividerOffset.removeListener(listener)
    }

    fun setTrackAudioInput(index: Int, child: Widget) {
    }

    fun setTrackEnabled(child: Widget): Boolean {
        return true
    }

    fun setTrackDisabled(child: Widget) {
    }
}