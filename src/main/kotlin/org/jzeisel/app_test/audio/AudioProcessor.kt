package org.jzeisel.app_test.audio

import kotlin.math.abs
import kotlin.math.sqrt

class AudioProcessor(val N: Int) {
    private val lastNSamples: MutableList<Int> = mutableListOf()

    private fun MutableList<Int>.addWithLimit(sample: Int, limit: Int) {
        if (this.size < limit) {
            this.add(sample)
        }
        else {
            this.add(sample)
            this.removeAt(0)
        }
    }

    fun addSample(sample: Int) {
        lastNSamples.addWithLimit(sample, N)
    }

    fun getMeanOfLastNSamples(): Double {
        var sum = 0.0
        for (element in lastNSamples) {
            sum += abs(element)*abs(element)
        }
        return sqrt(sum / lastNSamples.size)
    }
}