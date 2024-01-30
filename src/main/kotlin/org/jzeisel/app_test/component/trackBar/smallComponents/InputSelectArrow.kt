package org.jzeisel.app_test.component.trackBar.smallComponents

import javafx.application.Platform
import javafx.event.EventHandler
import javafx.scene.layout.StackPane
import javafx.scene.paint.Color
import javafx.scene.shape.Polygon
import javafx.scene.shape.Rectangle
import javafx.scene.shape.StrokeLineJoin
import org.jzeisel.app_test.component.Widget
import org.jzeisel.app_test.component.dropdownbox.DropDownBox
import org.jzeisel.app_test.component.trackBar.tracks.Track
import org.jzeisel.app_test.logger.Logger

class InputSelectArrow(override val parent: Widget?) : Widget {
    companion object {
        const val TAG = "InputSelectArrow"
        const val LEVEL = 4
    }
    override val children: MutableList<Widget> = mutableListOf()
    private val parentTrack = parent as Track
    var isDropDownBoxActive = false
    val inputSelectRectangle = Rectangle(15.0, 15.0, Color.MEDIUMPURPLE.brighter())
    val inputSelectArrow = Polygon(0.0, 0.0,
            8.0, 0.0,
            4.0, -4.0)
    init {
        parentTrack.trackList.stageWidthProperty.addListener { _, old, new ->
            inputSelectRectangle.translateX -= (new as Double - old as Double)/2.0
            inputSelectArrow.translateX -= (new as Double - old as Double)/2.0
        }
        parentTrack.trackList.stageHeightProperty.addListener {_, old, new ->
            inputSelectRectangle.translateY -= (new as Double - old as Double)/2.0
            inputSelectArrow.translateY -= (new as Double - old as Double)/2.0
        }
        inputSelectRectangle.translateX = -(parentTrack.trackWidth/2.0) + 60.0
        inputSelectRectangle.translateY = parentTrack.trackOffsetY + 10.0
        inputSelectRectangle.arcWidth = 5.0
        inputSelectRectangle.arcHeight = 5.0
        inputSelectRectangle.stroke = Color.BLACK
        inputSelectRectangle.strokeWidth = 1.6

        inputSelectArrow.fill = Color.BLACK
        inputSelectArrow.translateX = -(parentTrack.trackWidth/2.0) + 59.5
        inputSelectArrow.translateY = parentTrack.trackOffsetY + 10.0
        inputSelectArrow.rotate = 180.0
        inputSelectArrow.stroke = Color.BLACK
        inputSelectArrow.strokeWidth = 1.0
        inputSelectArrow.strokeLineJoin = StrokeLineJoin.ROUND
    }

    val dropDownBox = DropDownBox(arrayListOf("hello", "world"), inputSelectRectangle, ::selectionChosen)

    init {
        inputSelectRectangle.onMouseReleased = EventHandler {
            dropDownBox.makeVisible()
            isDropDownBoxActive = true
        }
        inputSelectArrow.onMouseReleased = EventHandler {
            dropDownBox.makeVisible()
            isDropDownBoxActive = true
        }
    }
    override fun addChild(child: Widget) {
    }

    override fun addMeToScene(root: StackPane) {
        root.children.add(inputSelectRectangle)
        root.children.add(inputSelectArrow)
        dropDownBox.addMeToScene(root)
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
        }
    }

    fun makeInputSelectInvisible() {
        dropDownBox.makeInvisible()
        isDropDownBoxActive = false
    }

    fun selectionChosen(index: Int) {
        Logger.debug(TAG, "chose index $index", LEVEL)
    }
}