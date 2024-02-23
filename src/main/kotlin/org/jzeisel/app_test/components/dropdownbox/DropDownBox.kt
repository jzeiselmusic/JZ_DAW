package org.jzeisel.app_test.components.dropdownbox

import javafx.animation.PauseTransition
import javafx.application.Platform
import javafx.event.EventHandler
import javafx.scene.layout.StackPane
import javafx.scene.shape.Rectangle
import javafx.scene.text.Text
import javafx.scene.text.TextAlignment
import javafx.util.Duration
import org.jzeisel.app_test.TrackListViewModel
import org.jzeisel.app_test.components.TrackComponentWidget
import org.jzeisel.app_test.util.Observable
import org.jzeisel.app_test.util.ObservableListener

class DropDownBox(stringList: List<String>, parent: Rectangle,
                  private val trackListViewModel: TrackListViewModel,
                  clickCallback: (index: Int) -> Unit)
    : TrackComponentWidget, ObservableListener<Double> {
    companion object {
        const val TAG = "DropDownBox"
    }
    private val parentButton = parent
    private val rectangleList = mutableListOf<Rectangle>()
    private val textList = mutableListOf<Text>()
    private val buttonOffsetX = parentButton.translateX
    private val buttonOffsetY = parentButton.translateY
    private var rectangleWidth = 100.0
    private var rectangleHeight = 25.0
    private var isAdded = false
    init {
        /* find the largest text and conform width */
        for ( string in stringList ) {
            val testText = Text(string)
            if (testText.boundsInLocal.width > (rectangleWidth - 8)) rectangleWidth = testText.boundsInLocal.width + 8
            if (testText.boundsInLocal.height > (rectangleHeight - 8)) rectangleHeight = testText.boundsInLocal.height + 8
        }
        for ( (idx,string) in stringList.withIndex() ) {
            val text = Text(string)
            text.translateX = buttonOffsetX + rectangleWidth / 2.0
            text.translateY = (buttonOffsetY + rectangleHeight / 2.0) + rectangleHeight*idx
            text.fill = trackListViewModel.strokeColor
            text.textAlignment = TextAlignment.CENTER
            text.isVisible = true
            /*********************/
            val rect = Rectangle(rectangleWidth, rectangleHeight, trackListViewModel.generalPurple)
            rect.translateX = buttonOffsetX + rectangleWidth / 2.0
            rect.translateY = (buttonOffsetY + rectangleHeight / 2.0) + rectangleHeight*idx
            rect.stroke = trackListViewModel.strokeColor
            rect.strokeWidth = trackListViewModel.strokeSize
            rect.arcWidth = 3.0
            rect.arcHeight = 3.0
            rect.isVisible = true
            /*********************/
            text.onMouseEntered = EventHandler {
                rect.fill = trackListViewModel.generalPurple.darker()
            }
            text.onMouseExited = EventHandler {
                rect.fill = trackListViewModel.generalPurple
            }
            text.onMouseClicked = EventHandler {
                clickCallback(idx)
            }
            /*********************/
            rect.onMouseEntered = EventHandler {
                rect.fill = trackListViewModel.generalPurple.darker()
            }
            rect.onMouseExited = EventHandler {
                rect.fill = trackListViewModel.generalPurple
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
                registerForBroadcasts()
                root.children.addAll(rectangleList)
                root.children.addAll(textList)
                isAdded = true
            }
            delay.play()
        }
    }

    fun removeMeFromScene(root: StackPane) {
        if (isAdded) {
            Platform.runLater {
                unregisterForBroadcasts()
                root.children.removeAll(textList)
                root.children.removeAll(rectangleList)
                isAdded = false
            }
        }
    }

    override fun respondToHeightChange(old: Double, new: Double) {
        val tList = rectangleList.zip(textList)
        for (pair in tList) {
            ((new - old)/2.0).let {
                pair.first.translateY -= it
                pair.second.translateY -= it
            }
        }
    }

    override fun respondToWidthChange(old: Double, new: Double) {
        val tList = rectangleList.zip(textList)
        for (pair in tList) {
            ((new - old)/2.0).let {
                pair.first.translateX -= it
                pair.second.translateX -= it
            }
        }
    }

    override fun respondToChange(observable: Observable<*>, old: Double, new: Double) {
        when (observable) {
            trackListViewModel.testStageWidth -> respondToWidthChange(old, new)
            trackListViewModel.testStageHeight -> respondToHeightChange(old, new)
        }
    }

    override fun registerForBroadcasts() {
        trackListViewModel.registerForWidthChanges(this)
        trackListViewModel.registerForHeightChanges(this)
    }

    override fun unregisterForBroadcasts() {
        trackListViewModel.unregisterForWidthChanges(this)
        trackListViewModel.unregisterForHeightChanges(this)
    }
}