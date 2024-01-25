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
    private lateinit var inputDeviceMixerInfo: Mixer.Info
    private var targetDataLine: TargetDataLine? = null

    fun findInputDevice(): Boolean {
        val mixerInfos = AudioSystem.getMixerInfo()
        if (mixerInfos.isNotEmpty()) {
            for (info in mixerInfos) {
                if (info.name.contains("Default")) {
                    inputDeviceMixerInfo = info
                    return true
                }
            }
        }
        return false
    }

    fun recordMilliSeconds(time: Int) {
        if (!findInputDevice()) return
        val targetDataLineInfo = DataLine.Info(TargetDataLine::class.java, audioFormat)
        try {
            targetDataLine = AudioSystem.getMixer(inputDeviceMixerInfo).getLine(targetDataLineInfo) as TargetDataLine
            targetDataLine!!.open()
            targetDataLine!!.start()
            val byteArray = ByteArray(1024)

            val startTime = System.currentTimeMillis()
            while (System.currentTimeMillis() - startTime < time) {
                val count = targetDataLine!!.read(byteArray, 0, byteArray.size)
                if (count > 0) {
                    // process recorded audio
                    for (i in 0 until count step 2) {
                        val twoBytes: Int = byteArray[i].toInt() + byteArray[i+1].toInt()
                        // do something with the data
                    }
                }
            }

            targetDataLine!!.stop()
            targetDataLine!!.close()
            targetDataLine = null
            println("Recording stopped")

        } catch (ex: LineUnavailableException) {
            ex.printStackTrace()
        }
    }

    fun startInputStreaming(): Boolean {
        if (!findInputDevice()) return false
        val targetDataLineInfo = DataLine.Info(TargetDataLine::class.java, audioFormat)
        targetDataLine = AudioSystem.getMixer(inputDeviceMixerInfo).getLine(targetDataLineInfo) as TargetDataLine
        targetDataLine!!.open()
        targetDataLine!!.start()
        return true
    }

    fun getLatestStreamByteArray(): ByteArray? {
        if (targetDataLine == null) {
            println("error: target data line is null")
            return null
        }
        val byteArray = ByteArray(1024)
        val count = targetDataLine!!.read(byteArray, 0, byteArray.size)
        if (count > 0) return byteArray else {
            println("error: data was not read")
            return null
        }
    }

    fun stopInputStreaming() {
        if (targetDataLine == null) return
        targetDataLine!!.stop()
        targetDataLine!!.close()
        targetDataLine = null
    }
}