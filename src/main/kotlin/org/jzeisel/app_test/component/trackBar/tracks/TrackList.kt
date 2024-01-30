package org.jzeisel.app_test.component.trackBar.tracks

import javafx.application.Platform
import javafx.beans.property.DoubleProperty
import javafx.beans.property.ReadOnlyDoubleProperty
import javafx.beans.value.ChangeListener
import javafx.scene.control.TextFormatter.Change
import javafx.scene.layout.StackPane
import javafx.stage.Stage
import org.jzeisel.app_test.audio.AudioInputManager
import org.jzeisel.app_test.component.Widget
import org.jzeisel.app_test.logger.Logger
import kotlin.properties.Delegates

class TrackList(val root: StackPane, val stage: Stage): Widget {
    companion object {
        const val TAG = "TrackList"
        const val LEVEL = 0
    }
    val stageWidthProperty: ReadOnlyDoubleProperty = stage.widthProperty()
    val stageHeightProperty: ReadOnlyDoubleProperty = stage.heightProperty()
    val trackHeight = 100.0
    var masterOffsetY = -(stage.height / 2.0) + (trackHeight / 2.0) + 4.0

    val audioInputManager = AudioInputManager()

    /* every track list should start with a Master Track */
    /* this will automatically add the master track */
    private val masterTrack: MasterTrack = MasterTrack(root,this)

    override val parent: Widget? = null
    /* all TrackList children will be NormalTracks */
    override val children = mutableListOf<Widget>()

    init {
        Logger.debug(TAG, "instantiated: master y-offset $masterOffsetY", LEVEL)
    }

    override fun addChild(child: Widget) {
        children.add(child)
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
            children.remove(children.last())
        }
    }

    fun getNumTracks(): Int {
        /* add one for master track */
        return children.size + 1
    }

    fun broadcastMouseClick() {
        for (child in children) {
            val track = child as NormalTrack
            track.inputSelectArrow.makeInputSelectInvisible()
        }
    }
}