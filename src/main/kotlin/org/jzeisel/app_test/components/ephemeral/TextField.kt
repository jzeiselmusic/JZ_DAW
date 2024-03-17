package org.jzeisel.app_test.components.ephemeral

import javafx.animation.KeyFrame
import javafx.animation.PauseTransition
import javafx.animation.Timeline
import javafx.application.Platform
import javafx.event.ActionEvent
import javafx.scene.input.KeyEvent
import javafx.scene.layout.StackPane
import javafx.scene.paint.Color
import javafx.scene.paint.Paint
import javafx.scene.shape.Rectangle
import javafx.scene.text.Text
import javafx.util.Duration
import org.jzeisel.app_test.viewmodel.TrackListViewModel
import org.jzeisel.app_test.components.TrackComponentWidget
import org.jzeisel.app_test.util.BroadcastType
import org.jzeisel.app_test.util.ObservableListener
import org.jzeisel.app_test.util.viewOrderFlip


class TextField(private val parentRect: Rectangle,
                private val parentText: Text,
                private val trackListViewModel: TrackListViewModel,
                private val clickCallback: (name: String) -> Unit)
    : TrackComponentWidget, ObservableListener<Double> {
    var isShowing = false
    private val trackListState = trackListViewModel._trackListStateFlow.state
    private val cursorDistanceFromEndOfText = 3.0
    private val rectangleWidth: Double get() { return parentRect.width }
    private val rectangleHeight: Double get() { return parentRect.height }
    private val rectangleTranslateX: Double get() { return parentRect.translateX }
    private val rectangleTranslateY: Double get() { return parentRect.translateY }
    private val textString: String get() { return parentText.text }
    private val textFill: Paint get() { return parentText.fill }

    lateinit var timeline: Timeline
    lateinit var rectangle: Rectangle
    lateinit var text: Text
    lateinit var cursor: Rectangle

    fun addMeToScene(root: StackPane) {
        registerForBroadcasts()

        rectangle = Rectangle()
        text = Text()
        cursor = Rectangle()

        rectangle.width = rectangleWidth
        rectangle.height = rectangleHeight
        rectangle.translateX = rectangleTranslateX
        rectangle.translateY = rectangleTranslateY
        rectangle.arcWidth = trackListState.arcSize
        rectangle.arcHeight = trackListState.arcSize
        rectangle.fill = Color.WHITESMOKE.brighter()
        rectangle.stroke = trackListState.strokeColor
        rectangle.strokeWidth = 2.3
        rectangle.viewOrder = viewOrderFlip - 0.61
        text.text = textString
        text.fill = textFill
        text.translateX = rectangleTranslateX
        text.translateY = rectangleTranslateY
        text.viewOrder = viewOrderFlip - 0.62
        cursor.width = 1.2
        cursor.height = text.boundsInLocal.height - 1.0
        cursor.translateX = rectangleTranslateX + text.boundsInLocal.width / 2.0 + cursorDistanceFromEndOfText
        cursor.translateY = rectangleTranslateY
        cursor.fill = trackListState.backgroundGray.brighter()
        cursor.stroke = trackListState.backgroundGray.brighter()
        cursor.isVisible = true
        cursor.viewOrder = viewOrderFlip - 0.62
        val delay = PauseTransition(Duration.millis(100.0));
        Platform.runLater {
            delay.setOnFinished {
                root.children.add(rectangle)
                root.children.add(text)
                root.children.add(cursor)
                isShowing = true
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
    }

    fun removeMeFromScene(root: StackPane) {
        if (isShowing) {
            Platform.runLater {
                timeline.stop()
                root.children.removeAll(cursor, rectangle, text)
                isShowing = false
                clickCallback(text.text)
                unregisterForBroadcasts()
            }
        }
    }

    override fun respondToChange(broadcastType: BroadcastType, old: Double, new: Double) {
        when (broadcastType) {
            BroadcastType.STAGE_WIDTH -> respondToWidthChange(old, new)
            BroadcastType.STAGE_HEIGHT -> respondToHeightChange(old, new)
            BroadcastType.DIVIDER -> {}
            BroadcastType.INDEX -> {}
            BroadcastType.SCROLL -> {}
        }
    }

    override fun registerForBroadcasts() {
        trackListViewModel.registerForHeightChanges(this)
        trackListViewModel.registerForWidthChanges(this)
    }

    override fun unregisterForBroadcasts() {
        trackListViewModel.unregisterForHeightChanges(this)
        trackListViewModel.unregisterForWidthChanges(this)
    }

    fun backspace() {
        if (isShowing) {
            if (text.text.isNotBlank()) {
                text.text = text.text.dropLast(1)
                cursor.translateX = rectangleTranslateX + text.boundsInLocal.width / 2.0 + cursorDistanceFromEndOfText
            }
        }
    }

    fun character(character: KeyEvent) {
        if (isShowing) {
            if (character.isShiftDown) text.text += character.code.char.uppercase()
            else text.text += character.code.char.lowercase()
            cursor.translateX = rectangleTranslateX + text.boundsInLocal.width / 2.0 + cursorDistanceFromEndOfText
        }
    }

    override fun respondToHeightChange(old: Double, new: Double) {
        ((new - old)/2.0).let {
            cursor.translateY -= it
            rectangle.translateY -= it
            text.translateY -= it
        }
    }

    override fun respondToWidthChange(old: Double, new: Double) {
        ((new - old) / 2.0).let {
            cursor.translateX -= it
            rectangle.translateX -= it
            text.translateX -= it
        }
    }

    override fun respondToIndexChange(old: Double, new: Double) {
        /* not necessary for text field */
    }
}