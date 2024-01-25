package org.jzeisel.app_test

import javafx.application.Application
import javafx.scene.Scene
import javafx.scene.layout.Pane
import javafx.scene.layout.StackPane
import javafx.scene.paint.Color
import javafx.scene.paint.Paint
import javafx.stage.Stage
import org.jzeisel.app_test.audio.AudioProcessor
import org.jzeisel.app_test.audio.Recorder
import org.jzeisel.app_test.component.trackBar.tracks.TrackList

class AudioWaveform : Application() {
    companion object {
        private const val INIT_STAGE_HEIGHT = 320.0
        private const val INIT_STAGE_WIDTH = 640.0
    }
    private var streamingThread: Thread? = null
    private lateinit var root: StackPane
    private lateinit var recorder: Recorder
    private val audioProcessor = AudioProcessor(25)

    private fun startStreamingFromMicrophone() {
        recorder = Recorder()
        if (recorder.findInputDevice()) {
            if (recorder.startInputStreaming()) {
                streamingThread = Thread {
                    while(true) {
                        val data = recorder.getLatestStreamByteArray()
                        if (data != null) {
                            val count = data.size
                            for (i in 0 until count step 2) {
                                val sample: Int = Recorder.bytesToInt(data[i], data[i+1])
                                println("\t value: $sample")
                                audioProcessor.addSample(sample)
                            }
                        }
                    }
                }
                streamingThread!!.start()
            }
        }
    }

    override fun start(stage: Stage) {
        stage.title = "My App"
        stage.isResizable = true
        stage.width = INIT_STAGE_WIDTH
        stage.height = INIT_STAGE_HEIGHT + 4
        stage.isResizable = true
        root = StackPane()
        val scene = Scene(root, Paint.valueOf(Color.DIMGREY.darker().darker().toString()))
        stage.scene = scene
        // startStreamingFromMicrophone()

        val trackList = TrackList(root, stage)
        trackList.addMeToScene(root)
        /*
        val timeline = Timeline(
                KeyFrame(Duration.millis(25.0),
                        { firstVuMeter.setBarsBasedOnAudio(audioProcessor) }))
        timeline.cycleCount = Timeline.INDEFINITE
        timeline.play() */
        stage.show()
    }
}

fun main() {
    Application.launch(AudioWaveform::class.java)
}