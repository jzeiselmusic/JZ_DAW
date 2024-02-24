package org.jzeisel.app_test.components.trackBar.smallComponents

import javafx.event.EventHandler
import javafx.scene.input.KeyEvent
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
import org.jzeisel.app_test.components.trackBar.tracks.NormalTrack
import org.jzeisel.app_test.util.Observable
import org.jzeisel.app_test.util.ObservableListener

class InputNameBox(private val root: StackPane, override val parent: Widget)
    : Widget, TrackComponentWidget, ObservableListener<Double> {

    override val children = mutableListOf<Widget>()
    private val parentTrack = parent as Track
    private val trackListViewModel = parentTrack.trackListViewModel
    private val nameText = Text()
    private val generalBox = Rectangle(trackListViewModel.inputNameBoxWidth,
                                        trackListViewModel.buttonSize,
                                        trackListViewModel.generalGray)
    private val textField = TextField(generalBox, nameText, trackListViewModel, ::returnValueFromTextField)
    var nameSetByUser = false

    var name: String = ""
        get() {
            return if (nameSetByUser) {
                field
            } else {
                if (parentTrack is MasterTrack) "Master"
                else "Track ${parentTrack.name}"
            }
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
        generalBox.opacity = 0.8
        nameText.text = name
        nameText.translateY = generalBox.translateY
        nameText.translateX = generalBox.translateX
        nameText.textAlignment = TextAlignment.CENTER
        nameText.fill = trackListViewModel.strokeColor
        nameText.isVisible = true
        nameText.opacity = 0.95

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

    override fun respondToChange(observable: Observable<*>, old: Double, new: Double) {
        when (observable) {
            trackListViewModel.testStageWidth -> respondToWidthChange(old, new)
            trackListViewModel.testStageHeight -> respondToHeightChange(old, new)
            (parentTrack as NormalTrack).index -> respondToIndexChange(old, new)
        }
    }

    override fun registerForBroadcasts() {
        trackListViewModel.registerForHeightChanges(this)
        trackListViewModel.registerForWidthChanges(this)
        if (parentTrack is NormalTrack) {
            parentTrack.registerForIndexChanges(this)
        }
    }

    override fun unregisterForBroadcasts() {
        trackListViewModel.unregisterForWidthChanges(this)
        trackListViewModel.unregisterForHeightChanges(this)
        if (parentTrack is NormalTrack) {
            parentTrack.unregisterForIndexChanges(this)
        }
    }

    override fun addChild(child: Widget) {
    }

    override fun addMeToScene(root: StackPane) {
        registerForBroadcasts()
        root.children.add(generalBox)
        root.children.add(nameText)
    }

    override fun removeMeFromScene(root: StackPane) {
        unregisterForBroadcasts()
        root.children.remove(generalBox)
        root.children.remove(nameText)
    }

    override fun respondToHeightChange(old: Double, new: Double) {
        ((new - old)/2.0).let {
            generalBox.translateY -= it
            nameText.translateY -= it
        }
    }

    override fun respondToWidthChange(old: Double, new: Double) {
        ((new - old)/2.0).let {
            generalBox.translateX -= it
            nameText.translateX -= it
        }
    }

    override fun respondToIndexChange(old: Double, new: Double) {
        generalBox.translateY = parentTrack.trackOffsetY - trackListViewModel.verticalDistancesBetweenWidgets
        nameText.translateY = generalBox.translateY
    }

    fun backspaceText() {
        textField.backspace()
    }

    fun characterText(character: KeyEvent) {
        textField.character(character)
    }

    fun exitTextField(root: StackPane) {
        textField.removeMeFromScene(root)
        generalBox.toBack()
        nameText.toBack()
        generalBox.toFront()
        nameText.toFront()
    }

    private fun returnValueFromTextField(userName: String) {
        nameSetByUser = true
        name = userName
    }
}