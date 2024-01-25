package org.jzeisel.app_test.component.trackBar.tracks

import javafx.event.EventHandler
import javafx.scene.layout.StackPane
import javafx.scene.paint.Color
import javafx.scene.shape.Rectangle
import javafx.scene.shape.StrokeLineJoin
import org.jzeisel.app_test.audio.Recorder
import org.jzeisel.app_test.component.Widget
import org.jzeisel.app_test.component.trackBar.smallComponents.AddButton
import org.jzeisel.app_test.component.trackBar.smallComponents.InputEnableButton
import org.jzeisel.app_test.component.vuMeter.VUMeter

abstract class Track(val root: StackPane, parent: Widget) {
    /* a track component has the following elements:
        1. a vuMeter
        2. a waveform box
        3. a panning component
        4. a volume component
        5. mute and solo buttons
        5. an input selector dropbox
        6. a button to add a new track
        7. a recorder object to save audio input
           when the master wants to record
     */
    val trackWidth = (parent as TrackList).stage.width
    val trackHeight = (parent as TrackList).trackHeight
    val trackColorNormal = Color.WHITESMOKE.darker()
    val trackColorHL = Color.WHITESMOKE
    val trackList = parent as TrackList
    val trackRectangle = Rectangle(trackWidth, trackHeight, trackColorNormal)
    abstract val trackOffsetY: Double
    abstract val addButton: AddButton
    abstract val vuMeter: VUMeter
    // abstract val recorder: Recorder
    abstract val inputEnableButton: InputEnableButton

    fun setTrackRectangleProperties() {
        trackRectangle.translateY = trackOffsetY
        trackRectangle.stroke = Color.BLACK
        trackRectangle.strokeWidth = 1.0
        trackRectangle.strokeLineJoin = StrokeLineJoin.ROUND
        trackRectangle.onMouseEntered = EventHandler {
            trackRectangle.fill = trackColorHL
        }
        trackRectangle.onMouseExited = EventHandler {
            trackRectangle.fill = trackColorNormal
        }
        trackRectangle.onMousePressed = EventHandler {
            trackRectangle.opacity = 0.7
        }
        trackRectangle.onMouseReleased = EventHandler {
            trackRectangle.opacity = 1.0
        }

        /* all tracks have the same width and height changes */
        trackList.stageWidthProperty.addListener {_, _, newWidth ->
                                    trackRectangle.width = newWidth as Double}
        trackList.stageHeightProperty.addListener {_, old, newHeight ->
                                    trackRectangle.translateY -= (newHeight as Double - old as Double)/2.0}
    }
}