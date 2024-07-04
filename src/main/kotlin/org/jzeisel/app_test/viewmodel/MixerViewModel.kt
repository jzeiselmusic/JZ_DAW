package org.jzeisel.app_test.viewmodel

import javafx.event.Event
import javafx.event.EventHandler
import javafx.scene.Cursor
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.image.WritableImage
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
import org.jzeisel.app_test.components.mixerComponents.MixerButton
import org.jzeisel.app_test.main
import org.jzeisel.app_test.stateflow.TrackListStateFlow
import org.jzeisel.app_test.util.Logger
import org.jzeisel.app_test.util.animateObjectScale
import org.jzeisel.app_test.util.loop
import org.jzeisel.app_test.util.viewOrderFlip

class MixerViewModel(
    val root: StackPane,
    val viewModelController: ViewModelController,
    val trackListStateFlow: TrackListStateFlow): NodeWidget {

    override val parent: Widget? = null
    override val children = mutableListOf<Widget>()
    val toolBarButtons = mutableListOf<MixerButton>()

    val toolBarInitHeight = 35.0
    val toolBarY = -root.maxHeight/2.0 + toolBarInitHeight/2.0

    init {
        Logger.debug(javaClass.simpleName, "toolBarY: $toolBarY", 3)
    }
    private val dividerRect = Rectangle()
    private var dividerPressed = false
    private val toolBarRect = Rectangle()
    private val playButton = MixerButton(
        "file:/Users/jacobzeisel/git/App_Test/src/main/resources/play.png",
        0,
        trackListStateFlow.state.stageWidthProperty.value,
        toolBarY
    )
    private val stopButton = MixerButton(
        "file:/Users/jacobzeisel/git/App_Test/src/main/resources/stop.png",
        1,
        trackListStateFlow.state.stageWidthProperty.value,
        toolBarY
    )
    private val loopButton = MixerButton(
        "file:/Users/jacobzeisel/git/App_Test/src/main/resources/loop.png",
        2,
        trackListStateFlow.state.stageWidthProperty.value,
        toolBarY
    )
    private val metronomeButton = MixerButton(
        "file:/Users/jacobzeisel/git/App_Test/src/main/resources/metronome.png",
        3,
        trackListStateFlow.state.stageWidthProperty.value,
        toolBarY
    )

    init {
        playButton.setOnMousePressed {
            animateObjectScale(1.0, 0.9, playButton.button, 50.0)
        }
        playButton.setOnMouseReleased {
            animateObjectScale(0.9, 1.0, playButton.button, 40.0)
            play(!playButton.buttonEnabled, true)
        }
        stopButton.setOnMousePressed {
            animateObjectScale(1.0, 0.9, stopButton.button, 50.0)
        }
        stopButton.setOnMouseReleased {
            animateObjectScale(0.9, 1.0, stopButton.button, 40.0)
            viewModelController.stopPressed()
        }
        loopButton.setOnMousePressed {
            animateObjectScale(1.0, 0.9, loopButton.button, 50.0)
        }
        loopButton.setOnMouseReleased {
            animateObjectScale(0.9, 1.0, loopButton.button, 40.0)
            if (loopButton.buttonEnabled) {
                loopButton.buttonEnabled = false
                loopButton.button.fill = Color.TRANSPARENT
                viewModelController.enableLooper(false)
            }
            else {
                loopButton.buttonEnabled = true
                loopButton.button.fill = Color.WHITESMOKE
                viewModelController.enableLooper(true)
            }
        }
        metronomeButton.setOnMousePressed {
            animateObjectScale(1.0, 0.9, metronomeButton.button, 50.0)
        }
        metronomeButton.setOnMouseReleased {
            animateObjectScale(0.9, 1.0, metronomeButton.button, 40.0)
            if (metronomeButton.buttonEnabled) {
                metronomeButton.buttonEnabled = false
                metronomeButton.button.fill = Color.TRANSPARENT
                viewModelController.enableMetronome(false)
            }
            else {
                metronomeButton.buttonEnabled = true
                metronomeButton.button.fill = Color.WHITESMOKE
                viewModelController.enableMetronome(true)
            }
        }
        toolBarButtons.addAll(listOf(playButton, stopButton, loopButton, metronomeButton))
        trackListStateFlow.state.stageHeightProperty.addListener { _, _, new ->
            root.translateY = new.toDouble()/2.0 - root.maxHeight / 2.0
            toolBarRect.translateY = -root.maxHeight/2.0 + toolBarRect.height/2.0
            dividerRect.translateY = -root.maxHeight/2.0
        }
        trackListStateFlow.state.stageWidthProperty.addListener { _, _, new ->
            root.maxWidth = new.toDouble()
            toolBarRect.width = new.toDouble()
            dividerRect.width = new.toDouble()

            toolBarButtons.forEach {
                it.updateStageWidth(new.toDouble())
            }
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

        toolBarButtons.forEach {
            it.updateTranslateY(toolBarRect.translateY)
        }

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

            toolBarButtons.forEach {button->
                button.updateTranslateY(toolBarRect.translateY)
            }
        }
        root.children.addAll(dividerRect, toolBarRect)
        toolBarButtons.forEach {button->
            button.addMeToScene(root)
        }
    }

    fun play(enabled: Boolean, action: Boolean) {
        when(enabled) {
            true -> {
                playButton.buttonEnabled = true
                playButton.button.opacity = 1.0
                playButton.button.fill = trackListStateFlow.state.generalPurple
            }
            false -> {
                playButton.buttonEnabled = false
                playButton.button.opacity = 0.5
                playButton.button.fill = Color.TRANSPARENT
            }
        }
        if (action) viewModelController.spacePressed()
    }

    override fun removeMeFromScene(root: StackPane) {

    }
}