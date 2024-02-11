package org.jzeisel.app_test.components.trackBar.smallComponents

import javafx.scene.layout.StackPane
import javafx.scene.shape.Rectangle
import javafx.scene.text.Text
import javafx.scene.text.TextAlignment
import org.jzeisel.app_test.components.TrackComponentWidget
import org.jzeisel.app_test.components.Widget
import org.jzeisel.app_test.components.trackBar.tracks.Track
import org.jzeisel.app_test.components.trackBar.tracks.MasterTrack

class InputNameBox(override val parent: Widget) : Widget, TrackComponentWidget {
    override val children = mutableListOf<Widget>()
    private val parentTrack = parent as Track
    private val trackListViewModel = parentTrack.trackListViewModel
    private val generalBox = Rectangle(trackListViewModel.inputNameBoxWidth,
                                        trackListViewModel.buttonSize,
                                        trackListViewModel.generalGray)
    private val nameText = Text()

    init {
        generalBox.translateY = parentTrack.trackOffsetY - trackListViewModel.verticalDistancesBetweenWidgets
        generalBox.translateX = -(trackListViewModel.stage.width / 2) + trackListViewModel.inputNameBoxOffset
        generalBox.strokeWidth = trackListViewModel.strokeSize
        generalBox.stroke = trackListViewModel.strokeColor
        generalBox.arcWidth = trackListViewModel.arcSize
        generalBox.arcHeight = trackListViewModel.arcSize

        if (parentTrack is MasterTrack) nameText.text = "Master"
        else nameText.text = "Track ${parentTrack.name}"
        nameText.translateY = generalBox.translateY
        nameText.translateX = generalBox.translateX
        nameText.textAlignment = TextAlignment.CENTER
        nameText.fill = trackListViewModel.strokeColor
        nameText.isVisible = true
    }
    override fun addChild(child: Widget) {
    }

    override fun addMeToScene(root: StackPane) {
        root.children.add(generalBox)
        root.children.add(nameText)
    }

    override fun removeMeFromScene(root: StackPane) {
        root.children.remove(generalBox)
        root.children.remove(nameText)
    }

    override fun respondToOffsetYChange(old: Double, new: Double) {
        generalBox.translateY += new - old
        nameText.translateY += new - old
    }

    override fun respondToWidthChange(old: Double, new: Double) {
        generalBox.translateX -= (new - old)/2.0
        nameText.translateX -= (new - old)/2.0
    }
}