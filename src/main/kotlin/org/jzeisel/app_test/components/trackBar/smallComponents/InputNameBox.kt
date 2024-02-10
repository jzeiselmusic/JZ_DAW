package org.jzeisel.app_test.components.trackBar.smallComponents

import javafx.scene.layout.StackPane
import javafx.scene.paint.Color
import javafx.scene.shape.Rectangle
import org.jzeisel.app_test.components.TrackComponentWidget
import org.jzeisel.app_test.components.Widget
import org.jzeisel.app_test.components.trackBar.tracks.Track

class InputNameBox(override val parent: Widget) : Widget, TrackComponentWidget {
    override val children = mutableListOf<Widget>()
    private val parentTrack = parent as Track
    private val trackListViewModel = parentTrack.trackListViewModel
    private val generalBox = Rectangle(100.0, 20.0, Color.DIMGREY.darker())
    init {
        generalBox.translateY = parentTrack.trackOffsetY - 15.0
        generalBox.translateX = -(trackListViewModel.stage.width / 2) + trackListViewModel.inputNameBoxOffset
    }
    override fun addChild(child: Widget) {
    }

    override fun addMeToScene(root: StackPane) {
        root.children.add(generalBox)
    }

    override fun removeMeFromScene(root: StackPane) {
        root.children.remove(generalBox)
    }

    override fun respondToOffsetYChange(old: Double, new: Double) {
    }

    override fun respondToWidthChange(old: Double, new: Double) {
    }
}