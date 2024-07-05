package org.jzeisel.app_test.components.mixerComponents

import javafx.event.EventHandler
import javafx.scene.layout.StackPane
import javafx.scene.paint.Color
import javafx.scene.shape.Rectangle
import javafx.scene.text.Font
import javafx.scene.text.Text
import javafx.scene.text.TextAlignment
import org.jzeisel.app_test.components.interfaces.widget.Widget
import org.jzeisel.app_test.util.viewOrderFlip

class BpmDisplay(
    val newTempo: (Double) -> Unit,
    toolBarY: Double,
    toolBarHeight: Double,
    timeDisplayWidth: Double,
    initialBpm: Double): Widget {

    private val bpmRectangle = Rectangle()
    private val bpmText = Text()
    private var dragValue: Double? = null
    private var tempo = initialBpm
        set(value) {
            field = value
            newTempo(value)
            bpmText.text = value.toString()
        }

    init {
        bpmRectangle.translateX = timeDisplayWidth / 2.0 + 45.0
        bpmRectangle.translateY = toolBarY
        bpmRectangle.fill = Color.BLACK
        bpmRectangle.opacity = 0.7
        bpmRectangle.arcWidth = 8.0
        bpmRectangle.arcHeight = 8.0
        bpmRectangle.width = 50.0
        bpmRectangle.height = toolBarHeight - 0.8
        bpmRectangle.viewOrder = viewOrderFlip - 0.03
        bpmRectangle.onMouseEntered = EventHandler {
            bpmRectangle.opacity = 0.5
        }
        bpmRectangle.onMouseExited = EventHandler {
            bpmRectangle.opacity = 0.7
        }
        bpmRectangle.onMouseDragged = EventHandler {
            dragValue?.let {lastVal->
                val distance = (lastVal - it.screenY) / 4.0
                tempo += distance
            }
            dragValue = it.screenY
        }

        bpmText.text = "${initialBpm}"
        bpmText.font = Font("Courier New", 16.0)
        bpmText.translateY = bpmRectangle.translateY
        bpmText.fill = Color.WHITESMOKE
        bpmText.viewOrder = viewOrderFlip - 0.04
        bpmText.textAlignment = TextAlignment.RIGHT
        bpmText.translateX = bpmRectangle.translateX
        bpmText.isMouseTransparent = true
    }

    override fun addMeToScene(root: StackPane) {
        root.children.addAll(bpmRectangle, bpmText)
    }

    override fun removeMeFromScene(root: StackPane) {
        root.children.removeAll(bpmRectangle, bpmText)
    }

    fun updateTranslateY(new: Double) {
        bpmRectangle.translateY = new
        bpmText.translateY = bpmRectangle.translateY
    }
}