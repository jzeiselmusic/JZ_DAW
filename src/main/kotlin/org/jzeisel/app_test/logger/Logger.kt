package org.jzeisel.app_test.logger

import com.andreapivetta.kolor.*

object Logger {
    private var isDebugEnabled = true
    private var debugLevel = 5
    fun debug(tag: String, message: String, level: Int) {
        if (isDebugEnabled) {
            if (level <= debugLevel) {
                val msg = "$tag: $message"
                when (level) {
                    0 -> println(msg.red())
                    1 -> println(msg.lightRed())
                    2 -> println(msg.yellow())
                    3 -> println(msg.lightYellow())
                    4 -> println(msg.blue())
                    5 -> println(msg.lightBlue())
                }
            }
        }
    }

    fun setDebug(b: Boolean) {
        isDebugEnabled = b
    }

    fun setDebugLevel(i: Int) {
        debugLevel = if (i > 5) 5 else i
    }
}