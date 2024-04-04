package org.jzeisel.app_test.components.trackComponents

import javafx.event.EventHandler
import javafx.scene.input.KeyEvent
import javafx.scene.input.MouseButton
import javafx.scene.input.MouseEvent
import javafx.scene.layout.StackPane
import javafx.scene.shape.Rectangle
import javafx.scene.text.Text
import javafx.scene.text.TextAlignment
import org.jzeisel.app_test.components.interfaces.TrackElement
import org.jzeisel.app_test.components.interfaces.widget.Widget
import org.jzeisel.app_test.components.ephemeral.TextField
import org.jzeisel.app_test.components.Track
import org.jzeisel.app_test.components.MasterTrack
import org.jzeisel.app_test.components.NormalTrack
import org.jzeisel.app_test.components.interfaces.WindowElement
import org.jzeisel.app_test.components.interfaces.widget.NodeWidget
import org.jzeisel.app_test.util.BroadcastType
import org.jzeisel.app_test.util.viewOrderFlip

class InputNameBox(private val root: StackPane, override val parent: Widget)
    : NodeWidget, TrackElement, WindowElement {

    override val children = mutableListOf<Widget>()
    private val parentTrack = parent as Track
    private val trackListViewModel = parentTrack.trackListViewModel
    private val trackListState = trackListViewModel._trackListStateFlow.state
    private val nameText = Text()
    private val generalBox = Rectangle(trackListState.inputNameBoxWidth,
                                        trackListState.buttonSize,
                                        trackListState.generalGray)
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
        generalBox.translateY = parentTrack.trackOffsetY - trackListState.verticalDistancesBetweenWidgets
        generalBox.translateX = -(trackListViewModel.stage.width / 2) + trackListState.inputNameBoxOffset
        generalBox.strokeWidth = trackListState.strokeSize
        generalBox.stroke = trackListState.strokeColor
        generalBox.arcWidth = trackListState.arcSize
        generalBox.arcHeight = trackListState.arcSize
        generalBox.opacity = 0.8
        generalBox.viewOrder = viewOrderFlip - 0.31
        nameText.text = name
        nameText.translateY = generalBox.translateY
        nameText.translateX = generalBox.translateX
        nameText.textAlignment = TextAlignment.CENTER
        nameText.fill = trackListState.strokeColor
        nameText.isVisible = true
        nameText.opacity = 0.95
        nameText.viewOrder = viewOrderFlip - 0.32

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

    override fun respondToChange(broadcastType: BroadcastType, old: Double, new: Double) {
        when (broadcastType) {
            BroadcastType.STAGE_WIDTH -> respondToWidthChange(old, new)
            BroadcastType.STAGE_HEIGHT -> respondToHeightChange(old, new)
            BroadcastType.INDEX -> respondToIndexChange(old, new)
            BroadcastType.DIVIDER -> {}
            BroadcastType.SCROLL -> {}
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
        generalBox.translateY = parentTrack.trackOffsetY - trackListState.verticalDistancesBetweenWidgets
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
    }

    private fun returnValueFromTextField(userName: String) {
        nameSetByUser = true
        name = userName
    }
}