package org.jzeisel.app_test.components.ephemeral.dropdownbox

import javafx.event.EventHandler
import javafx.scene.input.MouseEvent
import javafx.scene.layout.StackPane
import javafx.scene.shape.Polygon
import javafx.scene.shape.Rectangle
import javafx.scene.shape.Shape
import javafx.scene.shape.StrokeLineJoin
import kotlinx.coroutines.*
import org.jzeisel.app_test.util.BoxEntry
import org.jzeisel.app_test.util.runLater
import org.jzeisel.app_test.viewmodel.TrackListViewModel
import java.util.*
import java.util.concurrent.atomic.AtomicReference
import kotlin.math.max

class ExpandableDropDownBox(root: StackPane, val boxEntryList: List<BoxEntry>,
                            val clickCallback: (index: Int) -> Unit,
                            translateX: Double, translateY: Double,
                            private val trackListViewModel: TrackListViewModel,
                            isSubList: Boolean, val parentList: DropDownBox? = null)
        : DropDownBox(root, boxEntryList, clickCallback, translateX, translateY,
                        trackListViewModel, isSubList, parentList) {

    private val expansionArrows = mutableListOf<Polygon?>()
    private val mouseCheckedInFlags = Collections.synchronizedList(mutableListOf<Boolean>())
    private val mouseCheckedInWaitThreads = Collections.synchronizedList(mutableListOf<Job?>())
    private var expandableDropDownBoxChild: AtomicReference<ExpandableDropDownBox?> = AtomicReference(null)
    private var childIsHovering = false
    init {
        /* gets called every time input select arrow is pressed */
        boxEntryList.forEachIndexed { index, box ->
            expansionArrows.add(null)
            if (box.boxEntrySubList?.isNotEmpty() == true) {
                createExpansionArrow(index, rectangleList[index])
            }
            mouseCheckedInFlags.add(false)
            mouseCheckedInWaitThreads.add(null)
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
        expansionArrows[index] = arrow
    }

    override fun addMeToScene(root: StackPane) {
        super.addMeToScene(root)
        runLater(50.0) { expansionArrows.forEach { it?.let {arrow-> root.children.add(arrow) } } }
    }

    override fun removeMeFromScene(root: StackPane) {
        super.removeMeFromScene(root)
        runLater(0.0) { expansionArrows.forEach { it?.let {arrow-> root.children.remove(arrow) } } }
    }

    override fun onMouseHovers(obj: Shape): EventHandler<MouseEvent> {
        // normal DropDownBox simply highlights the hovered box
        val firstEvent = super.onMouseHovers(obj)
        // new event handles expansion logic
        val newEvent = EventHandler<MouseEvent> {
            (parentList as? ExpandableDropDownBox)?.childIsHovering = true
            val index = max(rectangleList.indexOf(it.source), textList.indexOf(it.source))
            if (expansionArrows[index] != null) {
                mouseCheckedInWaitThreads[index] = CoroutineScope(Dispatchers.Default).launch {
                    delay(400)
                    expandableDropDownBoxChild.set(ExpandableDropDownBox(
                        root,
                        boxEntryList[index].boxEntrySubList!!,
                        clickCallback,
                        rectangleList[index].translateX,
                        rectangleList[index].translateY,
                        trackListViewModel,
                        true,
                        this@ExpandableDropDownBox
                    ))
                    expandableDropDownBoxChild.get()?.addMeToScene(root)
                    mouseCheckedInWaitThreads[index] = null
                }
            }
        }
        return EventHandler { event ->
            firstEvent.handle(event)
            newEvent.handle(event)
        }
    }

    override fun onMouseExits(obj: Shape): EventHandler<MouseEvent> {
        val firstEvent = super.onMouseExits(obj)
        val newEvent = EventHandler<MouseEvent> {
            (parentList as? ExpandableDropDownBox)?.childIsHovering = false
            val index = max(rectangleList.indexOf(it.source), textList.indexOf(it.source))
            mouseCheckedInWaitThreads[index]?.cancel()
            runLater(100.0) {
                if (!childIsHovering) {
                    expandableDropDownBoxChild.get()?.removeMeFromScene(root)
                    expandableDropDownBoxChild.set(null)
                }
            }
        }
        return EventHandler { event ->
            firstEvent.handle(event)
            newEvent.handle(event)
        }
    }
}