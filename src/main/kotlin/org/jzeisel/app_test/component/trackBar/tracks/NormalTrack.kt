package org.jzeisel.app_test.component.trackBar.tracks

import javafx.application.Platform
import javafx.scene.layout.StackPane
import org.jzeisel.app_test.component.Widget
import org.jzeisel.app_test.component.trackBar.smallComponents.AddButton
import org.jzeisel.app_test.component.trackBar.smallComponents.InputEnableButton
import org.jzeisel.app_test.component.trackBar.smallComponents.InputSelectArrow
import org.jzeisel.app_test.component.vuMeter.VUMeter
import org.jzeisel.app_test.logger.Logger

class NormalTrack(root: StackPane, override val parent: Widget, override val name: String)
    : Track(root, parent), Widget {
    companion object {
        const val TAG = "NormalTrack"
        const val LEVEL = 1
    }

    /* same as a master track except its y offset will change */
    override var trackOffsetY = trackListViewModel.masterOffsetY +
            trackListViewModel.getNumTracks() * trackListViewModel.trackHeight
    override val children = mutableListOf<Widget>()
    init {
        setTrackRectangleProperties()
        /* all tracks have the same width and height changes */
        trackListViewModel.stageWidthProperty.addListener { _, _, newWidth ->
            trackRectangle.width = newWidth as Double

        }
        trackListViewModel.stageHeightProperty.addListener { _, old, newHeight ->
            trackRectangle.translateY -= (newHeight as Double - old as Double) / 2.0
        }
        Logger.debug(TAG, "instantiated: y-offset $trackOffsetY", LEVEL)
    }
    override val vuMeter = VUMeter(this)
    override val addButton = AddButton(this)
    override val inputEnableButton = InputEnableButton(this)
    override val inputSelectArrow = InputSelectArrow(root, this)

    override fun addMeToScene(root: StackPane) {
        root.children.add(trackRectangle)
        vuMeter.addMeToScene(root)
        addButton.addMeToScene(root)
        inputEnableButton.addMeToScene(root)
        inputSelectArrow.addMeToScene(root)
        addChild(vuMeter)
        addChild(addButton)
        addChild(inputEnableButton)
        addChild(inputSelectArrow)
    }

    private var audioInputIndex: Int? = null
    private var audioInputEnabled = false

    override fun addChild(child: Widget) {
        children.add(child)
    }

    override fun removeMeFromScene(root: StackPane) {
        Platform.runLater {
            for (child in children) {
                child.removeMeFromScene(root)
            }
            children.clear()
            root.children.remove(trackRectangle)
        }
    }

    fun audioInputEnable() {
        trackListViewModel.setTrackEnabled(this)
        audioInputEnabled = true
    }

    fun audioInputDisable() {
        trackListViewModel.setTrackDisabled(this)
        audioInputEnabled = false
    }

    fun setAudioInputIndex(index: Int) {
        audioInputIndex = index
        trackListViewModel.setTrackAudioInput(index, this)
    }
}