package org.jzeisel.app_test.components.trackComponents

import javafx.scene.layout.StackPane
import org.jzeisel.app_test.components.interfaces.TrackElement
import org.jzeisel.app_test.components.interfaces.WindowElement
import org.jzeisel.app_test.components.interfaces.widget.NodeWidget
import org.jzeisel.app_test.components.interfaces.widget.Widget
import org.jzeisel.app_test.util.BroadcastType

class SoloButton(override val parent: Widget)
    : NodeWidget, TrackElement, WindowElement {

    override val children: MutableList<Widget> = mutableListOf()
    override fun addChild(child: Widget) {
        TODO("Not yet implemented")
    }

    override fun addMeToScene(root: StackPane) {
        TODO("Not yet implemented")
    }

    override fun removeMeFromScene(root: StackPane) {
        TODO("Not yet implemented")
    }

    override fun respondToIndexChange(old: Double, new: Double) {
        TODO("Not yet implemented")
    }

    override fun respondToHeightChange(old: Double, new: Double) {
        TODO("Not yet implemented")
    }

    override fun respondToWidthChange(old: Double, new: Double) {
        TODO("Not yet implemented")
    }

    override fun respondToChange(broadcastType: BroadcastType, old: Double, new: Double) {
        TODO("Not yet implemented")
    }

    override fun registerForBroadcasts() {
        TODO("Not yet implemented")
    }

    override fun unregisterForBroadcasts() {
        TODO("Not yet implemented")
    }
}