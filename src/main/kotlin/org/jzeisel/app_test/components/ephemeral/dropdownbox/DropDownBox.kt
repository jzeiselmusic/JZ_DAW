package org.jzeisel.app_test.components.ephemeral.dropdownbox

import javafx.animation.PauseTransition
import javafx.application.Platform
import javafx.event.EventHandler
import javafx.scene.input.MouseEvent
import javafx.scene.layout.StackPane
import javafx.scene.shape.Rectangle
import javafx.scene.shape.Shape
import javafx.scene.text.Text
import javafx.scene.text.TextAlignment
import javafx.util.Duration
import org.jzeisel.app_test.viewmodel.TrackListViewModel
import org.jzeisel.app_test.components.TrackComponentWidget
import org.jzeisel.app_test.util.BroadcastType
import org.jzeisel.app_test.util.ObservableListener
import org.jzeisel.app_test.util.viewOrderFlip
import java.util.concurrent.atomic.AtomicReference

open class DropDownBox(
    open val root: StackPane, stringList: List<String>, parent: Shape,
    private val trackListViewModel: TrackListViewModel,
    clickCallback: (index: Int) -> Unit,
    xOffset: Double = 0.0, yOffset: Double = 0.0,
    val parentDropDownBox: InputDeviceList? = null)
    : TrackComponentWidget, ObservableListener<Double> {
    private val trackListState = trackListViewModel._trackListStateFlow.state
    private val parentButton = parent
    val rectangleList = mutableListOf<Rectangle>()
    val textList = mutableListOf<Text>()
    val setOfHoveredDropDownBoxes = mutableSetOf<AtomicReference<DropDownBox>>()
    private val buttonOffsetX = parentButton.translateX
    private val buttonOffsetY = parentButton.translateY
    var rectangleWidth = 100.0
    var rectangleHeight = 25.0
    init {
        /* find the largest text and conform width */
        for ( string in stringList ) {
            val testText = Text(string)
            if (testText.boundsInLocal.width > (rectangleWidth - 8)) rectangleWidth = testText.boundsInLocal.width + 40
            if (testText.boundsInLocal.height > (rectangleHeight - 8)) rectangleHeight = testText.boundsInLocal.height + 8
        }
        for ( (idx,string) in stringList.withIndex() ) {
            val text = Text(string)
            text.translateX = buttonOffsetX + rectangleWidth / 2.0 + xOffset
            text.translateY = (buttonOffsetY + rectangleHeight / 2.0) + rectangleHeight*idx + yOffset
            text.fill = trackListState.strokeColor
            text.textAlignment = TextAlignment.CENTER
            text.isVisible = true
            text.viewOrder = viewOrderFlip - 0.65
            /*********************/
            val rect = Rectangle(rectangleWidth, rectangleHeight, trackListState.generalPurple)
            rect.translateX = buttonOffsetX + rectangleWidth / 2.0 + xOffset
            rect.translateY = (buttonOffsetY + rectangleHeight / 2.0) + rectangleHeight*idx + yOffset
            rect.stroke = trackListState.strokeColor
            rect.strokeWidth = trackListState.strokeSize
            rect.arcWidth = 3.0
            rect.arcHeight = 3.0
            rect.isVisible = true
            rect.viewOrder = viewOrderFlip - 0.64
            /*********************/
            text.onMouseEntered = onMouseHovers(rect)
            text.onMouseExited = onMouseExits(rect)
            /*********************/
            rect.onMouseEntered = onMouseHovers(rect)
            rect.onMouseExited = onMouseExits(rect)
            /*********************/
            rect.onMouseClicked = EventHandler{ removeMeFromScene(root) }
            text.onMouseClicked = EventHandler { removeMeFromScene(root) }
            rectangleList.add(rect)
            textList.add(text)
        }
    }

    open fun addMeToScene(root: StackPane) {
        val delay = PauseTransition(Duration.millis(50.0));
        Platform.runLater {
            delay.setOnFinished {
                registerForBroadcasts()
                root.children.addAll(rectangleList)
                root.children.addAll(textList)
            }
            delay.play()
        }
    }

    open fun removeMeFromScene(root: StackPane) {
        Platform.runLater {
            unregisterForBroadcasts()
            root.children.removeAll(textList)
            root.children.removeAll(rectangleList)
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

    override fun respondToIndexChange(old: Double, new: Double) {
        /* not necessary for drop down box */
    }

    override fun respondToChange(broadcastType: BroadcastType, old: Double, new: Double) {
        when (broadcastType) {
            BroadcastType.STAGE_WIDTH -> respondToWidthChange(old, new)
            BroadcastType.STAGE_HEIGHT -> respondToHeightChange(old, new)
            BroadcastType.INDEX -> {}
            BroadcastType.DIVIDER -> {}
            BroadcastType.SCROLL -> {}
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

    open fun onMouseHovers(obj: Shape): EventHandler<MouseEvent> {
        return EventHandler {
            obj.fill = trackListState.generalPurple.darker()
            setOfHoveredDropDownBoxes.add(AtomicReference(this))
        }
    }

    open fun onMouseExits(obj: Shape): EventHandler<MouseEvent> {
        return EventHandler {
            obj.fill = trackListState.generalPurple
            setOfHoveredDropDownBoxes.clear()
        }
    }
}