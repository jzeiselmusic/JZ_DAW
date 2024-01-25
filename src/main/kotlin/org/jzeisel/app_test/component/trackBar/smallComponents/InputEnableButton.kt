package org.jzeisel.app_test.component.trackBar.smallComponents

import javafx.application.Platform
import javafx.event.EventHandler
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.input.MouseEvent
import javafx.scene.layout.StackPane
import javafx.scene.paint.Color
import javafx.scene.shape.Rectangle
import org.jzeisel.app_test.component.Widget
import org.jzeisel.app_test.component.trackBar.tracks.TrackList

class InputEnableButton(root: StackPane, yLocation: Double, override val parent: Widget?): Widget {
    private var isEnabled: Boolean = false
    private val buttonWidth = 20.0
    private val buttonHeight = 20.0
    private val buttonOffsetX = -(((parent!!.parent!!) as TrackList).stage.width / 2) + 60.0
    override val children = mutableListOf<Widget>()

    override fun addChild(child: Widget) {
        /* AddButton does not have any children */
    }

    private val buttonRect = Rectangle(buttonWidth, buttonHeight, Color.TRANSPARENT)
    private val iImage = Image("images/i_image.png")
    private var iImageView: ImageView = ImageView(iImage)

    private val mouseReleaseEvent = EventHandler<MouseEvent> {
        mouseReleaseLeft()
    }

    init {
        iImageView.fitHeight = 20.0
        iImageView.isPreserveRatio = true
        iImageView.translateY = yLocation
        iImageView.translateX = buttonOffsetX
        buttonRect.translateY = yLocation
        buttonRect.translateX = buttonOffsetX
        buttonRect.arcWidth = 5.0
        buttonRect.arcHeight = 5.0
        buttonRect.stroke = Color.BLACK
        buttonRect.strokeWidth = 1.6
        buttonRect.onMouseReleased = mouseReleaseEvent
        iImageView.onMouseReleased = mouseReleaseEvent

        (parent!!.parent as TrackList).stageWidthProperty
                .addListener{ _, old, new -> updatePositionOfX(old as Double, new as Double)}
        (parent.parent as TrackList).stageHeightProperty
                .addListener{ _, old, new -> updatePositionOfY(old as Double, new as Double)}
    }

    private fun updatePositionOfX(old: Double, new: Double) {
        buttonRect.translateX -= (new - old)/2.0
        iImageView.translateX -= (new - old)/2.0
    }

    private fun updatePositionOfY(old: Double, new: Double) {
        buttonRect.translateY -= (new - old)/2.0
        iImageView.translateY -= (new - old)/2.0
    }

    private fun mouseReleaseLeft() {
        when (isEnabled) {
            true -> {
                isEnabled = false
                buttonRect.fill = Color.TRANSPARENT
            }
            false -> {
                isEnabled = true
                buttonRect.fill = Color.rgb(0xFF, 0x64, 0x40).saturate()
            }
        }
    }

    override fun addMeToScene(root: StackPane) {
        root.children.add(buttonRect)
        root.children.add(iImageView)
    }

    override fun removeMeFromScene(root: StackPane) {
        Platform.runLater {
            root.children.remove(buttonRect)
        }
    }

}