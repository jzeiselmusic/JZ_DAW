package org.jzeisel.app_test.viewmodel

import javafx.event.EventHandler
import javafx.scene.Cursor
import javafx.scene.layout.Background
import javafx.scene.layout.BackgroundFill
import javafx.scene.layout.StackPane
import javafx.scene.paint.Color
import javafx.scene.shape.Rectangle
import org.jzeisel.app_test.audio.viewmodel.AudioViewModel
import org.jzeisel.app_test.audio.viewmodel.ViewModelController
import org.jzeisel.app_test.components.interfaces.widget.NodeWidget
import org.jzeisel.app_test.components.interfaces.widget.Widget
import org.jzeisel.app_test.components.mixerComponents.BpmDisplay
import org.jzeisel.app_test.components.mixerComponents.MasterMixerFader
import org.jzeisel.app_test.components.mixerComponents.MixerButton
import org.jzeisel.app_test.components.mixerComponents.TimeDisplay
import org.jzeisel.app_test.stateflow.TrackListStateFlow
import org.jzeisel.app_test.util.animateObjectScale
import org.jzeisel.app_test.util.viewOrderFlip

class MixerViewModel(
    val root: StackPane,
    private val viewModelController: ViewModelController,
    private val trackListStateFlow: TrackListStateFlow,
    private val audioViewModel: AudioViewModel): NodeWidget {
    init {
        root.maxWidth = trackListStateFlow.state.stageWidthProperty.value
        root.maxHeight = trackListStateFlow.state.stageHeightProperty.value/2.0
        root.background = Background(BackgroundFill(Color.DARKGRAY.darker().darker().darker(), null, null))
        root.translateX = 0.0
        root.translateY = trackListStateFlow.state.stageHeightProperty.value /2.0 - root.maxHeight / 2.0
    }

    override val parent: Widget? = null
    override val children = mutableListOf<Widget>()
    val toolBarButtons = mutableListOf<MixerButton>()
    val toolBarInitHeight = 35.0
    val toolBarY = -root.maxHeight/2.0 + toolBarInitHeight/2.0
    val screenWidth: Double get() { return trackListStateFlow.state.stageWidthProperty.value }

    private val dividerRect = Rectangle()
    private var dividerPressed = false
    val toolBarRect = Rectangle()
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
    private val timeDisplay = TimeDisplay(
        toolBarY,
        toolBarInitHeight
    )
    private val bpmDisplay = BpmDisplay(
        ::bpmCallback,
        ::isShiftPressed,
        toolBarY,
        toolBarInitHeight,
        timeDisplay.getWidth(),
        audioViewModel.tempo
    )

    fun bpmCallback(result: Double) {
        audioViewModel.setTempo(result)
    }

    fun isShiftPressed(): Boolean {
        return viewModelController.isShiftPressed()
    }

    init {
        setMousePressFunctions()
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
        /* children should be track fader objects */
    }

    override fun addMeToScene(root: StackPane) {
        setUpDividerRectangles()
        root.children.addAll(dividerRect, toolBarRect)
        toolBarButtons.forEach {button->
            button.addMeToScene(root)
        }
        timeDisplay.addMeToScene(root)
        bpmDisplay.addMeToScene(root)
        val master = MasterMixerFader(this)
        addChild(master)
        master.addMeToScene(root)
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
        root.children.removeAll(dividerRect, toolBarRect)
        toolBarButtons.forEach {
            it.removeMeFromScene(root)
        }
        timeDisplay.removeMeFromScene(root)
        bpmDisplay.removeMeFromScene(root)
    }

    fun setMousePressFunctions() {
        playButton.setOnMouseReleased {
            animateObjectScale(0.9, 1.0, playButton.button, 40.0)
            play(!playButton.buttonEnabled, true)
        }
        stopButton.setOnMouseReleased {
            animateObjectScale(0.9, 1.0, stopButton.button, 40.0)
            viewModelController.stopPressed()
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
    }

    fun setUpDividerRectangles() {
        toolBarRect.width = root.maxWidth
        toolBarRect.height = 60.0
        toolBarRect.fill = Color.DARKGRAY.darker()
        toolBarRect.stroke = Color.BLACK
        toolBarRect.strokeWidth = 1.8
        toolBarRect.translateX = 0.0
        toolBarRect.translateY = -root.maxHeight/2.0 + toolBarRect.height/2.0
        toolBarRect.viewOrder = viewOrderFlip - 0.01

        toolBarButtons.forEach {
            it.updateTranslateY(toolBarRect.translateY)
        }
        timeDisplay.updateTranslateY(toolBarRect.translateY)
        bpmDisplay.updateTranslateY(toolBarRect.translateY)

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
            timeDisplay.updateTranslateY(toolBarRect.translateY)
            bpmDisplay.updateTranslateY(toolBarRect.translateY)
        }
    }

    fun updateSamples(newSamples: Int, sampleRate: Int) {
        timeDisplay.updateSamples(newSamples, sampleRate)
    }
}