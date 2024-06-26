package org.jzeisel.app_test.components.ephemeral.dropdownbox

import javafx.event.EventHandler
import javafx.scene.input.MouseEvent
import javafx.scene.layout.StackPane
import javafx.scene.shape.Rectangle
import javafx.scene.shape.Shape
import javafx.scene.text.Text
import javafx.scene.text.TextAlignment
import org.jzeisel.app_test.components.NormalTrack
import org.jzeisel.app_test.viewmodel.TrackListViewModel
import org.jzeisel.app_test.components.interfaces.TrackElement
import org.jzeisel.app_test.components.interfaces.WindowElement
import org.jzeisel.app_test.components.interfaces.widget.SingularWidget
import org.jzeisel.app_test.util.*

open class DropDownBox(val root: StackPane,
                       boxEntryList: List<BoxEntry>,
                       val clickCallback: (indexes: List<Int>) -> Unit,
                       translateX: Double, translateY: Double,
                       private val trackListViewModel: TrackListViewModel,
                       private val parentTrack: NormalTrack,
                       val isSublist: Boolean, parentList: DropDownBox? = null)
            : SingularWidget, TrackElement, WindowElement {
    val trackListState = trackListViewModel._trackListStateFlow.state
    val boxEntryListNames = boxEntryList.map { it.name }.toMutableList()
    val rectangleList = mutableListOf<Rectangle>()
    val textList = mutableListOf<Text>()
    private val buttonOffsetX = translateX
    private val buttonOffsetY = translateY
    var rectangleWidth = 100.0
    var rectangleHeight = 25.0
    var isHovering = false
    init {
        /* find the largest text and conform width */
        for ((idx,string) in boxEntryListNames.withIndex() ) {
            if (!isSublist && idx == trackListViewModel.getTrackInputDeviceIndex(parentTrack.trackId)) {
                boxEntryListNames[idx] = "\u29BF   $string"
            }
            val testText = Text(string)
            if (testText.boundsInLocal.width > (rectangleWidth - 8)) rectangleWidth = testText.boundsInLocal.width + 50
            if (testText.boundsInLocal.height > (rectangleHeight - 8)) rectangleHeight = testText.boundsInLocal.height + 8
        }
        for ( (idx,string) in boxEntryListNames.withIndex() ) {
            val text = Text(string)
            if (!isSublist) {
                text.translateX = buttonOffsetX + rectangleWidth / 2.0
                text.translateY = (buttonOffsetY + rectangleHeight / 2.0) + rectangleHeight * idx
            }
            else {
                text.translateX = buttonOffsetX + parentList!!.rectangleWidth/2.0 + rectangleWidth / 2.0
                text.translateY = buttonOffsetY + rectangleHeight * idx
            }
            text.fill = trackListState.strokeColor
            text.textAlignment = TextAlignment.CENTER
            text.isVisible = true
            text.viewOrder = viewOrderFlip - 0.65
            text.isMouseTransparent = true
            /*********************/
            val rect = Rectangle(rectangleWidth, rectangleHeight, trackListState.generalPurple)
            if (!isSublist) {
                rect.translateX = buttonOffsetX + rectangleWidth / 2.0
                rect.translateY = (buttonOffsetY + rectangleHeight / 2.0) + rectangleHeight * idx
            }
            else {
                rect.translateX = buttonOffsetX + parentList!!.rectangleWidth/2.0 + rectangleWidth/2.0
                rect.translateY = buttonOffsetY + rectangleHeight * idx
            }
            rect.stroke = trackListState.strokeColor
            rect.strokeWidth = trackListState.strokeSize
            rect.arcWidth = 3.0
            rect.arcHeight = 3.0
            rect.isVisible = true
            rect.viewOrder = viewOrderFlip - 0.64
            /*********************/
            rect.onMouseEntered = onMouseHovers(rect)
            rect.onMouseExited = onMouseExits(rect)
            /*********************/
            rect.onMouseClicked = onMouseClicked(idx)
            rectangleList.add(rect)
            textList.add(text)
        }
    }

    override fun addMeToScene(root: StackPane) {
        runLater(50.0) {
            registerForBroadcasts()
            root.children.addAll(rectangleList)
            root.children.addAll(textList)
        }
    }

    override fun removeMeFromScene(root: StackPane) {
        runLater(0.0) {
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
            isHovering = true
            obj.fill = trackListState.generalPurple.darker()
        }
    }

    open fun onMouseExits(obj: Shape): EventHandler<MouseEvent> {
        return EventHandler {
            isHovering = false
            obj.fill = trackListState.generalPurple
        }
    }

    open fun onMouseClicked(idx: Int): EventHandler<MouseEvent> {
        return EventHandler{
            clickCallback(listOf(idx))
            removeMeFromScene(root)
        }
    }
}