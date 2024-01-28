package org.jzeisel.app_test.audio

import javax.sound.sampled.*


class Recorder {
    companion object {
        private val audioFormat = AudioFormat(
                AudioFormat.Encoding.PCM_SIGNED,
                44100.0f, 16, 1, 2, 44100.0f, false
        )

        fun bytesToInt(byteA: Byte, byteB: Byte): Int {
            return (byteB.toInt().shl(8)) or byteA.toInt()
        }
    }
    private var targetDataLine: TargetDataLine? = null

    fun findDefaultInputDevice(): Mixer.Info? {
        val mixerInfos = AudioSystem.getMixerInfo()
        if (mixerInfos.isNotEmpty()) {
            for (info in mixerInfos) {
                if (info.name.contains("Default")) {
                    return info
                }
            }
        }
        return null
    }

    fun findAllInputDevices(): Array<Mixer.Info> {
        val mixerInfos = AudioSystem.getMixerInfo()
        if (mixerInfos.isNotEmpty()) {
            return mixerInfos
        }
        else {
            return arrayOf()
        }
    }

    fun startInputStreaming(mixer: Mixer.Info): TargetDataLine? {
        val targetDataLineInfo = DataLine.Info(TargetDataLine::class.java, audioFormat)
        targetDataLine = AudioSystem.getMixer(mixer).getLine(targetDataLineInfo) as TargetDataLine
        targetDataLine!!.open()
        targetDataLine!!.start()
        return targetDataLine
    }

    fun stopInputStreaming(target: TargetDataLine) {
        target.stop()
        target.close()
    }

    fun getLatestStreamByteArray(target: TargetDataLine): ByteArray? {
        val byteArray = ByteArray(1024)
        val count = target.read(byteArray, 0, byteArray.size)
        if (count > 0) return byteArray else {
            return null
        }
    }
}