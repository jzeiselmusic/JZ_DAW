package org.jzeisel.app_test.viewmodel

import javafx.event.Event
import javafx.event.EventHandler
import javafx.scene.Cursor
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.input.MouseEvent
import javafx.scene.layout.Background
import javafx.scene.layout.BackgroundFill
import javafx.scene.layout.StackPane
import javafx.scene.paint.Color
import javafx.scene.shape.Circle
import javafx.scene.shape.Rectangle
import org.jzeisel.app_test.audio.viewmodel.ViewModelController
import org.jzeisel.app_test.components.interfaces.widget.NodeWidget
import org.jzeisel.app_test.components.interfaces.widget.Widget
import org.jzeisel.app_test.main
import org.jzeisel.app_test.stateflow.TrackListStateFlow
import org.jzeisel.app_test.util.animateObjectScale
import org.jzeisel.app_test.util.viewOrderFlip

class MixerViewModel(
    val root: StackPane,
    val viewModelController: ViewModelController,
    val trackListStateFlow: TrackListStateFlow): NodeWidget {

    override val parent: Widget? = null
    override val children = mutableListOf<Widget>()

    private val dividerRect = Rectangle()
    private var dividerPressed = false
    private val toolBarRect = Rectangle()
    private val metronomeButton = Circle()
    private var metronomeButtonEnabled = false
    private val metronomeImage = Image("file:/Users/jacobzeisel/git/App_Test/src/main/resources/metronome.png")
    private val imageView = ImageView(metronomeImage)


    init {
        trackListStateFlow.state.stageHeightProperty.addListener { _, _, new ->
            root.translateY = new.toDouble()/2.0 - root.maxHeight / 2.0
            toolBarRect.translateY = -root.maxHeight/2.0 + toolBarRect.height/2.0
            dividerRect.translateY = -root.maxHeight/2.0
            metronomeButton.translateY = toolBarRect.translateY
            imageView.translateY = metronomeButton.translateY
        }
        trackListStateFlow.state.stageWidthProperty.addListener { _, _, new ->
            root.maxWidth = new.toDouble()
            toolBarRect.width = new.toDouble()
            dividerRect.width = new.toDouble()
        }
    }
    override fun addChild(child: Widget) {
        /* children should be track objects */
    }

    override fun addMeToScene(root: StackPane) {
        root.maxWidth = trackListStateFlow.state.stageWidthProperty.value
        root.maxHeight = trackListStateFlow.state.stageHeightProperty.value/3.5
        root.background = Background(BackgroundFill(Color.DARKGRAY.darker().darker().darker(), null, null))
        root.translateX = 0.0
        root.translateY = trackListStateFlow.state.stageHeightProperty.value /2.0 - root.maxHeight / 2.0

        toolBarRect.width = root.maxWidth
        toolBarRect.height = 65.0
        toolBarRect.fill = Color.DARKGRAY.darker()
        toolBarRect.stroke = Color.BLACK
        toolBarRect.strokeWidth = 1.8
        toolBarRect.translateX = 0.0
        toolBarRect.translateY = -root.maxHeight/2.0 + toolBarRect.height/2.0
        toolBarRect.viewOrder = viewOrderFlip - 0.01

        metronomeButton.radius = 17.0
        metronomeButton.translateX = 0.0
        metronomeButton.translateY = toolBarRect.translateY
        metronomeButton.fill = Color.TRANSPARENT
        metronomeButton.stroke = Color.BLACK
        metronomeButton.strokeWidth = 1.8
        metronomeButton.viewOrder = viewOrderFlip - 0.02
        metronomeButton.onMousePressed = EventHandler {
            animateObjectScale(1.0, 0.9, metronomeButton, 50.0)
        }
        metronomeButton.onMouseReleased = EventHandler {
            animateObjectScale(0.9, 1.0, metronomeButton, 40.0)
            if (metronomeButtonEnabled) {
                metronomeButtonEnabled = false
                metronomeButton.fill = Color.TRANSPARENT
            }
            else {
                metronomeButtonEnabled = true
                metronomeButton.fill = trackListStateFlow.state.generalPurple
            }
        }
        imageView.translateY = metronomeButton.translateY
        imageView.viewOrder = viewOrderFlip - 0.03
        imageView.isMouseTransparent = true
        imageView.fitWidth = 20.0
        imageView.fitHeight = 20.0

        dividerRect.width = root.maxWidth
        dividerRect.height = 1.8
        dividerRect.fill = Color.BLACK
        dividerRect.stroke =Color.BLACK
        dividerRect.strokeWidth = 1.8
        dividerRect.translateX = 0.0
        dividerRect.translateY = -root.maxHeight/2.0
        dividerRect.viewOrder = viewOrderFlip - 0.5

        dividerRect.onMouseEntered = EventHandler {
            root.cursor = Cursor.N_RESIZE
            dividerRect.height = 2.0
            dividerRect.strokeWidth = 2.0
            dividerRect.fill = Color.WHITESMOKE.darker()
            dividerRect.stroke = Color.WHITESMOKE.darker()
        }
        dividerRect.onMouseExited = EventHandler {
            if (!dividerPressed) {
                root.cursor = Cursor.DEFAULT
                dividerRect.height = 1.8
                dividerRect.stroke = Color.BLACK
                dividerRect.fill = Color.BLACK
                dividerRect.strokeWidth = 1.8
            }
        }
        dividerRect.onMousePressed = EventHandler {
            dividerPressed = true
        }
        dividerRect.onMouseReleased = EventHandler {
            root.cursor = Cursor.DEFAULT
            dividerRect.height = 1.8
            dividerRect.stroke = Color.BLACK
            dividerRect.fill = Color.BLACK
            dividerRect.strokeWidth = 1.8
            dividerPressed = false
        }
        dividerRect.onMouseDragged = EventHandler {
            root.maxHeight -= it.y
            root.translateY = trackListStateFlow.state.stageHeightProperty.value /2.0 - root.maxHeight / 2.0
            dividerRect.translateY = -root.maxHeight/2.0
            toolBarRect.translateY = -root.maxHeight/2.0 + toolBarRect.height/2.0
            metronomeButton.translateY = toolBarRect.translateY
            imageView.translateY = metronomeButton.translateY
        }
        root.children.addAll(dividerRect, toolBarRect, metronomeButton, imageView)
    }

    override fun removeMeFromScene(root: StackPane) {

    }
}