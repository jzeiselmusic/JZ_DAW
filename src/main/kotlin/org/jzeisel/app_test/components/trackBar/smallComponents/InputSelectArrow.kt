package org.jzeisel.app_test.components.trackBar.smallComponents

import javafx.application.Platform
import javafx.event.EventHandler
import javafx.scene.layout.StackPane
import javafx.scene.paint.Color
import javafx.scene.shape.Polygon
import javafx.scene.shape.Rectangle
import javafx.scene.shape.StrokeLineJoin
import org.jzeisel.app_test.components.TrackComponentWidget
import org.jzeisel.app_test.components.Widget
import org.jzeisel.app_test.components.dropdownbox.DropDownBox
import org.jzeisel.app_test.components.trackBar.tracks.NormalTrack
import org.jzeisel.app_test.components.trackBar.tracks.Track
import org.jzeisel.app_test.logger.Logger

class InputSelectArrow(private val root: StackPane, override val parent: Widget?)
            : Widget, TrackComponentWidget {
    companion object {
        const val TAG = "InputSelectArrow"
        const val LEVEL = 4
    }
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

        inputSelectArrow.fill = Color.BLACK
        inputSelectArrow.translateX = -(parentTrack.initialTrackWidth/2.0) + trackListViewModel.inputButtonsOffset - 0.5
        inputSelectArrow.translateY = parentTrack.trackOffsetY + trackListViewModel.verticalDistancesBetweenWidgets
        inputSelectArrow.rotate = 180.0
        inputSelectArrow.stroke = trackListViewModel.strokeColor
        inputSelectArrow.strokeWidth = 1.5
        inputSelectArrow.strokeLineJoin = StrokeLineJoin.ROUND
    }

    private val dropDownBox = DropDownBox(trackListViewModel.audioInputManager.allMixerNames,
                                  inputSelectRectangle, trackListViewModel,
                                  ::selectionChosen)

    init {
        inputSelectRectangle.onMouseReleased = EventHandler {
            Logger.debug(TAG, "input select rectangle clicked for track ${parentTrack.name}", LEVEL)
            dropDownBox.addMeToScene(root)
            isDropDownBoxActive = true
        }
        inputSelectArrow.onMouseReleased = EventHandler {
            Logger.debug(TAG, "input select arrow clicked for track ${parentTrack.name}", LEVEL)
            dropDownBox.addMeToScene(root)
            isDropDownBoxActive = true
        }

        Logger.debug(TAG, "instantiated input selector- parent is track ${parentTrack.name}", LEVEL)
    }
    override fun addChild(child: Widget) {
    }

    fun removeDropDownBox(root: StackPane) {
        dropDownBox.removeMeFromScene(root)
    }

    override fun addMeToScene(root: StackPane) {
        Platform.runLater {
            root.children.add(inputSelectRectangle)
            root.children.add(inputSelectArrow)
            Logger.debug(TAG, "rectangle and arrow added to scene", LEVEL)
        }
    }

    override fun removeMeFromScene(root: StackPane) {
        Platform.runLater {
            dropDownBox.removeMeFromScene(root)
            for (child in children) {
                child.removeMeFromScene(root)
            }
            children.clear()
            root.children.remove(inputSelectArrow)
            root.children.remove(inputSelectRectangle)
            Logger.debug(TAG, "arrow and rectangle removed from scene", LEVEL)
        }
    }

    private fun selectionChosen(index: Int) {
        Logger.debug(TAG, "chose index $index", LEVEL)
        (parentTrack as NormalTrack).setAudioInputIndex(index)
    }

    override fun respondToOffsetYChange(old: Double, new: Double) {
        inputSelectRectangle.translateY += new - old
        inputSelectArrow.translateY += new - old
        dropDownBox.updateTranslation(inputSelectRectangle.translateX, inputSelectRectangle.translateY)
    }

    override fun respondToWidthChange(old: Double, new: Double) {
        inputSelectRectangle.translateX -= (new - old)/2.0
        inputSelectArrow.translateX -= (new - old)/2.0
        dropDownBox.updateTranslation(inputSelectRectangle.translateX, inputSelectRectangle.translateY)
    }
}