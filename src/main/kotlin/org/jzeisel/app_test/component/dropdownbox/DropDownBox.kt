package org.jzeisel.app_test.component.dropdownbox

import javafx.application.Platform
import javafx.event.EventHandler
import javafx.scene.layout.StackPane
import javafx.scene.paint.Color
import javafx.scene.shape.Rectangle
import javafx.scene.text.Text
import javafx.scene.text.TextAlignment

class DropDownBox(stringList: List<String>, parent: Rectangle, clickCallback: (index: Int) -> Unit) {
    /* this custom dropdown box will essentially be a VBox */
    /* should be placed at a specific x and y coordinate
       which is equal to the center x and y of the dropdown arrow

       when the dropdown arrow is clicked, this menu becomes visible
       when user clicks outside the dropdown box the box becomes invisible

       the dropdown box must be initialized with a menu of string items

       the size of the rectangles are set by the size of the string items */
    private val parentButton = parent
    private val rectangleList = mutableListOf<Rectangle>()
    private val textList = mutableListOf<Text>()
    private val buttonOffsetX = parentButton.translateX
    private val buttonOffsetY = parentButton.translateY
    private var rectangleWidth = 100.0
    private var rectangleHeight = 20.0
    init {
        for ( (idx,string) in stringList.withIndex() ) {
            val rect = Rectangle(100.0, 20.0, Color.MEDIUMPURPLE.brighter())
            rect.translateX = buttonOffsetX + rectangleWidth / 2.0
            rect.translateY = (buttonOffsetY + rectangleHeight / 2.0) + 20.0*idx
            rect.stroke = Color.BLACK
            rect.strokeWidth = 1.0
            rect.arcWidth = 3.0
            rect.arcHeight = 3.0
            rect.onMouseEntered = EventHandler {
                rect.fill = Color.MEDIUMPURPLE
            }
            rect.onMouseExited = EventHandler {
                rect.fill = Color.MEDIUMPURPLE.brighter()
            }
            rect.onMouseClicked = EventHandler {
                clickCallback(idx)
            }
            rect.isVisible = false
            /**************/
            val text = Text(buttonOffsetX + rectangleWidth / 2.0,
                    (buttonOffsetY + rectangleHeight / 2.0) + 20.0*idx,
                    string)
            text.translateX = buttonOffsetX + rectangleWidth / 2.0
            text.translateY = (buttonOffsetY + rectangleHeight / 2.0) + 20.0*idx
            text.fill = Color.BLACK
            text.textAlignment = TextAlignment.CENTER
            text.isVisible = false
            text.onMouseEntered = EventHandler {
                rect.fill = Color.MEDIUMPURPLE
            }
            text.onMouseExited = EventHandler {
                rect.fill = Color.MEDIUMPURPLE.brighter()
            }
            text.onMouseClicked = EventHandler {
                clickCallback(idx)
            }
            rectangleList.add(rect)
            textList.add(text)
        }
    }

    fun addMeToScene(root: StackPane) {
        for ((rectangle, text) in rectangleList.zip(textList)) {
            root.children.add(rectangle)
            root.children.add(text)
        }
    }

    fun removeMeFromScene(root: StackPane) {
        Platform.runLater {
            for ((rectangle, text) in rectangleList.zip(textList)) {
                root.children.remove(rectangle)
                root.children.remove(text)
            }
        }
    }

    fun makeVisible() {
        val thread = Thread {
            Thread.sleep(100)
            for ((rectangle, text) in rectangleList.zip(textList)) {
                rectangle.isVisible = true
                text.isVisible = true
            }
        }
        thread.start()
    }

    fun makeInvisible() {
        Platform.runLater {
            for ((rectangle, text) in rectangleList.zip(textList)) {
                rectangle.isVisible = false
                text.isVisible = false
            }
        }
    }

    fun updateTranslation(newX: Double, newY: Double) {
        val tList = rectangleList.zip(textList)
        for ((idx, pair) in tList.withIndex()) {
            pair.first.translateX = newX + rectangleWidth / 2.0
            pair.first.translateY = (newY + rectangleHeight / 2.0) + 20.0*idx
            pair.second.translateX = newX + rectangleWidth / 2.0
            pair.second.translateY = (newY + rectangleHeight / 2.0) + 20.0*idx
        }
    }
}