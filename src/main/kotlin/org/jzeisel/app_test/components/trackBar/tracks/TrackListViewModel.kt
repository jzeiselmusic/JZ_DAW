package org.jzeisel.app_test.components.trackBar.tracks

import javafx.application.Platform
import javafx.beans.property.ReadOnlyDoubleProperty
import javafx.scene.layout.StackPane
import javafx.stage.Stage
import org.jzeisel.app_test.audio.AudioInputManager
import org.jzeisel.app_test.components.Widget
import org.jzeisel.app_test.logger.Logger
import org.jzeisel.app_test.util.Observable
import org.jzeisel.app_test.util.ObservableListener
import kotlin.properties.Delegates

class TrackListViewModel(val root: StackPane, val stage: Stage): Widget {
    companion object {
        const val TAG = "TrackList"
        const val LEVEL = 0
    }
    val stageWidthProperty: ReadOnlyDoubleProperty = stage.widthProperty()
    val stageHeightProperty: ReadOnlyDoubleProperty = stage.heightProperty()
    var trackHeight: Double by Delegates.observable(100.0) {
        _, old, new ->
    }
    var trackWidth: Double by Delegates.observable(stageWidthProperty.value) {
        _, _, new ->
        for (child in children) {
            (child as NormalTrack).trackWidth = new
        }
        masterTrack.trackWidth = new
    }
    /* initial offsets */
    var masterOffsetY = -(stage.height / 2.0) + (trackHeight / 2.0) + 4.0
    val addButtonOffset = 45.0
    val inputButtonsOffset = 75.0
    val vuMeterOffset = 120.0
    var labelDividerOffset = -stageWidthProperty.value / 2.0 + 20.0
    var currentDividerOffset = Observable(-stageWidthProperty.value / 2.0 + 155.0)
    /*      *****      */
    val audioInputManager = AudioInputManager(this)

    private val masterTrack: MasterTrack = MasterTrack(root,this)
    init {
        currentDividerOffset.addListener(masterTrack as ObservableListener<Double>)
        stageWidthProperty.addListener { _, _, new ->
            trackWidth = new as Double
        }
        stageHeightProperty.addListener {_, old, new ->
            for (child in children) {
                (child as NormalTrack).trackOffsetY -= (new as Double - old as Double)/2.0
            }
            masterTrack.trackOffsetY -= (new as Double - old as Double)/2.0
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
    }

    val numChildren: Int
        get() = children.size

    override fun addChild(child: Widget) {
        children = children.toMutableList().apply {
            add((child as NormalTrack).index, child)
        }
        currentDividerOffset.addListener(child as ObservableListener<Double>)
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
                (child as NormalTrack).index + 1, child as Track)
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
        }
    }

    fun setTrackAudioInput(index: Int, child: Widget) {
    }

    fun setTrackEnabled(child: Widget): Boolean {
        return true
    }

    fun setTrackDisabled(child: Widget) {
    }
}