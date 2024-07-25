package org.jzeisel.app_test.components.mixerComponents

import javafx.scene.paint.Color
import javafx.scene.shape.Rectangle
import javafx.scene.shape.StrokeLineJoin
import org.jzeisel.app_test.components.interfaces.widget.Widget
import org.jzeisel.app_test.util.viewOrderFlip
import org.jzeisel.app_test.viewmodel.MixerViewModel

abstract class MixerFader(parent: Widget) {
    val mixerViewModel = parent as MixerViewModel

    val faderWidth = 70.0
    val faderHeight = mixerViewModel.root.maxHeight - mixerViewModel.toolBarRect.height
    val faderColorNormal = Color.WHITESMOKE.darker().darker()
    val faderColorHL = Color.WHITESMOKE.darker()
    val faderRectangle = Rectangle(faderWidth, faderHeight, faderColorNormal)

    abstract var faderOffsetX: Double

    fun setTrackRectangleProperties() {
        faderRectangle.translateX = faderOffsetX
        faderRectangle.translateY = mixerViewModel.toolBarRect.height / 2.0
        faderRectangle.stroke = Color.BLACK
        faderRectangle.strokeWidth = 0.4
        faderRectangle.strokeLineJoin = StrokeLineJoin.MITER
        faderRectangle.viewOrder = viewOrderFlip - 0.04
    }
}