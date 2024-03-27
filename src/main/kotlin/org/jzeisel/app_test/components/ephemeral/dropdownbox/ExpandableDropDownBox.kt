package org.jzeisel.app_test.components.ephemeral.dropdownbox

import javafx.event.EventHandler
import javafx.scene.input.MouseEvent
import javafx.scene.layout.StackPane
import javafx.scene.shape.Polygon
import javafx.scene.shape.Rectangle
import javafx.scene.shape.Shape
import javafx.scene.shape.StrokeLineJoin
import org.jzeisel.app_test.util.BoxEntry
import org.jzeisel.app_test.util.Logger
import org.jzeisel.app_test.util.runLater
import org.jzeisel.app_test.viewmodel.TrackListViewModel
import kotlin.math.max

class ExpandableDropDownBox(root: StackPane, boxEntryList: List<BoxEntry>,
                            clickCallback: (index: Int) -> Unit,
                            translateX: Double, translateY: Double,
                            private val trackListViewModel: TrackListViewModel,
                            xOffset: Double = 0.0, yOffset: Double = 0.0)
        : DropDownBox(root, boxEntryList, clickCallback, translateX, translateY,
                        trackListViewModel, xOffset, yOffset) {

    private val expansionArrows = mutableListOf<Polygon>()
    init {
        /* gets called every time input select arrow is pressed */
        boxEntryList.forEachIndexed { index, box ->
            if (box.boxEntrySubList?.isNotEmpty() == true) {
                createExpansionArrow(index, rectangleList[index])
            }
        }
    }

    private fun createExpansionArrow(index: Int, rect: Rectangle) {
        val arrow = Polygon(0.0, 0.0,
                                    8.0, 0.0,
                                    4.0, -3.0)
        arrow.viewOrder = rect.viewOrder - 1.0
        arrow.translateY = rect.translateY
        arrow.translateX = rect.translateX + rect.width/2.0 - 10.0
        arrow.rotate = 90.0
        arrow.stroke = trackListState.strokeColor
        arrow.strokeWidth = 1.5
        arrow.strokeLineJoin = StrokeLineJoin.ROUND
        arrow.opacity = 0.55
        expansionArrows.add(arrow)
    }

    override fun addMeToScene(root: StackPane) {
        super.addMeToScene(root)
        runLater(50.0) { expansionArrows.forEach { root.children.add(it) } }
    }

    override fun removeMeFromScene(root: StackPane) {
        super.removeMeFromScene(root)
        runLater(0.0) { expansionArrows.forEach { root.children.remove(it) } }
    }

    override fun onMouseHovers(obj: Shape): EventHandler<MouseEvent> {
        val firstEvent = super.onMouseHovers(obj)
        val newEvent = EventHandler<MouseEvent> {
            val index = max(rectangleList.indexOf(it.source), textList.indexOf(it.source))
            Logger.debug(javaClass.simpleName, "$index entered", 5)
        }
        return EventHandler { event ->
            firstEvent.handle(event)
            newEvent.handle(event)
        }
    }

    override fun onMouseExits(obj: Shape): EventHandler<MouseEvent> {
        val firstEvent = super.onMouseExits(obj)
        val newEvent = EventHandler<MouseEvent> {
            val index = max(rectangleList.indexOf(it.source), textList.indexOf(it.source))
            Logger.debug(javaClass.simpleName, "$index exited", 3)
        }
        return EventHandler { event ->
            firstEvent.handle(event)
            newEvent.handle(event)
        }
    }
}