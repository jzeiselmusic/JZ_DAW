package org.jzeisel.app_test.audio

enum class Direction {
    INPUT, OUTPUT
}
data class Device(
    var index: Int,
    var name: String,
    var id: String,
    var direction: Direction,
    var numChannels: Int
)