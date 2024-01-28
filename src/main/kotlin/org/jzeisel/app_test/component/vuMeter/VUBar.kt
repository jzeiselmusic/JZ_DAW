package org.jzeisel.app_test.component.vuMeter

import javafx.application.Platform
import javafx.scene.layout.StackPane
import javafx.scene.paint.Color
import javafx.scene.shape.Rectangle
import org.jzeisel.app_test.component.Widget

class VUBar(color: Color, private var barOffsetY: Double,
            override val parent: Widget): Widget {

    private val barWidth = (parent as VUMeter).vuMeterWidth - 4.0
    private val barHeight = (parent as VUMeter).barHeight
    private var barOffsetX = (parent as VUMeter).vuMeterOffsetX
    private val barRectangle = Rectangle(barWidth, barHeight, color)

    override val children = mutableListOf<Widget>()

    override fun addChild(child: Widget) {
        /* VUBar will not have any children */
    }

    fun isVisible(t: Boolean) {
        barRectangle.isVisible = t
    }

    fun updateOffsetX(new: Double) {
        barOffsetX = new
        barRectangle.translateX = barOffsetX
    }

    fun updateOffsetY(new: Double) {
        barOffsetY = new
        barRectangle.translateY = barOffsetY
    }

    override fun addMeToScene(root: StackPane) {
        barRectangle.translateX = barOffsetX
        barRectangle.translateY = barOffsetY
        barRectangle.arcWidth = 5.0
        barRectangle.arcHeight = 5.0
        root.children.add(barRectangle)
    }

    override fun removeMeFromScene(root: StackPane) {
        Platform.runLater {
            root.children.remove(barRectangle)
        }
    }
}