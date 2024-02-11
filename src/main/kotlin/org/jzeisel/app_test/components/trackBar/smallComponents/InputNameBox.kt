package org.jzeisel.app_test.components.trackBar.smallComponents

import javafx.event.EventHandler
import javafx.scene.input.MouseButton
import javafx.scene.input.MouseEvent
import javafx.scene.layout.StackPane
import javafx.scene.shape.Rectangle
import javafx.scene.text.Text
import javafx.scene.text.TextAlignment
import org.jzeisel.app_test.components.TrackComponentWidget
import org.jzeisel.app_test.components.Widget
import org.jzeisel.app_test.components.textfield.TextField
import org.jzeisel.app_test.components.trackBar.tracks.Track
import org.jzeisel.app_test.components.trackBar.tracks.MasterTrack
import org.jzeisel.app_test.logger.Logger

class InputNameBox(private val root: StackPane, override val parent: Widget) : Widget, TrackComponentWidget {
    companion object {
        const val TAG = "InputNameBox"
    }
    override val children = mutableListOf<Widget>()
    private val parentTrack = parent as Track
    private val trackListViewModel = parentTrack.trackListViewModel
    private val nameText = Text()
    private val generalBox = Rectangle(trackListViewModel.inputNameBoxWidth,
                                        trackListViewModel.buttonSize,
                                        trackListViewModel.generalGray)
    private val textField = TextField(generalBox, nameText, trackListViewModel)

    var name: String = ""
        get() {
            return if (parentTrack is MasterTrack) "Master"
            else "Track ${parentTrack.name}"
        }
        set(value) {
            nameText.text = value
            field = value
        }


    init {
        generalBox.translateY = parentTrack.trackOffsetY - trackListViewModel.verticalDistancesBetweenWidgets
        generalBox.translateX = -(trackListViewModel.stage.width / 2) + trackListViewModel.inputNameBoxOffset
        generalBox.strokeWidth = trackListViewModel.strokeSize
        generalBox.stroke = trackListViewModel.strokeColor
        generalBox.arcWidth = trackListViewModel.arcSize
        generalBox.arcHeight = trackListViewModel.arcSize

        nameText.text = name
        nameText.translateY = generalBox.translateY
        nameText.translateX = generalBox.translateX
        nameText.textAlignment = TextAlignment.CENTER
        nameText.fill = trackListViewModel.strokeColor
        nameText.isVisible = true

        val doubleClickHandler = EventHandler<MouseEvent>{
            if (it.button.equals(MouseButton.PRIMARY)) {
                if (it.clickCount == 2) {
                    textField.addMeToScene(root)
                }
            }
        }

        generalBox.onMouseClicked = doubleClickHandler
        nameText.onMouseClicked = doubleClickHandler
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

    fun backspaceText() {
        textField.backspace()
    }
}