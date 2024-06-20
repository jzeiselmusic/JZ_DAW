package org.jzeisel.app_test.components.trackComponents

import javafx.event.EventHandler
import javafx.scene.Cursor
import javafx.scene.input.ScrollEvent
import javafx.scene.layout.StackPane
import javafx.scene.paint.Color
import javafx.scene.shape.Rectangle
import javafx.scene.shape.StrokeLineJoin
import org.jzeisel.app_test.components.interfaces.TrackElement
import org.jzeisel.app_test.components.interfaces.widget.Widget
import org.jzeisel.app_test.components.MasterTrack
import org.jzeisel.app_test.components.NormalTrack
import org.jzeisel.app_test.components.Track
import org.jzeisel.app_test.components.interfaces.WaveElement
import org.jzeisel.app_test.components.interfaces.WindowElement
import org.jzeisel.app_test.components.interfaces.widget.NodeWidget
import org.jzeisel.app_test.components.singletons.CursorFollower
import org.jzeisel.app_test.stateflow.TrackListState
import org.jzeisel.app_test.stateflow.TrackListStateFlow
import org.jzeisel.app_test.util.*
import org.jzeisel.app_test.viewmodel.TrackListViewModel
import kotlin.math.abs
import kotlin.math.roundToInt

class WaveFormBox(override val parent: Widget) :
    NodeWidget, TrackElement, WindowElement, WaveElement {

    private lateinit var root: StackPane
    override val children: MutableList<Widget> = mutableListOf()
    val parentTrack: Track
        get() { return parent as Track }
    val trackListViewModel: TrackListViewModel
        get() { return parentTrack.trackListViewModel }
    val trackListFlow: TrackListStateFlow
        get() { return trackListViewModel._trackListStateFlow }
    val trackListState : TrackListState
        get() { return parentTrack.trackListState }

    val waveFormWidth = trackListState.waveFormWidth
    val trackRectangle = Rectangle(waveFormWidth,
        parentTrack.initialTrackHeight,
        trackListState.generalPurple)

    private val measureDividers = mutableListOf<Rectangle>()
    private val beatDividers = mutableListOf<Rectangle>()
    private val ticksForMasterTrack = mutableListOf<Rectangle>()

    private val zValBase = viewOrderFlip - 0.1
    private val zValMeasures = viewOrderFlip - 0.11
    private val zValTicks = viewOrderFlip - 0.12
    private val originalLocation: Double get() { return trackListFlow.waveFormTranslateX }
    private var lastMousePressLocationX: Double = 0.0
    private var highlightRectangle = Rectangle(0.0, 0.0, Color.TRANSPARENT)
    private val farthestLocation: Double
        get() {
            return (trackListFlow.waveFormTranslateX
                    - trackListState.waveFormWidth
                    + (trackListState.observableStageWidth.getValue()
                    - trackListState.initialTrackDividerWidth))
        }

    private val scrollEvent = EventHandler<ScrollEvent> {
        if (((trackRectangle.translateX + it.deltaX) > originalLocation)&& it.deltaX > 0) {
            trackListViewModel.onWaveFormBoxScroll((trackRectangle.translateX - originalLocation)/4.0)
        }
        else if (((trackRectangle.translateX + it.deltaX) < farthestLocation) && it.deltaX < 0) {
            trackListViewModel.onWaveFormBoxScroll((trackRectangle.translateX - farthestLocation)/4.0)
        }
        else {
            trackListViewModel.onWaveFormBoxScroll(-it.deltaX/3.0)
        }
    }

    init {
        trackRectangle.onScroll = scrollEvent
        trackRectangle.translateY = parentTrack.trackOffsetY
        trackRectangle.translateX = trackListFlow.waveFormTranslateX - trackListState.waveFormOffset
        trackRectangle.opacity = 0.8
        trackRectangle.stroke = trackListState.strokeColor
        trackRectangle.strokeWidth = 0.5
        trackRectangle.strokeLineJoin = StrokeLineJoin.MITER
        trackRectangle.viewOrder = zValBase
        trackRectangle.onMousePressed = EventHandler {
            /* calculate the nearest allowable x offset given increments of 25 / 22050 */
            val quantizedLoc = (it.x / trackListState.incrementSize).roundToInt().toDouble() * trackListState.incrementSize
            lastMousePressLocationX = trackListState.currentDividerOffset.getValue() + quantizedLoc - trackListState.waveFormOffset
            trackListViewModel.broadcastMouseClickOnWaveFormBox(quantizedLoc)
            trackListViewModel.unclickAllFiles()
        }
        trackRectangle.onMouseDragged = EventHandler {
            val totalDistanceX = (trackListState.currentDividerOffset.getValue() + it.x - trackListState.waveFormOffset) - lastMousePressLocationX
            trackListViewModel.updateHighlightSection(totalDistanceX, lastMousePressLocationX)
            val quantizedLoc = (it.x / trackListState.incrementSize).roundToInt().toDouble() * trackListState.incrementSize
            trackListViewModel.broadcastMouseClickOnWaveFormBox(quantizedLoc)
        }

        trackRectangle.onMouseEntered = EventHandler {
            root.cursor = Cursor.TEXT
        }
        trackRectangle.onMouseExited = EventHandler {
            root.cursor = Cursor.DEFAULT
        }

        for (i in 0..(waveFormWidth/100).toInt()) {
            val measure = Rectangle(1.0, trackListState.trackHeight, trackListState.strokeColor)
            measure.opacity = 0.6
            measure.strokeWidth = 0.8
            measure.translateY = parentTrack.trackOffsetY
            measure.translateX = trackRectangle.translateX - waveFormWidth/2.0 + i*(trackListState.pixelsInABeat * 4)
            measure.isMouseTransparent = true
            measure.viewOrder = zValMeasures
            measureDividers.add(measure)
            if (parentTrack is MasterTrack) {
                val tick = Rectangle(1.2, 5.0, trackListState.generalGray.darker())
                tick.opacity = 1.0
                tick.strokeWidth = 0.8
                tick.arcHeight = 2.0
                tick.arcWidth = 2.0
                tick.stroke = color()
                tick.translateY = measure.translateY - trackListState.trackHeight/2.0 + tick.height/2.0 + 1.0
                tick.translateX = measure.translateX
                tick.isMouseTransparent = true
                tick.viewOrder = zValTicks
                ticksForMasterTrack.add(tick)
            }

            for (j in 1..3) {
                val beat = Rectangle(0.4, trackListState.trackHeight, trackListState.strokeColor)
                beat.opacity = 0.5
                beat.strokeWidth = 0.2
                beat.translateY = parentTrack.trackOffsetY
                beat.translateX = measure.translateX + (j * trackListState.pixelsInABeat)
                beat.isMouseTransparent = true
                beat.viewOrder = zValMeasures
                beatDividers.add(beat)
                if (parentTrack is MasterTrack) {
                    val tick = Rectangle(1.0, 3.0, trackListState.generalGray.darker())
                    tick.opacity = 1.0
                    tick.strokeWidth = 0.4
                    tick.arcHeight = 2.0
                    tick.arcWidth = 2.0
                    tick.stroke = trackListState.generalGray
                    tick.translateY = measure.translateY - trackListState.trackHeight/2.0 + tick.height/2.0 + 1.0
                    tick.translateX = beat.translateX
                    tick.isMouseTransparent = true
                    tick.viewOrder = zValTicks
                    ticksForMasterTrack.add(tick)
                }
            }
        }
    }

    override fun respondToChange(broadcastType: BroadcastType, old: Double, new: Double) {
        when(broadcastType) {
            BroadcastType.DIVIDER -> respondToDividerShift(new)
            BroadcastType.STAGE_WIDTH -> respondToWidthChange(old, new)
            BroadcastType.STAGE_HEIGHT -> respondToHeightChange(old, new)
            BroadcastType.INDEX -> respondToIndexChange(old, new)
            BroadcastType.SCROLL -> respondToScrollChange(new)
        }
    }

    override fun registerForBroadcasts() {
        trackListViewModel.registerForDividerOffsetChanges(this)
        trackListViewModel.registerForWidthChanges(this)
        trackListViewModel.registerForHeightChanges(this)
        trackListViewModel.registerForScrollChanges(this)
        if (parentTrack is NormalTrack) {
            (parentTrack as NormalTrack).registerForIndexChanges(this)
        }
    }

    override fun unregisterForBroadcasts() {
        trackListViewModel.unregisterForHeightChanges(this)
        trackListViewModel.unregisterForDividerOffsetChanges(this)
        trackListViewModel.unregisterForWidthChanges(this)
        trackListViewModel.unregisterForScrollChanges(this)
        if (parentTrack is NormalTrack) {
            (parentTrack as NormalTrack).unregisterForIndexChanges(this)
        }
    }

    private fun color(): Color? = trackListState.generalGray.darker()
    override fun addChild(child: Widget) {
    }

    override fun addMeToScene(root: StackPane) {
        this.root = root
        registerForBroadcasts()
        root.children.add(trackRectangle)
        root.children.add(highlightRectangle)
        for (measureDivider in measureDividers) {
            root.children.add(measureDivider)
        }
        for (tickDivider in beatDividers) {
            root.children.add(tickDivider)
        }
        for (beatTick in ticksForMasterTrack) {
            root.children.add(beatTick)
        }
        if (!CursorFollower.isAdded)
            CursorFollower.addMeToScene(root, trackListState.waveFormOffset)
    }

    override fun removeMeFromScene(root: StackPane) {
        children.forEach {
            (it as? WaveFormFile)?.let { file ->
                trackListViewModel.deleteAudioFile((parentTrack as NormalTrack).trackId, file.fileId)
                file.removeMeFromScene(root)
            }
        }
        runLater {
            unregisterForBroadcasts()
            root.children.remove(trackRectangle)
            root.children.remove(highlightRectangle)
            for (measureDivider in measureDividers) {
                root.children.remove(measureDivider)
            }
            for (tickDivider in beatDividers) {
                root.children.remove(tickDivider)
            }
            for (beatTick in ticksForMasterTrack) {
                root.children.remove(beatTick)
            }
            root.children.remove(highlightRectangle)
        }
    }

    override fun respondToHeightChange(old: Double, new: Double) {
        ((new - old) /2.0).let {
            trackRectangle.translateY -= it
            for (measureDivider in measureDividers) {
                measureDivider.translateY -= it
            }
            for (beat in beatDividers) {
                beat.translateY -= it
            }
            for (tick in ticksForMasterTrack) {
                tick.translateY -= it
            }
            highlightRectangle.translateY -= it
        }
    }

    override fun respondToWidthChange(old: Double, new: Double) {
        ((new - old)/2.0).let {
            trackRectangle.translateX -= it
            for (measureDivider in measureDividers) {
                measureDivider.translateX -= it
            }
            for (beat in beatDividers) {
                beat.translateX -= it
            }
            for (tick in ticksForMasterTrack) {
                tick.translateX -= it
            }
            highlightRectangle.translateX -= it
        }
    }

    override fun respondToIndexChange(old: Double, new: Double) {
        trackRectangle.translateY = parentTrack.trackOffsetY
        for (measureDivider in measureDividers) {
            measureDivider.translateY = parentTrack.trackOffsetY
        }
        for (tickDivider in beatDividers) {
            tickDivider.translateY = parentTrack.trackOffsetY
        }
        highlightRectangle.translateY = parentTrack.trackOffsetY
    }

    fun respondToDividerShift(newValue: Double) {
        val oldTranslate = trackRectangle.translateX
        trackRectangle.translateX = waveFormWidth / 2.0 + newValue
        val change = trackRectangle.translateX - oldTranslate
        for (measureDivider in measureDividers) {
            measureDivider.translateX += change
        }
        for (beat in beatDividers) {
            beat.translateX += change
        }
        for (tick in ticksForMasterTrack) {
            tick.translateX += change
        }
        highlightRectangle.translateX += change
    }

    override fun respondToScrollChange(deltaX: Double) {
        trackRectangle.translateX -= deltaX
        trackListViewModel.updateWaveFormOffset(trackListFlow.waveFormTranslateX - trackRectangle.translateX)
        for (measureDivider in measureDividers) {
            measureDivider.translateX -= deltaX
        }
        for (beat in beatDividers) {
            beat.translateX -= deltaX
        }
        for (tick in ticksForMasterTrack) {
            tick.translateX -= deltaX
        }
        highlightRectangle.translateX -= deltaX
    }

    fun startRecording(initialPixelOffset: Double, fileId: Int) {
        val newFile = WaveFormFile(this, fileId)
        newFile.addMeToScene(root)
        newFile.startRecording(initialPixelOffset)
        children.add(newFile)
    }

    fun stopRecording() {
        (children.lastOrNull() as? WaveFormFile)?.stopRecording()
    }

    fun deleteHighlightedFiles() {
        val indexesToDelete = mutableListOf<Int>()
        children.forEach {
            (it as? WaveFormFile)?.let { file->
                if (file.isHighlighted) {
                    trackListViewModel.deleteAudioFile((parentTrack as NormalTrack).trackId, file.fileId)
                    file.removeMeFromScene(root)
                    val index = children.indexOf(it)
                    indexesToDelete.add(index)
                }
            }
        }
        val filesToDelete = indexesToDelete.map { children[it] }
        filesToDelete.forEach {
            children.remove(it)
        }
    }

    fun processBuffer(dbLevel: Double, pixels: Double) {
        (children.lastOrNull() as? WaveFormFile)?.processBuffer(dbLevel, pixels)
    }

    fun unclickAllFiles() {
        children.forEach {
            (it as WaveFormFile).unclickFile()
        }
    }

    fun popFile(fileId: Int) : WaveFormFile {
        val file = children.filter { (it as WaveFormFile).fileId == fileId }[0]
        children.remove(file)
        return file as WaveFormFile
    }

    fun addAlreadyExistingFile(file: WaveFormFile) {
        file.mutableParent = this
        children.add(file)
    }

    fun updateHighlightSection(numIncrementsHighlight: Int, direction: Int, broadcastMousePressLocationX: Double) {
        highlightRectangle.width = abs(numIncrementsHighlight) * trackListState.incrementSize
        highlightRectangle.height = trackListState.trackHeight
        highlightRectangle.translateX = broadcastMousePressLocationX + (direction * highlightRectangle.width/2.0)
        highlightRectangle.translateY = parentTrack.trackOffsetY
        highlightRectangle.fill = Color.LIGHTGRAY
        highlightRectangle.opacity = 0.3
        highlightRectangle.viewOrder = viewOrderFlip - 0.105
        highlightRectangle.isMouseTransparent = true
    }
}