package org.jzeisel.app_test.component.vuMeter

import javafx.application.Platform
import javafx.scene.layout.StackPane
import javafx.scene.paint.Color
import javafx.scene.shape.Rectangle
import org.jzeisel.app_test.component.Widget

class VUBar(color: Color, val offsetY: Double,
            override val parent: Widget): Widget {

    private val width = (parent as VUMeter).width - 4.0
    private val height = (parent as VUMeter).barHeight
    private val offsetX = (parent as VUMeter).offsetX
    private val rectangle = Rectangle(width, height, color)

    override val children = mutableListOf<Widget>()

    override fun addChild(child: Widget) {
        /* VUBar will not have any children */
    }

    fun isVisible(t: Boolean) {
        rectangle.isVisible = t
    }

    override fun addMeToScene(root: StackPane) {
        rectangle.translateX = offsetX
        rectangle.translateY = offsetY
        rectangle.arcWidth = 5.0
        rectangle.arcHeight = 5.0
        root.children.add(rectangle)
    }

    override fun removeMeFromScene(root: StackPane) {
        Platform.runLater {
            root.children.remove(rectangle)
        }
    }
}