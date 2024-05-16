package org.jzeisel.app_test.components.trackComponents

import javafx.scene.layout.StackPane
import org.jzeisel.app_test.components.interfaces.TrackElement
import org.jzeisel.app_test.components.interfaces.WindowElement
import org.jzeisel.app_test.components.interfaces.widget.NodeWidget
import org.jzeisel.app_test.components.interfaces.widget.Widget
import org.jzeisel.app_test.util.BroadcastType

class WaveFormFile(override val parent: Widget) :
    NodeWidget, TrackElement, WindowElement {
    /* every waveformfile represents a single .wav file with audio */

    private val parentWaveBox = parent as WaveFormBox
    private val parentTrack = parentWaveBox.parentTrack
    private val trackListViewModel = parentWaveBox.parentTrack.trackListViewModel
    private val trackListState = parentWaveBox.parentTrack.trackListState

    override val children = mutableListOf<Widget>()
    override fun addChild(child: Widget) {
        /* will have no children */
    }

    override fun addMeToScene(root: StackPane) {
        /* gets added to scene when track starts recording */
    }

    override fun removeMeFromScene(root: StackPane) {
        /* when user wants to delete a recorded file from the track */
    }

    override fun respondToIndexChange(old: Double, new: Double) {
        TODO("Not yet implemented")
    }

    override fun respondToHeightChange(old: Double, new: Double) {
        TODO("Not yet implemented")
    }

    override fun respondToWidthChange(old: Double, new: Double) {
        TODO("Not yet implemented")
    }

    override fun respondToChange(broadcastType: BroadcastType, old: Double, new: Double) {
        TODO("Not yet implemented")
    }

    override fun registerForBroadcasts() {
        TODO("Not yet implemented")
    }

    override fun unregisterForBroadcasts() {
        TODO("Not yet implemented")
    }
    
    fun updateLengthAndValue() {
        /* called by waveform box when new samples are ready to be printed */
    }
}