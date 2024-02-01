package org.jzeisel.app_test.audio

import org.jzeisel.app_test.component.Widget
import org.jzeisel.app_test.component.trackBar.tracks.TrackListViewModel
import org.jzeisel.app_test.logger.Logger
import javax.sound.sampled.Mixer
import javax.sound.sampled.TargetDataLine

class AudioInputManager(private val parent: TrackListViewModel) {
    /*
    audio input manager keeps track of which audio inputs are enabled and
    provides an interface to read data from those inputs such that every
    track that uses that input gets the same information

    this class knows what audio ports are available, and enables them
    and starts streaming audio into the port when a track requests it.*/
    private val recorder = Recorder()
    private val audioProcessor = AudioProcessor(50)

    val audioInputByTrack = mutableMapOf<Widget, Int?>()
    /* widgets will only be in this list if they are enabled */
    val tracksEnabled = mutableListOf<Widget>()
    /* find all available audio inputs */
    val allMixerInfos: List<Mixer.Info> = recorder.findAllInputDevices().toList()
    val allMixerNames: List<String> = allMixerInfos.map { it.name }
    /* all ports are disabled by default */
    private var enabledMixers = Array(allMixerInfos.size) { false }
    private val targetDataLines: Array<TargetDataLine?> = Array(allMixerInfos.size) { null }
    val dataStreams: Array<Double> = Array(allMixerInfos.size) { 0.0 }
    private val dataAcquisitionThreads: Array<Thread?> = Array(allMixerInfos.size) { null }

    fun findIndexOfDefaultInput() {
        val mixer = recorder.findDefaultInputDevice()
        println("found default mixer: index ${allMixerInfos.indexOf(mixer)}")
    }

    fun addTrack(track: Widget) {
        audioInputByTrack[track] = null
    }

    fun setTrackAudioInput(child: Widget, index: Int) {
        audioInputByTrack[child] = index
    }

    fun setTrackEnabled(child: Widget) {
        tracksEnabled.add(child)
    }

    fun setTrackDisabled(child: Widget) {
        tracksEnabled.remove(child)
    }

    fun enableInputAtIndex(index: Int) {
        enabledMixers[index] = true
        targetDataLines[index] = recorder.startInputStreaming(allMixerInfos[index])
        dataAcquisitionThreads[index] = Thread {
            while(true) {
                val data = recorder.getLatestStreamByteArray(targetDataLines[index]!!)
                if (data != null) {
                    val count = data.size
                    for (i in 0 until count step 2) {
                        val sample: Int = Recorder.bytesToInt(data[i], data[i+1])
                        audioProcessor.addSample(sample)
                        dataStreams[index] = audioProcessor.getMeanOfLastNSamples()
                    }
                }
            }
        }
        dataAcquisitionThreads[index]!!.start()
    }

}