package org.jzeisel.app_test.error

import javafx.event.EventHandler
import javafx.scene.layout.StackPane
import javafx.scene.paint.Color
import javafx.scene.shape.Rectangle
import javafx.scene.text.FontWeight
import javafx.scene.text.Text
import org.jzeisel.app_test.components.interfaces.WindowElement
import org.jzeisel.app_test.components.interfaces.widget.SingularWidget
import org.jzeisel.app_test.stateflow.TrackListState
import org.jzeisel.app_test.util.BroadcastType
import org.jzeisel.app_test.util.viewOrderFlip
import org.jzeisel.app_test.viewmodel.TrackListViewModel
import javafx.scene.text.Font
import javafx.scene.text.Font.font

class PanicErrorMessage(type: ErrorType,
                        val trackListState: TrackListState,
                        val viewModel: TrackListViewModel,
                        message: String?): SingularWidget, WindowElement {
    /* this class should create a popup that alerts the user to an error
        and allows them to either close the app or ignore the error */
    private val width: Double get() { return trackListState.observableStageWidth.getValue() + 100.0 }
    private val height: Double get() { return trackListState.observableStageHeight.getValue() + 100.0 }

    private val grayTint = Rectangle(width, height, Color.DIMGREY.darker().darker())
    private val errorMessageBox = Rectangle(250.0, 150.0, Color.DIMGREY)
    private val errorMessageTitle = Text(type.readable)
    private val errorMessageText = Text(message)

    private val firstButton = Rectangle(70.0, 30.0, trackListState.generalPurple)
        private val firstButtonText = Text("ignore")
    private val secondButton = Rectangle(70.0, 30.0, trackListState.generalPurple)
        private val secondButtonText = Text("save & exit")
    init {
        grayTint.viewOrder = viewOrderFlip - 1.0
        grayTint.opacity = 0.7

        errorMessageBox.viewOrder = viewOrderFlip - 1.01
        errorMessageBox.arcWidth = 10.0
        errorMessageBox.arcHeight = 10.0
        errorMessageBox.strokeWidth = 1.5
        errorMessageBox.stroke = Color.BLACK

        errorMessageTitle.font = font(Font.getDefault().toString(), FontWeight.BOLD, 14.0)
        errorMessageTitle.viewOrder = viewOrderFlip - 1.02
        errorMessageTitle.translateY = -50.0

        errorMessageText.viewOrder = viewOrderFlip - 1.02

        firstButton.translateX = -50.0
        firstButton.translateY = 50.0
        firstButton.arcWidth = 10.0
        firstButton.arcHeight = 10.0
        firstButton.strokeWidth = 1.0
        firstButton.stroke = Color.BLACK
        firstButton.viewOrder = viewOrderFlip - 1.02
        firstButton.onMousePressed = EventHandler {
            firstButton.opacity = 0.5
            firstButtonText.opacity = 0.5
        }
        firstButton.onMouseReleased = EventHandler {
            firstButton.opacity = 1.0
            firstButtonText.opacity = 1.0
            viewModel.removeErrorMessage()
        }

        secondButton.translateX = 50.0
        secondButton.translateY = 50.0
        secondButton.arcWidth = 10.0
        secondButton.arcHeight = 10.0
        secondButton.strokeWidth = 1.0
        secondButton.stroke = Color.BLACK
        secondButton.viewOrder = viewOrderFlip - 1.02
        secondButton.onMousePressed = EventHandler {
            secondButton.opacity = 0.5
            secondButtonText.opacity = 0.5
        }
        secondButton.onMouseReleased = EventHandler {
            secondButton.opacity = 1.0
            secondButtonText.opacity = 1.0
            viewModel.removeErrorMessage()
        }

        firstButtonText.translateX = firstButton.translateX
        firstButtonText.translateY = firstButton.translateY
        firstButtonText.font = font(Font.getDefault().toString(), FontWeight.BOLD, 12.5)
        firstButtonText.isMouseTransparent = true
        firstButtonText.viewOrder = viewOrderFlip - 1.03

        secondButtonText.translateX = secondButton.translateX
        secondButtonText.translateY = secondButton.translateY
        secondButtonText.font = font(Font.getDefault().toString(), FontWeight.BOLD, 12.5)
        secondButtonText.isMouseTransparent = true
        secondButtonText.viewOrder = viewOrderFlip - 1.03
    }
    override fun addMeToScene(root: StackPane) {
        registerForBroadcasts()
        root.children.addAll(grayTint,
                            errorMessageBox,
                            errorMessageTitle,
                            errorMessageText,
                            firstButton, firstButtonText,
                            secondButton, secondButtonText)
    }

    override fun removeMeFromScene(root: StackPane) {
        unregisterForBroadcasts()
        root.children.removeAll(grayTint,
                            errorMessageBox,
                            errorMessageTitle,
                            errorMessageText,
                            firstButton, firstButtonText,
                            secondButton, secondButtonText)
    }

    override fun respondToChange(broadcastType: BroadcastType, old: Double, new: Double) {
        when (broadcastType) {
            BroadcastType.DIVIDER -> {}
            BroadcastType.STAGE_WIDTH -> { respondToWidthChange(old, new) }
            BroadcastType.STAGE_HEIGHT -> { respondToHeightChange(old, new) }
            BroadcastType.INDEX -> {}
            BroadcastType.SCROLL -> {}
        }
    }

    override fun registerForBroadcasts() {
        viewModel.registerForWidthChanges(this)
        viewModel.registerForHeightChanges(this)
    }

    override fun unregisterForBroadcasts() {
        viewModel.unregisterForWidthChanges(this)
        viewModel.unregisterForHeightChanges(this)
    }

    override fun respondToHeightChange(old: Double, new: Double) {
        grayTint.height = height
    }

    override fun respondToWidthChange(old: Double, new: Double) {
        grayTint.width = width
    }

}