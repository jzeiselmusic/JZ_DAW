package org.jzeisel.app_test.component.dropdownbox

import javafx.animation.PauseTransition
import javafx.application.Platform
import javafx.event.EventHandler
import javafx.scene.layout.StackPane
import javafx.scene.paint.Color
import javafx.scene.shape.Rectangle
import javafx.scene.text.Text
import javafx.scene.text.TextAlignment
import javafx.util.Duration

class DropDownBox(stringList: List<String>, parent: Rectangle, clickCallback: (index: Int) -> Unit) {
    companion object {
        const val TAG = "DropDownBox"
        const val LEVEL = 4
    }
    private val parentButton = parent
    private val rectangleList = mutableListOf<Rectangle>()
    private val textList = mutableListOf<Text>()
    private val buttonOffsetX = parentButton.translateX
    private val buttonOffsetY = parentButton.translateY
    private var rectangleWidth = 100.0
    private var rectangleHeight = 25.0
    init {
        /* find the largest text and conform width */
        for ( string in stringList ) {
            val testText = Text(string)
            if (testText.boundsInLocal.width > (rectangleWidth - 8)) rectangleWidth = testText.boundsInLocal.width + 8
            if (testText.boundsInLocal.height > (rectangleHeight - 8)) rectangleHeight = testText.boundsInLocal.height + 8
        }
        for ( (idx,string) in stringList.withIndex() ) {
            val text = Text(buttonOffsetX + rectangleWidth / 2.0,
                    (buttonOffsetY + rectangleHeight / 2.0) + 20.0*idx,
                    string)
            text.translateX = buttonOffsetX + rectangleWidth / 2.0
            text.translateY = (buttonOffsetY + rectangleHeight / 2.0) + 20.0*idx
            text.fill = Color.BLACK
            text.textAlignment = TextAlignment.CENTER
            text.isVisible = true
            /*********************/
            val rect = Rectangle(rectangleWidth, rectangleHeight, Color.MEDIUMPURPLE.brighter())
            rect.translateX = buttonOffsetX + rectangleWidth / 2.0
            rect.translateY = (buttonOffsetY + rectangleHeight / 2.0) + 20.0*idx
            rect.stroke = Color.BLACK
            rect.strokeWidth = 1.0
            rect.arcWidth = 3.0
            rect.arcHeight = 3.0
            rect.isVisible = true
            /*********************/
            text.onMouseEntered = EventHandler {
                rect.fill = Color.MEDIUMPURPLE
            }
            text.onMouseExited = EventHandler {
                rect.fill = Color.MEDIUMPURPLE.brighter()
            }
            text.onMouseClicked = EventHandler {
                clickCallback(idx)
            }
            /*********************/
            rect.onMouseEntered = EventHandler {
                rect.fill = Color.MEDIUMPURPLE
            }
            rect.onMouseExited = EventHandler {
                rect.fill = Color.MEDIUMPURPLE.brighter()
            }
            rect.onMouseClicked = EventHandler {
                clickCallback(idx)
            }
            rectangleList.add(rect)
            textList.add(text)
        }
    }

    fun addMeToScene(root: StackPane) {
        val delay = PauseTransition(Duration.millis(100.0));
        Platform.runLater {
            delay.setOnFinished {
                root.children.addAll(rectangleList)
                root.children.addAll(textList)
            }
            delay.play()
        }
    }

    fun removeMeFromScene(root: StackPane) {
        Platform.runLater {
            root.children.removeAll(textList)
            root.children.removeAll(rectangleList)
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