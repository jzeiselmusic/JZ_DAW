package org.jzeisel.app_test.components.ephemeral.dropdownbox

import javafx.event.EventHandler
import javafx.scene.input.MouseEvent
import javafx.scene.layout.StackPane
import javafx.scene.shape.Polygon
import javafx.scene.shape.Rectangle
import javafx.scene.shape.Shape
import javafx.scene.shape.StrokeLineJoin
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jzeisel.app_test.viewmodel.TrackListViewModel
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference
import kotlin.math.max

class InputDeviceList(override val root: StackPane, stringList: List<String>, parent: Rectangle,
                      private val trackListViewModel: TrackListViewModel,
                      private val clickCallback: (index: Int) -> Unit)
            : DropDownBox(root, stringList, parent, trackListViewModel, clickCallback){
    private val state = trackListViewModel._trackListStateFlow.state
    private val expansionArrows = mutableListOf<Polygon>()
    private val listOfChannelLists = mutableListOf<List<String>>()
    private val listOfAtomicVars = mutableListOf<AtomicInteger>()
    private val listOfActiveDropDownBoxes = mutableListOf<AtomicReference<DropDownBox>>()

    init {
        /* gets called every time input select arrow is pressed */
        rectangleList.forEachIndexed { index, rect ->
            trackListViewModel.audioViewModel.getChannelsFromDevice(index)?.let {
                if (it.isNotEmpty()) {
                    createExpansionArrow(index, rect)
                }
                listOfChannelLists.add(it.map { channel-> channel.name })
                listOfAtomicVars.add(AtomicInteger())
                listOfAtomicVars.forEach { atomic -> atomic.set(0) }
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
        val extraEventHandler = EventHandler<MouseEvent> {
            val index = max(rectangleList.indexOf(it.source), textList.indexOf(it.source))
            listOfAtomicVars.forEach { v -> v.set(0) }
            listOfAtomicVars[index].set(1)
            val dropDownBox = DropDownBox(root, listOfChannelLists[index],
                                          it.source as Shape,
                                          trackListViewModel,
                                          clickCallback,
                                          rectangleWidth/2.0, -rectangleHeight/2.0,
                                            this)
            CoroutineScope(Dispatchers.Default).launch {
                delay(300L)
                if (listOfAtomicVars[index].get() == 1) {
                    dropDownBox.addMeToScene(root)
                    listOfActiveDropDownBoxes.add(AtomicReference(dropDownBox))
                }
            }
        }
        return EventHandler { event ->
            superEventHandler.handle(event)
            extraEventHandler.handle(event)
        }
    }

    override fun onMouseExits(obj: Shape): EventHandler<MouseEvent> {
        val superEventHandler = super.onMouseExits(obj)
        val extraEventHandler = EventHandler<MouseEvent> {
            listOfAtomicVars.forEach { v -> v.set(0) }
            CoroutineScope(Dispatchers.Default).launch {
                delay(250)
                listOfActiveDropDownBoxes.forEach { ddb ->
                    if (ddb?.get()?.setOfHoveredDropDownBoxes?.isEmpty() == true) {
                        ddb?.get()?.removeMeFromScene(root)
                        CoroutineScope(Dispatchers.Default).launch {
                            delay(500L)
                            listOfActiveDropDownBoxes.remove(ddb)
                        }
                    }
                }
            }
        }
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