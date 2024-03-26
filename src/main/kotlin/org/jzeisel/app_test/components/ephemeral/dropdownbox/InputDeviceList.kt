package org.jzeisel.app_test.components.ephemeral.dropdownbox

import javafx.animation.PauseTransition
import javafx.application.Platform
import javafx.event.EventHandler
import javafx.scene.input.MouseEvent
import javafx.scene.layout.StackPane
import javafx.scene.shape.Polygon
import javafx.scene.shape.Rectangle
import javafx.scene.shape.Shape
import javafx.scene.shape.StrokeLineJoin
import javafx.util.Duration
import org.jzeisel.app_test.viewmodel.TrackListViewModel
import kotlin.math.max

class InputDeviceList(stringList: List<String>, parent: Rectangle,
                      private val trackListViewModel: TrackListViewModel,
                      private val clickCallback: (index: Int) -> Unit)
            : DropDownBox(stringList, parent, trackListViewModel, clickCallback){
    private val state = trackListViewModel._trackListStateFlow.state
    lateinit var root: StackPane
    private val expansionArrows = mutableListOf<Polygon>()
    private val listOfChannelLists = mutableListOf<List<String>>()
    private var activeChannelList: DropDownBox? = null

    init {
        /* gets called every time input select arrow is pressed */
        rectangleList.forEachIndexed { index, rect ->
            trackListViewModel.audioViewModel.getChannelsFromDevice(index)?.let {
                if (it.isNotEmpty()) {
                    createExpansionArrow(index, rect)
                }
                listOfChannelLists.add(it.map { channel-> channel.name })
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
            val dropDownBox = DropDownBox(listOfChannelLists[index],
                                          it.source as Shape,
                                          trackListViewModel,
                                          clickCallback,
                                          rectangleWidth/2.0, -rectangleHeight/2.0)
            dropDownBox.addMeToScene(root)
            activeChannelList = dropDownBox
        }
        return EventHandler { event ->
            superEventHandler.handle(event)
            extraEventHandler.handle(event)
        }
    }

    override fun onMouseExits(obj: Shape): EventHandler<MouseEvent> {
        val superEventHandler = super.onMouseExits(obj)
        val extraEventHandler = EventHandler<MouseEvent> {
            val delay = PauseTransition(Duration.millis(100.0));
            Platform.runLater {
                delay.setOnFinished {
                    activeChannelList?.removeMeFromScene(root)
                    activeChannelList = null
                }
                delay.play()
            }
        }
        return EventHandler { event ->
            superEventHandler.handle(event)
            extraEventHandler.handle(event)
        }
    }

    override fun addMeToScene(root: StackPane) {
        super.addMeToScene(root)
        this.root = root
        expansionArrows.forEach { root.children.add(it) }
    }

    override fun removeMeFromScene(root: StackPane) {
        super.removeMeFromScene(root)
        expansionArrows.forEach { root.children.remove(it) }
    }
}