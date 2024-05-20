package org.jzeisel.app_test.components.trackComponents

import javafx.event.EventHandler
import javafx.scene.input.MouseEvent
import javafx.scene.layout.StackPane
import javafx.scene.paint.Color
import javafx.scene.shape.Rectangle
import org.jzeisel.app_test.components.interfaces.TrackElement
import org.jzeisel.app_test.components.interfaces.WindowElement
import org.jzeisel.app_test.components.interfaces.widget.NodeWidget
import org.jzeisel.app_test.components.interfaces.widget.Widget
import org.jzeisel.app_test.util.*

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

    /* while recording, rectangles are created separately for each buffer */
    private val trackBackgroundRectangles = mutableListOf<Rectangle>()
    private val trackWaveformRectangles = mutableListOf<Rectangle>()

    private val wrappingRectangle = Rectangle()
    private val fillingRectangle = Rectangle()

    override val children = mutableListOf<Widget>()

    private val bgViewOrder = viewOrderFlip - 0.13
    private val wfViewOrder = viewOrderFlip - 0.14
    private val wrapperViewOrder = viewOrderFlip - 0.145

    private var numPixels = 0.0
    private var startingPixelOffset = 0.0
    private var totalPixelWidth = 0.0

    private val mousePressEvent = EventHandler<MouseEvent> {
        clickFile()
    }

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

    fun clickFile() {
        runLater(50.0) {
            for (rect in trackBackgroundRectangles) {
                rect.fill = Color.LIGHTGRAY.brighter().brighter()
            }
        }
    }

    fun unclickFile() {
        for (rect in trackBackgroundRectangles) {
            rect.fill = Color.WHITESMOKE.darker()
        }
    }

    fun startRecording(pixelOffset: Double) {
        recordingState = RecordingState.RECORDING
        startingPixelOffset = pixelOffset
        currentPixelOffset = pixelOffset
    }

    fun stopRecording() {
        recordingState = RecordingState.RECORDED

        runLater {
            fillingRectangle.height = trackListState.trackHeight - 8
            fillingRectangle.width = totalPixelWidth + 0.5
            fillingRectangle.translateY = parentTrack.trackOffsetY
            fillingRectangle.translateX =
                trackListState.currentDividerOffset.getValue() + startingPixelOffset + fillingRectangle.width / 2.0
            fillingRectangle.fill = Color.BLACK
            fillingRectangle.opacity = 0.0
            fillingRectangle.viewOrder = wrapperViewOrder
            fillingRectangle.isMouseTransparent = false
            fillingRectangle.onMousePressed = mousePressEvent

            wrappingRectangle.height = trackListState.trackHeight - 8
            wrappingRectangle.width = totalPixelWidth + 0.5
            wrappingRectangle.translateY = parentTrack.trackOffsetY
            wrappingRectangle.translateX =
                trackListState.currentDividerOffset.getValue() + startingPixelOffset + wrappingRectangle.width / 2.0
            wrappingRectangle.fill = null
            wrappingRectangle.isMouseTransparent = true
            wrappingRectangle.stroke = Color.BLACK
            wrappingRectangle.strokeWidth = 1.7
            wrappingRectangle.arcWidth = 3.0
            wrappingRectangle.arcHeight = 3.0
            wrappingRectangle.viewOrder = wrapperViewOrder
            root.children.addAll(fillingRectangle, wrappingRectangle)
        }
    }

    fun processBuffer(dbLevel: Double, numSamples: Int) {
        /* called by waveform box when new samples are ready to be printed */
        if (recordingState == RecordingState.RECORDING) {
            val pixels = samplesToPixels(numSamples, tempo, sampleRate, trackListState.pixelsInABeat)
            val levelHeight = scaleNumber(dbLevel, trackListState.trackHeight, 8.0)
            numPixels += pixels
            if (numPixels >= 1) {
                val bgRect = Rectangle(numPixels.toInt().toDouble()+1.0, trackListState.trackHeight - 8, Color.WHITESMOKE.darker())
                bgRect.translateY = parentTrack.trackOffsetY
                bgRect.translateX =
                    (trackListState.currentDividerOffset.getValue() + currentPixelOffset!! + bgRect.width / 2.0)
                bgRect.strokeWidth = 0.0
                bgRect.viewOrder = bgViewOrder
                bgRect.isMouseTransparent = true

                val wfRect = Rectangle(numPixels.toInt().toDouble() + 1.0, levelHeight, Color.DIMGREY.darker().darker())
                wfRect.translateY = parentTrack.trackOffsetY
                wfRect.translateX =
                    (trackListState.currentDividerOffset.getValue() + currentPixelOffset!! + bgRect.width / 2.0)
                wfRect.strokeWidth = 0.0
                wfRect.viewOrder = wfViewOrder
                wfRect.isMouseTransparent = true

                currentPixelOffset = currentPixelOffset!! + numPixels

                runLater {
                    if (recordingState == RecordingState.RECORDING) {
                        trackWaveformRectangles.add(wfRect)
                        trackBackgroundRectangles.add(bgRect)
                        root.children.addAll(bgRect, wfRect)
                        totalPixelWidth += numPixels
                        numPixels = 0.0
                    }
                }
            }
        }
    }
}