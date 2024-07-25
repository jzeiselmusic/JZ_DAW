package org.jzeisel.app_test.components.mixerComponents

import javafx.scene.layout.StackPane
import org.jzeisel.app_test.components.interfaces.widget.NodeWidget
import org.jzeisel.app_test.components.interfaces.widget.Widget

class MasterMixerFader(
    override val parent: Widget)
    : MixerFader(parent), NodeWidget {

    override var faderOffsetX: Double = -mixerViewModel.screenWidth/2.0 + faderWidth/2.0
    override val children: MutableList<Widget> = mutableListOf()

    override fun addChild(child: Widget) {
    }

    override fun addMeToScene(root: StackPane) {
        setTrackRectangleProperties()
        root.children.add(faderRectangle)
    }

    override fun removeMeFromScene(root: StackPane) {
        root.children.remove(faderRectangle)
    }
}