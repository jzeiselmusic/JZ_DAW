package org.jzeisel.app_test.components.trackComponents

import javafx.application.Platform
import javafx.event.EventHandler
import javafx.scene.layout.StackPane
import javafx.scene.paint.Color
import javafx.scene.shape.Polygon
import javafx.scene.shape.Rectangle
import javafx.scene.shape.StrokeLineJoin
import org.jzeisel.app_test.components.TrackComponentWidget
import org.jzeisel.app_test.components.Widget
import org.jzeisel.app_test.components.ephemeral.DropDownBox
import org.jzeisel.app_test.components.NormalTrack
import org.jzeisel.app_test.components.Track
import org.jzeisel.app_test.util.Logger
import org.jzeisel.app_test.util.BroadcastType
import org.jzeisel.app_test.util.ObservableListener
import org.jzeisel.app_test.util.viewOrderFlip

class InputSelectArrow(private val root: StackPane, override val parent: Widget?)
            : Widget, TrackComponentWidget, ObservableListener<Double> {

    override val children: MutableList<Widget> = mutableListOf()
    private val parentTrack = parent as Track
    private val trackListViewModel = parentTrack.trackListViewModel
    private var isDropDownBoxActive = false
    private val inputSelectRectangle = Rectangle(trackListViewModel.buttonSize,
                                                trackListViewModel.buttonSize,
                                                trackListViewModel.generalPurple)
    private val inputSelectArrow = Polygon(0.0, 0.0,
                                        8.0, 0.0,
                                        4.0, -4.0)
    init {
        inputSelectRectangle.translateX = -(parentTrack.initialTrackWidth/2.0) + trackListViewModel.inputButtonsOffset
        inputSelectRectangle.translateY = parentTrack.trackOffsetY + trackListViewModel.verticalDistancesBetweenWidgets
        inputSelectRectangle.arcWidth = trackListViewModel.arcSize
        inputSelectRectangle.arcHeight = trackListViewModel.arcSize
        inputSelectRectangle.stroke = trackListViewModel.strokeColor
        inputSelectRectangle.strokeWidth = trackListViewModel.strokeSize
        inputSelectRectangle.viewOrder = viewOrderFlip - 0.31

        inputSelectArrow.fill = Color.BLACK
        inputSelectArrow.translateX = -(parentTrack.initialTrackWidth/2.0) + trackListViewModel.inputButtonsOffset - 0.5
        inputSelectArrow.translateY = parentTrack.trackOffsetY + trackListViewModel.verticalDistancesBetweenWidgets
        inputSelectArrow.rotate = 180.0
        inputSelectArrow.stroke = trackListViewModel.strokeColor
        inputSelectArrow.strokeWidth = 1.5
        inputSelectArrow.strokeLineJoin = StrokeLineJoin.ROUND
        inputSelectRectangle.viewOrder = viewOrderFlip - 0.32
    }

    private val dropDownBox = DropDownBox(trackListViewModel.audioInputManager.allMixerNames,
                                  inputSelectRectangle, trackListViewModel,
                                  ::selectionChosen)

    init {
        inputSelectRectangle.onMouseReleased = EventHandler {
            dropDownBox.addMeToScene(root)
            isDropDownBoxActive = true
        }
        inputSelectArrow.onMouseReleased = EventHandler {
            dropDownBox.addMeToScene(root)
            isDropDownBoxActive = true
        }
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
        if (parentTrack is NormalTrack) {
            parentTrack.registerForIndexChanges(this)
        }
    }

    override fun unregisterForBroadcasts() {
        trackListViewModel.unregisterForWidthChanges(this)
        trackListViewModel.unregisterForHeightChanges(this)
        if (parentTrack is NormalTrack) {
            parentTrack.unregisterForIndexChanges(this)
        }
    }

    override fun addChild(child: Widget) {
    }

    fun removeDropDownBox(root: StackPane) {
        dropDownBox.removeMeFromScene(root)
    }

    override fun addMeToScene(root: StackPane) {
        Platform.runLater {
            registerForBroadcasts()
            root.children.add(inputSelectRectangle)
            root.children.add(inputSelectArrow)
        }
    }

    override fun removeMeFromScene(root: StackPane) {
        Platform.runLater {
            unregisterForBroadcasts()
            dropDownBox.removeMeFromScene(root)
            for (child in children) {
                child.removeMeFromScene(root)
            }
            children.clear()
            root.children.remove(inputSelectArrow)
            root.children.remove(inputSelectRectangle)
        }
    }

    private fun selectionChosen(index: Int) {
        Logger.debug(javaClass.simpleName, "chose index $index", 5)
        (parentTrack as NormalTrack).setAudioInputIndex(index)
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
        (parentTrack.trackOffsetY + trackListViewModel.verticalDistancesBetweenWidgets).let {
            inputSelectRectangle.translateY = it
            inputSelectArrow.translateY = it
        }
    }
}