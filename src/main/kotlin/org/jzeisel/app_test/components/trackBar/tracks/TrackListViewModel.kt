package org.jzeisel.app_test.components.trackBar.tracks

import javafx.application.Platform
import javafx.beans.property.ReadOnlyDoubleProperty
import javafx.scene.layout.StackPane
import javafx.stage.Stage
import org.jzeisel.app_test.audio.AudioInputManager
import org.jzeisel.app_test.components.Widget
import org.jzeisel.app_test.logger.Logger
import org.jzeisel.app_test.util.Observable
import org.jzeisel.app_test.util.ObservableList
import org.jzeisel.app_test.util.ObservableListener

class TrackListViewModel(val root: StackPane, val stage: Stage): Widget {
    companion object {
        const val TAG = "TrackList"
        const val LEVEL = 0
    }
    val stageWidthProperty: ReadOnlyDoubleProperty = stage.widthProperty()
    val stageHeightProperty: ReadOnlyDoubleProperty = stage.heightProperty()
    val trackHeight = 100.0
    var masterOffsetY = -(stage.height / 2.0) + (trackHeight / 2.0) + 4.0
    val addButtonOffset = 40.0
    val inputButtonsOffset = 70.0
    val vuMeterOffset = 115.0
    var labelDividerOffset = -stageWidthProperty.value / 2.0 + 20.0
    var currentDividerOffset = Observable(-stageWidthProperty.value / 2.0 + 150.0)

    val audioInputManager = AudioInputManager(this)

    private val masterTrack: MasterTrack = MasterTrack(root,this)
    init {
        currentDividerOffset.addListener(masterTrack as ObservableListener<Double>)
    }

    override val parent: Widget? = null
    /* all TrackList children will be NormalTracks */
    override val children = ObservableList<Widget>(0)

    init {
        Logger.debug(TAG, "instantiated: master y-offset $masterOffsetY", LEVEL)
    }

    override fun addChild(child: Widget) {
        children.addAndNotify(child)
        currentDividerOffset.addListener(child as ObservableListener<Double>)
        children.addListener(child as ObservableListener<Int>)
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
        val newTrack = NormalTrack(root, this, children.size.toString())
        newTrack.addMeToScene(root)
        addChild(newTrack)
        Logger.debug(TAG, "adding new track-- called by ${(child as Track).name}", LEVEL)
        Logger.debug(TAG, "current num tracks-- ${children.size} plus master", LEVEL)
    }

    fun removeTrack(child: Widget) {
        /* same comment as above */
        Platform.runLater {
            children.last().removeMeFromScene(root)
            children.removeListener(children.last() as ObservableListener<Int>)
            children.removeAndNotify(children.last())
        }
    }

    fun getNumTracks(): Int {
        /* add one for master track */
        return children.size + 1
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