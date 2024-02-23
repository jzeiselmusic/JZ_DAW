package org.jzeisel.app_test.components.vuMeter

import javafx.application.Platform
import javafx.scene.layout.StackPane
import javafx.scene.paint.Color
import javafx.scene.shape.Rectangle
import org.jzeisel.app_test.components.TrackComponentWidget
import org.jzeisel.app_test.components.Widget
import org.jzeisel.app_test.util.Observable
import org.jzeisel.app_test.util.ObservableListener

class VUBar(color: Color, private var barOffsetY: Double,
            override val parent: Widget)
    : Widget, TrackComponentWidget, ObservableListener<Double> {

    private val barWidth = (parent as VUMeter).vuMeterWidth - 4.0
    private val barHeight = (parent as VUMeter).barHeight
    private var barOffsetX = (parent as VUMeter).vuMeterOffsetX
    private val barRectangle = Rectangle(barWidth, barHeight, color)

    override val children = mutableListOf<Widget>()

    override fun addChild(child: Widget) {
        /* VUBar will not have any children */
    }

    fun bringToFront() {
        barRectangle.toFront()
    }

    fun isVisible(t: Boolean) {
        barRectangle.isVisible = t
    }

    override fun addMeToScene(root: StackPane) {
        registerForBroadcasts()
        barRectangle.translateX = barOffsetX
        barRectangle.translateY = barOffsetY
        barRectangle.arcWidth = 0.5
        barRectangle.arcHeight = 0.5
        root.children.add(barRectangle)
    }

    override fun removeMeFromScene(root: StackPane) {
        Platform.runLater {
            unregisterForBroadcasts()
            root.children.remove(barRectangle)
        }
    }

    override fun respondToHeightChange(old: Double, new: Double) {
        ((new - old)/2.0).let {
            barOffsetY -= it
            barRectangle.translateY -= it
        }
    }

    override fun respondToWidthChange(old: Double, new: Double) {
        ((new - old)/2.0).let {
            barOffsetX -= it
            barRectangle.translateX -= it
        }
    }

    override fun respondToChange(observable: Observable<*>, old: Double, new: Double) {
        when (observable) {
            (parent as VUMeter).trackListViewModel.testStageWidth -> respondToWidthChange(old, new)
            parent.trackListViewModel.testStageHeight -> respondToHeightChange(old, new)
        }
    }

    override fun registerForBroadcasts() {
        (parent as VUMeter).trackListViewModel.registerForWidthChanges(this)
        parent.trackListViewModel.registerForHeightChanges(this)
    }

    override fun unregisterForBroadcasts() {
        (parent as VUMeter).trackListViewModel.unregisterForWidthChanges(this)
        parent.trackListViewModel.unregisterForHeightChanges(this)
    }
}