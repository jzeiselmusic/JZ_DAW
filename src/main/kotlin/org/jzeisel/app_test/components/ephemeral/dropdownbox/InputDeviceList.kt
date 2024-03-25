package org.jzeisel.app_test.components.ephemeral.dropdownbox

import javafx.event.EventHandler
import javafx.scene.input.MouseEvent
import javafx.scene.layout.StackPane
import javafx.scene.shape.Polygon
import javafx.scene.shape.Rectangle
import javafx.scene.shape.Shape
import javafx.scene.shape.StrokeLineJoin
import org.jzeisel.app_test.util.Logger
import org.jzeisel.app_test.viewmodel.TrackListViewModel
import kotlin.math.exp

class InputDeviceList(stringList: List<String>, parent: Rectangle,
                      private val trackListViewModel: TrackListViewModel,
                      clickCallback: (index: Int) -> Unit)
            : DropDownBox(stringList, parent, trackListViewModel, clickCallback){
    private val state = trackListViewModel._trackListStateFlow.state
    private val expansionArrows = mutableListOf<Polygon>()

    init {
        rectangleList.forEachIndexed { index, rect ->
            trackListViewModel.audioViewModel.getChannelsFromDevice(index)?.let {
                if (it.isNotEmpty()) {
                    createExpansionArrow(index, rect)
                }
            }
        }
    }

    private fun createExpansionArrow(index: Int, rect: Rectangle) {
        val arrow = Polygon(0.0, 0.0,
                                        8.0, 0.0,
                                        4.0, -4.0)
        arrow.viewOrder = rect.viewOrder - 1.0
        arrow.translateY = rect.translateY
        arrow.translateX = rect.translateX + rect.width/2.0 - 10.0
        arrow.rotate = 90.0
        arrow.stroke = state.strokeColor
        arrow.strokeWidth = 1.5
        arrow.strokeLineJoin = StrokeLineJoin.ROUND
        arrow.opacity = 0.55
        expansionArrows.add(arrow)
    }

    override fun onMouseHovers(obj: Shape): EventHandler<MouseEvent> {
        val superEventHandler = super.onMouseHovers(obj)
        val extraEventHandler = EventHandler<MouseEvent> { /* create new list of channels */ }
        return EventHandler { event ->
            superEventHandler.handle(event)
            extraEventHandler.handle(event)
        }
    }

    override fun onMouseExits(obj: Shape): EventHandler<MouseEvent> {
        val superEventHandler = super.onMouseExits(obj)
        val extraEventHandler = EventHandler<MouseEvent> { /* remove list of channels */ }
        return EventHandler { event ->
            superEventHandler.handle(event)
            extraEventHandler.handle(event)
        }
    }

    override fun addMeToScene(root: StackPane) {
        super.addMeToScene(root)
        expansionArrows.forEach { root.children.add(it) }
    }

    override fun removeMeFromScene(root: StackPane) {
        super.removeMeFromScene(root)
        expansionArrows.forEach { root.children.remove(it) }
    }
}