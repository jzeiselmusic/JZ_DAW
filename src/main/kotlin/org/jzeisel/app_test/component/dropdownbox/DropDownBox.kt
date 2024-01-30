package org.jzeisel.app_test.component.dropdownbox

import javafx.application.Platform
import javafx.event.EventHandler
import javafx.scene.layout.StackPane
import javafx.scene.paint.Color
import javafx.scene.shape.Rectangle
import org.jzeisel.app_test.component.trackBar.smallComponents.InputSelectArrow

class DropDownBox(stringList: List<String>, parent: Rectangle, clickCallback: (index: Int) -> Unit) {
    /* this custom dropdown box will essentially be a VBox */
    /* should be placed at a specific x and y coordinate
       which is equal to the center x and y of the dropdown arrow

       when the dropdown arrow is clicked, this menu becomes visible
       when user clicks outside the dropdown box the box becomes invisible

       the dropdown box must be initialized with a menu of string items

       the size of the rectangles are set by the size of the string items */
    val parentButton = parent
    val rectangleList = mutableListOf<Rectangle>()
    val buttonOffsetX = parentButton.translateX
    val buttonOffsetY = parentButton.translateY
    init {
        for (string in stringList) {
            val rect = Rectangle(100.0, 20.0, Color.MEDIUMPURPLE.brighter())
            rect.translateX = buttonOffsetX + rect.width / 2.0
            rect.translateY = (buttonOffsetY + rect.height / 2.0) + 20.0*stringList.indexOf(string)
            rect.stroke = Color.BLACK
            rect.strokeWidth = 1.0
            rect.arcWidth = 3.0
            rect.arcHeight = 3.0
            rect.onMouseEntered = EventHandler {
                rect.fill = Color.MEDIUMPURPLE.brighter()
            }
            rect.onMouseExited = EventHandler {
                rect.fill = Color.MEDIUMPURPLE
            }
            rect.onMouseClicked = EventHandler {
                clickCallback(stringList.indexOf(string))
            }
            rect.isVisible = false
            rectangleList.add(rect)
        }
    }

    fun addMeToScene(root: StackPane) {
        for (rectangle in rectangleList) {
            root.children.add(rectangle)
        }
    }

    fun removeMeFromScene(root: StackPane) {
        Platform.runLater {
            for (rectangle in rectangleList) {
                root.children.remove(rectangle)
            }
        }
    }

    fun makeVisible() {
        val thread = Thread {
            Thread.sleep(100)
            for (rectangle in rectangleList) {
                rectangle.isVisible = true
            }
        }
        thread.start()
    }

    fun makeInvisible() {
        Platform.runLater {
            for (rectangle in rectangleList) {
                rectangle.isVisible = false
            }
        }
    }
}