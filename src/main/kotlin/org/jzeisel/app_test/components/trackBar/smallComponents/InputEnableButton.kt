package org.jzeisel.app_test.components.trackBar.smallComponents

import javafx.application.Platform
import javafx.event.EventHandler
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.input.MouseEvent
import javafx.scene.layout.StackPane
import javafx.scene.paint.Color
import javafx.scene.shape.Rectangle
import org.jzeisel.app_test.components.TrackComponentWidget
import org.jzeisel.app_test.components.Widget
import org.jzeisel.app_test.components.trackBar.tracks.NormalTrack
import org.jzeisel.app_test.components.trackBar.tracks.Track
import org.jzeisel.app_test.logger.Logger

class InputEnableButton(override val parent: Widget?): Widget, TrackComponentWidget {
    companion object {
        const val TAG = "InputEnableButton"
        const val LEVEL = 3
    }
    private val parentTrack = parent as Track
    private val trackListViewModel = parentTrack.trackListViewModel
    private var isEnabled: Boolean = false
    private val buttonWidth = 20.0
    private val buttonHeight = 20.0
    private val buttonOffsetY = parentTrack.trackOffsetY - 15.0
    private val buttonOffsetX = -(trackListViewModel.stage.width / 2) + trackListViewModel.inputButtonsOffset
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
        iImageView.fitHeight = 15.0
        iImageView.isPreserveRatio = true
        iImageView.translateY = buttonOffsetY
        iImageView.translateX = buttonOffsetX
        buttonRect.translateY = buttonOffsetY
        buttonRect.translateX = buttonOffsetX
        buttonRect.arcWidth = 5.0
        buttonRect.arcHeight = 5.0
        buttonRect.stroke = Color.BLACK
        buttonRect.strokeWidth = 1.6
        buttonRect.onMouseReleased = mouseReleaseEvent
        iImageView.onMouseReleased = mouseReleaseEvent
    }

    private fun mouseReleaseLeft() {
        when (isEnabled) {
            true -> {
                isEnabled = false
                buttonRect.fill = Color.TRANSPARENT
                (parentTrack as NormalTrack).audioInputDisable()
            }
            false -> {
                if ((parentTrack as NormalTrack).audioInputEnable()) {
                    isEnabled = true
                    buttonRect.fill = Color.rgb(0xFF, 0x64, 0x40)
                    buttonRect.opacity = 0.9

                    parentTrack.startGettingDataForVuMeter()
                }
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
            root.children.remove(iImageView)
        }
    }

    override fun respondToOffsetYChange(old: Double, new: Double) {
        buttonRect.translateY += new - old
        iImageView.translateY += new - old
    }

    override fun respondToWidthChange(old: Double, new: Double) {
        buttonRect.translateX -= (new - old)/2.0
        iImageView.translateX -= (new - old)/2.0
    }

}