package org.jzeisel.app_test.components.trackComponents

import javafx.event.EventHandler
import javafx.scene.input.MouseEvent
import javafx.scene.layout.StackPane
import javafx.scene.paint.Color
import javafx.scene.shape.Polygon
import javafx.scene.shape.Rectangle
import javafx.scene.shape.StrokeLineJoin
import org.jzeisel.app_test.audio.Device
import org.jzeisel.app_test.components.interfaces.TrackElement
import org.jzeisel.app_test.components.interfaces.widget.Widget
import org.jzeisel.app_test.components.NormalTrack
import org.jzeisel.app_test.components.Track
import org.jzeisel.app_test.components.ephemeral.dropdownbox.ExpandableDropDownBox
import org.jzeisel.app_test.components.interfaces.PressableButton
import org.jzeisel.app_test.components.interfaces.WindowElement
import org.jzeisel.app_test.components.interfaces.widget.NodeWidget
import org.jzeisel.app_test.util.*

class InputSelectArrow(private val root: StackPane, override val parent: Widget?)
            : NodeWidget, TrackElement, WindowElement, PressableButton {

    override val children: MutableList<Widget> = mutableListOf()
    private val parentTrack = parent as NormalTrack
    private val trackListViewModel = parentTrack.trackListViewModel
    private val trackListState = trackListViewModel._trackListStateFlow.state
    private val inputSelectRectangle = Rectangle(trackListState.buttonSize,
                                                trackListState.buttonSize,
                                                trackListState.generalPurple)
    private val inputSelectArrow = Polygon(0.0, 0.0,
                                        8.0, 0.0,
                                        4.0, -4.0)
    private var dropDownBox: ExpandableDropDownBox? = null

    private val deviceList: List<Device>
        get() {
            trackListViewModel.audioViewModel.getInputDeviceList()?.let {
                return it
            }
            return listOf()
        }

    override val mousePressEvent = EventHandler<MouseEvent> {
        animateObjectScale(1.0, 0.9, inputSelectRectangle, 80.0)
        trackListViewModel.dropDownBoxOpened()
    }

    override val mouseReleaseEvent = EventHandler<MouseEvent> {
        animateObjectScale(0.9, 1.0, inputSelectRectangle, 80.0)
        runLater(50.0) {
            val deviceList = trackListViewModel.audioViewModel.getInputDeviceList()
            val inputDevice = deviceList?.get(trackListViewModel.audioViewModel.defaultInputIndex)
            inputDevice?.let {device ->
                val deviceBoxEntryList = List(1) {
                    BoxEntry(name = device.name,
                        boxEntrySubList = device.channels.map { BoxEntry(it.name, null)})
                }
                dropDownBox = ExpandableDropDownBox(
                    root, deviceBoxEntryList, parentTrack, ::selectionChosen,
                    inputSelectRectangle.translateX, inputSelectRectangle.translateY,
                    trackListViewModel, false)
                dropDownBox!!.addMeToScene(root)
            }
        }
    }
    init {
        inputSelectRectangle.translateX = -(parentTrack.initialTrackWidth/2.0) + trackListState.inputButtonsOffset
        inputSelectRectangle.translateY = parentTrack.trackOffsetY + trackListState.verticalDistancesBetweenWidgets
        inputSelectRectangle.arcWidth = trackListState.arcSize
        inputSelectRectangle.arcHeight = trackListState.arcSize
        inputSelectRectangle.stroke = trackListState.strokeColor
        inputSelectRectangle.strokeWidth = trackListState.strokeSize
        inputSelectRectangle.viewOrder = viewOrderFlip - 0.31
        inputSelectRectangle.onMousePressed = mousePressEvent
        inputSelectRectangle.onMouseReleased = mouseReleaseEvent

        inputSelectArrow.fill = Color.BLACK
        inputSelectArrow.translateX = -(parentTrack.initialTrackWidth/2.0) + trackListState.inputButtonsOffset - 0.5
        inputSelectArrow.translateY = parentTrack.trackOffsetY + trackListState.verticalDistancesBetweenWidgets
        inputSelectArrow.rotate = 180.0
        inputSelectArrow.stroke = trackListState.strokeColor
        inputSelectArrow.strokeWidth = 1.5
        inputSelectArrow.strokeLineJoin = StrokeLineJoin.ROUND
        inputSelectArrow.viewOrder = viewOrderFlip - 0.32
        inputSelectArrow.isMouseTransparent = true
    }

    override fun respondToChange(broadcastType: BroadcastType, old: Double, new: Double) {
        when (broadcastType) {
            BroadcastType.STAGE_WIDTH -> respondToWidthChange(old, new)
            BroadcastType.STAGE_HEIGHT -> respondToHeightChange(old, new)
            BroadcastType.INDEX -> respondToIndexChange(old, new)
            BroadcastType.DIVIDER -> {}
            BroadcastType.SCROLL -> {}
        }
    }

    override fun registerForBroadcasts() {
        trackListViewModel.registerForHeightChanges(this)
        trackListViewModel.registerForWidthChanges(this)
        parentTrack.registerForIndexChanges(this)
    }

    override fun unregisterForBroadcasts() {
        trackListViewModel.unregisterForWidthChanges(this)
        trackListViewModel.unregisterForHeightChanges(this)
        parentTrack.unregisterForIndexChanges(this)
    }

    override fun addChild(child: Widget) {
    }

    fun removeDropDownBox(root: StackPane) {
        dropDownBox?.let {
            if (!it.isHovering) {
                it.removeMeFromScene(root)
                dropDownBox = null
                trackListViewModel.dropDownBoxClosed()
            }
        }
    }

    override fun addMeToScene(root: StackPane) {
        runLater {
            registerForBroadcasts()
            root.children.add(inputSelectRectangle)
            root.children.add(inputSelectArrow)
        }
    }

    override fun removeMeFromScene(root: StackPane) {
        runLater {
            removeDropDownBox(root)
            unregisterForBroadcasts()
            dropDownBox?.removeMeFromScene(root)
            for (child in children) {
                child.removeMeFromScene(root)
            }
            children.clear()
            root.children.remove(inputSelectArrow)
            root.children.remove(inputSelectRectangle)
        }
    }

    private fun selectionChosen(index: List<Int>) {
        trackListViewModel.setTrackDeviceAndChannel(
            parentTrack.trackId, index[0], index[1])
    }

    override fun respondToHeightChange(old: Double, new: Double) {
        ((new - old)/2.0).let {
            inputSelectRectangle.translateY -= it
            inputSelectArrow.translateY -= it
        }
    }

    override fun respondToWidthChange(old: Double, new: Double) {
        ((new - old)/2.0).let {
            inputSelectRectangle.translateX -= it
            inputSelectArrow.translateX -= it
        }
    }

    override fun respondToIndexChange(old: Double, new: Double) {
        (parentTrack.trackOffsetY + trackListState.verticalDistancesBetweenWidgets).let {
            inputSelectRectangle.translateY = it
            inputSelectArrow.translateY = it
        }
    }
}