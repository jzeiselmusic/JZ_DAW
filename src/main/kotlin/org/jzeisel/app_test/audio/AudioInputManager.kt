package org.jzeisel.app_test.audio

import org.jzeisel.app_test.components.Widget
import org.jzeisel.app_test.components.NormalTrack
import org.jzeisel.app_test.viewmodel.TrackListViewModel
import org.jzeisel.app_test.util.Logger
import javax.sound.sampled.Mixer
import javax.sound.sampled.TargetDataLine

class AudioInputManager(parent: TrackListViewModel) {
    companion object {
        const val TAG = "AudioInputManager"
        const val LVL = 4
    }
    private val recorder = Recorder()
    private val audioProcessor = AudioProcessor(50)

    val trackListViewModel = parent
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

    fun setTrackEnabled(child: Widget): Boolean {
        tracksEnabled.add(child)
        /* enable the mixer and data line that this track (child) has chosen */
        audioInputByTrack[child]?.let{
            enableInputAtIndex(it)
            return true
        }
        return false
    }

    fun setTrackDisabled(child: Widget) {
        tracksEnabled.remove(child)
    }

    private fun enableInputAtIndex(index: Int) {
        if (!enabledMixers[index]) {
            enabledMixers[index] = true
            targetDataLines[index] = recorder.startInputStreaming(allMixerInfos[index])
            dataAcquisitionThreads[index] = Thread {
                Logger.debug(TAG, "starting thread for $index", LVL)
                while (true) {
                    val data = recorder.getLatestStreamByteArray(targetDataLines[index]!!)
                    if (data != null) {
                        val count = data.size
                        // Logger.debug(TAG, "n data: $count", LVL)
                        if (count % 2 == 0) {
                            for (i in 0 until count step 2) {
                                val sample: Int = Recorder.bytesToInt(data[i], data[i + 1])
                                audioProcessor.addSample(sample)
                                dataStreams[index] = audioProcessor.getMeanOfLastNSamples()
                            }
                            for (track in trackListViewModel.children) {
                                val t = track as NormalTrack
                                if (t.audioInputEnabled && t.audioInputIndex == index) {
                                    t.vuMeter.setBarsBasedOnAudio(this, index)
                                }
                            }
                        }
                    }
                    else {
                        Logger.debug(TAG, "data is null", LVL)
                    }
                }
            }
            dataAcquisitionThreads[index]!!.start()
        }
    }

}