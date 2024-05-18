package org.jzeisel.app_test.components.trackComponents

import javafx.scene.layout.StackPane
import javafx.scene.paint.Color
import javafx.scene.shape.Rectangle
import org.jzeisel.app_test.components.interfaces.TrackElement
import org.jzeisel.app_test.components.interfaces.WindowElement
import org.jzeisel.app_test.components.interfaces.widget.NodeWidget
import org.jzeisel.app_test.components.interfaces.widget.Widget
import org.jzeisel.app_test.util.*
import kotlin.math.roundToInt

class WaveFormFile(override val parent: Widget) :
    NodeWidget, TrackElement, WindowElement {
    /* every waveformfile represents a single .wav file with audio */
    private lateinit var root: StackPane

    private val parentWaveBox = parent as WaveFormBox
    private val parentTrack = parentWaveBox.parentTrack
    private val trackListViewModel = parentTrack.trackListViewModel
    private val trackListState = parentTrack.trackListState
    private val tempo = trackListViewModel.audioViewModel.tempo
    private val sampleRate = trackListViewModel.audioViewModel.sampleRate
    enum class RecordingState {
        EMPTY, RECORDING, RECORDED
    }

    /* can be changed and moved around once track has been recorded */
    private var currentPixelOffset: Double? = null

    private var recordingState = RecordingState.EMPTY

    private val trackBackgroundList = mutableListOf<Rectangle>()
    private val trackWaveFormList = mutableListOf<Rectangle>()

    override val children = mutableListOf<Widget>()

    private val bgViewOrder = viewOrderFlip - 0.13
    private val wfViewOrder = viewOrderFlip - 0.14

    private var numBuffers = 0
    private var numPixels = 0.0

    override fun addChild(child: Widget) {
        /* will have no children */
    }

    override fun addMeToScene(root: StackPane) {
        /* save root to start adding rectangles to scene */
        this.root = root
    }

    override fun removeMeFromScene(root: StackPane) {
        /* when user wants to delete a recorded file from the track */
    }

    override fun respondToIndexChange(old: Double, new: Double) {

    }

    override fun respondToHeightChange(old: Double, new: Double) {

    }

    override fun respondToWidthChange(old: Double, new: Double) {

    }

    override fun respondToChange(broadcastType: BroadcastType, old: Double, new: Double) {

    }

    override fun registerForBroadcasts() {

    }

    override fun unregisterForBroadcasts() {

    }

    fun startRecording(pixelOffset: Double) {
        recordingState = RecordingState.RECORDING
        currentPixelOffset = pixelOffset
    }

    fun stopRecording() {
        recordingState = RecordingState.RECORDED
    }

    fun processBuffer(dbLevel: Double, numSamples: Int) {
        /* called by waveform box when new samples are ready to be printed */
        if (recordingState == RecordingState.RECORDING) {
            val pixels = samplesToPixels(numSamples, tempo, sampleRate, trackListState.pixelsInABeat)
            val levelHeight = scaleNumber(dbLevel, trackListState.trackHeight, 8.0)
            numPixels += pixels
            if (numPixels >= 5) {
                val bgRect = Rectangle(5.0, trackListState.trackHeight - 8, Color.WHITESMOKE.darker())
                bgRect.translateY = parentTrack.trackOffsetY
                bgRect.translateX =
                    (trackListState.currentDividerOffset.getValue() + currentPixelOffset!! + bgRect.width / 2.0)
                bgRect.strokeWidth = 0.0
                bgRect.viewOrder = bgViewOrder

                val wfRect = Rectangle(5.0, levelHeight, Color.BLACK)
                wfRect.translateY = parentTrack.trackOffsetY
                wfRect.translateX =
                    (trackListState.currentDividerOffset.getValue() + currentPixelOffset!! + bgRect.width / 2.0)
                wfRect.strokeWidth = 0.0
                wfRect.viewOrder = wfViewOrder

                currentPixelOffset = currentPixelOffset!! + 5.0

                numPixels = 0.0

                trackBackgroundList.add(bgRect)
                trackWaveFormList.add(wfRect)
                runLater {
                    root.children.addAll(bgRect, wfRect)
                }
            }
        }
    }
}