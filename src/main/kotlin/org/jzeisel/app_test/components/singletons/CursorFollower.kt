package org.jzeisel.app_test.components.singletons

import javafx.scene.layout.StackPane
import javafx.scene.paint.Color
import javafx.scene.shape.Polygon
import javafx.scene.shape.Rectangle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.jzeisel.app_test.viewmodel.TrackListViewModel
import org.jzeisel.app_test.components.interfaces.TrackElement
import org.jzeisel.app_test.components.interfaces.WindowElement
import org.jzeisel.app_test.components.interfaces.widget.SingularWidget
import org.jzeisel.app_test.stateflow.TrackListState
import org.jzeisel.app_test.stateflow.TrackListStateFlow
import org.jzeisel.app_test.util.*

object CursorFollower: SingularWidget, TrackElement, WindowElement {

    private lateinit var trackListViewModel: TrackListViewModel
    private lateinit var trackListState: TrackListState
    private lateinit var trackListFlow: TrackListStateFlow
    private val waveFormOffset: Double
        get() {
            return trackListViewModel._trackListStateFlow.state.waveFormOffset
        }
    var isAdded = false
    private var rectangleWidth = 1.8
    private const val zValCursor = viewOrderFlip - 0.16
    private const val zValCursorTriangle = viewOrderFlip - 0.165
    private val rectangleHeight: Double
        get() {
            return trackListFlow.numNormalTracks * trackListFlow.state.trackHeight
        }
    private val rectangleTranslateY: Double
        get() {
            return trackListFlow.topOfTracks + rectangleHeight /2.0
        }

    private lateinit var cursorRectangle: Rectangle
    private lateinit var cursorPointer: Polygon
    private var numMoves = 0

    // pixel distance from start of track waveform
    var currentOffsetX = 0.0
        set(value) {
            field = value
            if (trackListFlow.state.playBackStarted && trackListViewModel.isMetronomeEnabled()) {
                if (value % trackListFlow.state.pixelsInABeat < 1) {
                    animateObjectColor(Color.DARKGRAY, Color.WHITE, cursorRectangle, 350.0)
                }
            }
        }

    override fun addMeToScene(root:StackPane) {}

    fun addMeToScene(root: StackPane, offsetX: Double) {
        /* offset x is distance to the right from track divider offset */
        registerForBroadcasts()
        currentOffsetX = offsetX
        cursorRectangle = Rectangle(rectangleWidth, rectangleHeight, trackListState.generalGray)
        cursorRectangle.opacity = 1.0
        cursorRectangle.translateY = rectangleTranslateY
        cursorRectangle.translateX = trackListFlow.state.currentDividerOffset.getValue() + offsetX - waveFormOffset
        cursorRectangle.viewOrder = zValCursor
        cursorRectangle.fill = Color.DARKGRAY
        cursorRectangle.isMouseTransparent = true
        root.children.add(cursorRectangle)

        cursorPointer = Polygon(0.0, 0.0,
                                12.0, 0.0,
                                6.0, -8.0)
        cursorPointer.fill = Color.LIGHTGOLDENRODYELLOW
        cursorPointer.stroke = trackListState.strokeColor
        cursorPointer.strokeWidth = 1.5
        cursorPointer.translateY =  cursorRectangle.translateY -
                                    cursorRectangle.height/2.0 +
                                    cursorPointer.layoutBounds.height/2.0 - 1.0
        cursorPointer.translateX = cursorRectangle.translateX
        cursorPointer.rotate = 180.0
        cursorPointer.viewOrder = zValCursorTriangle
        root.children.add(cursorPointer)
        isAdded = true
    }

    override fun removeMeFromScene(root: StackPane) {
        runLater {
            unregisterForBroadcasts()
            root.children.remove(cursorRectangle)
            root.children.remove(cursorPointer)
        }
    }

    override fun respondToChange(broadcastType: BroadcastType, old: Double, new: Double) {
        when (broadcastType) {
            BroadcastType.DIVIDER -> {
                cursorRectangle.translateX = trackListFlow.state.currentDividerOffset.getValue() + currentOffsetX - waveFormOffset
                cursorPointer.translateX = cursorRectangle.translateX
            }
            BroadcastType.STAGE_WIDTH -> { respondToWidthChange(old, new) }
            BroadcastType.STAGE_HEIGHT -> { respondToHeightChange(old, new) }
            BroadcastType.INDEX -> {}
            BroadcastType.SCROLL -> { respondToScrollChange(new) }
        }
    }

    override fun registerForBroadcasts() {
        trackListViewModel.registerForDividerOffsetChanges(this)
        trackListViewModel.registerForWidthChanges(this)
        trackListViewModel.registerForHeightChanges(this)
        trackListViewModel.registerForScrollChanges(this)
    }

    override fun unregisterForBroadcasts() {
        trackListViewModel.unregisterForWidthChanges(this)
        trackListViewModel.unregisterForHeightChanges(this)
        trackListViewModel.unregisterForDividerOffsetChanges(this)
        trackListViewModel.unregisterForScrollChanges(this)
    }

    fun updateFromTrackList(root: StackPane) {
        runLater {
            cursorRectangle.height = rectangleHeight
            cursorRectangle.translateY = rectangleTranslateY
            cursorRectangle.translateX = trackListFlow.state.currentDividerOffset.getValue() + currentOffsetX - waveFormOffset
            cursorPointer.translateX = cursorRectangle.translateX
        }
    }

    fun initialize(trackListViewModel: TrackListViewModel){
        CursorFollower.trackListViewModel = trackListViewModel
        trackListState = trackListViewModel._trackListStateFlow.state
        trackListFlow = trackListViewModel._trackListStateFlow
    }

    @Synchronized
    override fun respondToHeightChange(old: Double, new: Double) {
        ((new - old) / 2.0).let {
            cursorRectangle.translateY -= it
            cursorPointer.translateY -= it
        }
    }

    @Synchronized
    override fun respondToWidthChange(old: Double, new: Double) {
        ((new - old) / 2.0).let {
            cursorRectangle.translateX -= it
            cursorPointer.translateX -= it
        }
    }

    override fun respondToIndexChange(old: Double, new: Double) {
        /* not necessary for cursor follower */
    }

    @Synchronized
    fun respondToScrollChange(deltaX: Double) {
        runLater {
            cursorRectangle.translateX -= deltaX
            cursorPointer.translateX -= deltaX
        }
    }

    @Synchronized
    fun updateLocation(offsetX: Double) {
        /* update location and also alert audio engine */
        runLater {
            currentOffsetX = if (offsetX < 0.0) 0.0 else offsetX
            trackListViewModel.updateCursorOffsetFromWaveformStart(currentOffsetX)
            cursorRectangle.translateX = trackListFlow.state.currentDividerOffset.getValue() + currentOffsetX - waveFormOffset
            cursorPointer.translateX = cursorRectangle.translateX
        }
    }

    @Synchronized
    fun moveLocationForward(shiftX: Double) {
        /* move cursor forward but dont tell audio engine */
        runLater {
            CoroutineScope(Dispatchers.Default).launch {
                currentOffsetX += shiftX
                cursorRectangle.translateX =
                    trackListFlow.state.currentDividerOffset.getValue() + currentOffsetX - waveFormOffset
                cursorPointer.translateX = cursorRectangle.translateX
                numMoves = 0
            }
        }
    }
}