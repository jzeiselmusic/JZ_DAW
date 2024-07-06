package org.jzeisel.app_test.components.mixerComponents

import javafx.event.EventHandler
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.input.MouseEvent
import javafx.scene.layout.StackPane
import javafx.scene.paint.Color
import javafx.scene.shape.Circle
import org.jzeisel.app_test.components.interfaces.widget.Widget
import org.jzeisel.app_test.util.animateObjectScale
import org.jzeisel.app_test.util.viewOrderFlip

class MixerButton(
    imagePath: String,
    private val orderNum: Int,
    stageWidth: Double,
    toolBarY: Double
) : Widget {

    val button = Circle()
    var buttonEnabled = false
    private val buttonImage = Image(imagePath)
    private val buttonView = ImageView(buttonImage)

    init {
        button.radius = 17.0
        button.translateX = -stageWidth / 2.0 + (orderNum + 1)*45.0
        button.translateY = toolBarY
        button.fill = Color.TRANSPARENT
        button.stroke = Color.BLACK
        button.opacity = 0.5
        button.strokeWidth = 1.9
        button.viewOrder = viewOrderFlip - 0.02

        buttonView.translateY = button.translateY
        buttonView.translateX = button.translateX
        buttonView.viewOrder = viewOrderFlip - 0.03
        buttonView.isMouseTransparent = true
        buttonView.fitWidth = 20.0
        buttonView.fitHeight = 20.0

        button.onMousePressed = EventHandler {
            animateObjectScale(1.0, 0.9, button, 50.0)
        }
    }

    fun updateStageWidth(new: Double) {
        button.translateX = -new / 2.0 + (orderNum + 1)*45.0
        buttonView.translateX = button.translateX
    }

    fun updateTranslateY(new: Double) {
        button.translateY = new
        buttonView.translateY = new
    }

    override fun addMeToScene(root: StackPane) {
        root.children.addAll(button, buttonView)
    }

    override fun removeMeFromScene(root: StackPane) {
        root.children.removeAll(button, buttonView)
    }

    fun setOnMousePressed(handler: EventHandler<MouseEvent>) {
        button.onMousePressed = handler
    }

    fun setOnMouseReleased(handler: EventHandler<MouseEvent>) {
        button.onMouseReleased = handler
    }
}