package org.jzeisel.app_test.components.textfield

import javafx.animation.KeyFrame
import javafx.animation.PauseTransition
import javafx.animation.Timeline
import javafx.application.Platform
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.scene.input.KeyEvent
import javafx.scene.layout.StackPane
import javafx.scene.paint.Color
import javafx.scene.paint.Paint
import javafx.scene.shape.Rectangle
import javafx.scene.text.Text
import javafx.util.Duration
import org.jzeisel.app_test.TrackListViewModel
import org.jzeisel.app_test.logger.Logger


class TextField(private val parentRect: Rectangle,
                private val parentText: Text,
                private val trackListViewModel: TrackListViewModel) {
    companion object {
        const val TAG = "TextField"
        const val LEVEL = 4
    }
    private var isShowing = false
    private val rectangleWidth: Double get() { return parentRect.width - 6.0 }
    private val rectangleHeight: Double get() { return parentRect.height - 2.0 }
    private val rectangleTranslateX: Double get() { return parentRect.translateX }
    private val rectangleTranslateY: Double get() { return parentRect.translateY }
    private val textString: String get() { return parentText.text }
    private val textFill: Paint get() { return parentText.fill }

    lateinit var timeline: Timeline
    lateinit var rectangle: Rectangle
    lateinit var text: Text
    lateinit var cursor: Rectangle

    fun addMeToScene(root: StackPane) {
        rectangle = Rectangle()
        text = Text()
        cursor = Rectangle()

        rectangle.width = rectangleWidth
        rectangle.height = rectangleHeight
        rectangle.translateX = rectangleTranslateX
        rectangle.translateY = rectangleTranslateY
        rectangle.arcWidth = trackListViewModel.arcSize
        rectangle.arcHeight = trackListViewModel.arcSize
        rectangle.fill = Color.WHITESMOKE

        text.text = textString
        text.fill = textFill
        text.translateX = rectangleTranslateX
        text.translateY = rectangleTranslateY

        cursor.width = 1.5
        cursor.height = rectangleHeight - 4.0
        cursor.translateX = rectangleTranslateX + text.boundsInLocal.width / 2.0 + 2.0
        cursor.translateY = rectangleTranslateY
        cursor.fill = trackListViewModel.backgroundGray.brighter()
        cursor.stroke = trackListViewModel.backgroundGray.brighter()
        cursor.isVisible = true

        rectangle.toFront()
        text.toFront()
        cursor.toFront()

        val delay = PauseTransition(Duration.millis(100.0));
        Platform.runLater {
            delay.setOnFinished {
                root.children.add(rectangle)
                root.children.add(text)
                root.children.add(cursor)
            }
            delay.play()
        }

        timeline = Timeline(
                KeyFrame(Duration.millis(500.0), { _: ActionEvent? ->
                    Platform.runLater{ cursor.isVisible = !cursor.isVisible }
                }),
        )
        timeline.cycleCount = Timeline.INDEFINITE
        timeline.play()

        isShowing = true
    }

    fun removeMeFromScene(root: StackPane) {
        Platform.runLater {
            root.children.remove(cursor)
            root.children.remove(text)
            root.children.remove(rectangle)
        }
        timeline.stop()
        isShowing = false
    }

    fun backspace() {
        if (isShowing) {
            Logger.debug(TAG, "backspace", 5)
        }
    }
}