package org.jzeisel.app_test.components.trackComponents

import javafx.event.EventHandler
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.input.MouseEvent
import javafx.scene.layout.StackPane
import javafx.scene.paint.Color
import javafx.scene.shape.Rectangle
import org.jzeisel.app_test.error.AudioError
import org.jzeisel.app_test.components.interfaces.TrackElement
import org.jzeisel.app_test.components.interfaces.widget.Widget
import org.jzeisel.app_test.components.NormalTrack
import org.jzeisel.app_test.components.Track
import org.jzeisel.app_test.components.interfaces.WindowElement
import org.jzeisel.app_test.components.interfaces.widget.NodeWidget
import org.jzeisel.app_test.util.*

class InputEnableButton(override val parent: Widget?)
    : NodeWidget, TrackElement, WindowElement {

    private val parentTrack = parent as Track
    private val trackListViewModel = parentTrack.trackListViewModel
    private val trackListState = trackListViewModel._trackListStateFlow.state
    private var isEnabled: Boolean = false
    private val buttonWidth = trackListState.buttonSize
    private val buttonHeight = trackListState.buttonSize
    private var buttonOffsetY = parentTrack.trackOffsetY - trackListState.verticalDistancesBetweenWidgets
    private val buttonOffsetX = -(trackListViewModel.stage.width / 2) + trackListState.inputButtonsOffset
    override val children = mutableListOf<Widget>()

    override fun addChild(child: Widget) {
        /* AddButton does not have any children */
    }

    private val buttonRect = Rectangle(buttonWidth, buttonHeight, Color.TRANSPARENT)
    private val iImage = Image("images/i_image.png")
    private var iImageView: ImageView = ImageView(iImage)

    private val mouseReleaseEvent = EventHandler<MouseEvent> {
        mouseReleaseLeft()
    }

    init {
        iImageView.fitHeight = trackListState.buttonSize - 5.0
        iImageView.isPreserveRatio = true
        iImageView.translateY = buttonOffsetY
        iImageView.translateX = buttonOffsetX
        buttonRect.translateY = buttonOffsetY
        buttonRect.translateX = buttonOffsetX
        buttonRect.arcWidth = trackListState.arcSize
        buttonRect.arcHeight = trackListState.arcSize
        buttonRect.stroke = trackListState.strokeColor
        buttonRect.strokeWidth = trackListState.strokeSize
        buttonRect.onMouseReleased = mouseReleaseEvent
        buttonRect.viewOrder = viewOrderFlip - 0.31
        iImageView.onMouseReleased = mouseReleaseEvent
        iImageView.viewOrder = viewOrderFlip - 0.32
    }

    override fun respondToChange(broadcastType: BroadcastType, old: Double, new: Double) {
        when(broadcastType) {
            BroadcastType.STAGE_WIDTH -> respondToWidthChange(old, new)
            BroadcastType.STAGE_HEIGHT -> respondToHeightChange(old, new)
            BroadcastType.INDEX -> respondToIndexChange(old, new)
            BroadcastType.DIVIDER -> {}
            BroadcastType.SCROLL -> {}
        }
    }

    override fun registerForBroadcasts() {
        trackListViewModel.registerForWidthChanges(this)
        trackListViewModel.registerForHeightChanges(this)
        if (parentTrack is NormalTrack) {
            parentTrack.registerForIndexChanges(this)
        }
    }

    override fun unregisterForBroadcasts() {
        trackListViewModel.unregisterForHeightChanges(this)
        trackListViewModel.unregisterForWidthChanges(this)
        if (parentTrack is NormalTrack) {
            parentTrack.unregisterForIndexChanges(this)
        }
    }

    private fun mouseReleaseLeft() {
        when (isEnabled) {
            true -> {
                isEnabled = false
                buttonRect.fill = Color.TRANSPARENT
                (parentTrack as NormalTrack).audioInputDisable()
            }
            false -> {
                val err = (parentTrack as NormalTrack).audioInputEnable()
                if (err == AudioError.SoundIoErrorNone) {
                    isEnabled = true
                    buttonRect.fill = Color.rgb(0xFF, 0x64, 0x40)
                    buttonRect.opacity = 0.9
                }
                else {
                    Logger.debug(javaClass.simpleName, err.readable, 5)
                }
            }
        }
    }

    override fun addMeToScene(root: StackPane) {
        registerForBroadcasts()
        root.children.add(buttonRect)
        root.children.add(iImageView)
    }

    override fun removeMeFromScene(root: StackPane) {
        runLater {
            unregisterForBroadcasts()
            root.children.remove(buttonRect)
            root.children.remove(iImageView)
        }
    }

    override fun respondToHeightChange(old: Double, new: Double) {
        ((new - old)/2.0).let {
            buttonRect.translateY -= it
            iImageView.translateY -= it
        }
    }

    override fun respondToWidthChange(old: Double, new: Double) {
        ((new - old)/2.0).let {
            buttonRect.translateX -= it
            iImageView.translateX -= it
        }
    }

    override fun respondToIndexChange(old: Double, new: Double) {
        buttonOffsetY = parentTrack.trackOffsetY - trackListState.verticalDistancesBetweenWidgets
        iImageView.translateY = buttonOffsetY
        buttonRect.translateY = buttonOffsetY
    }

}