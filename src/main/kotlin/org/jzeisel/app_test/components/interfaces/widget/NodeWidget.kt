package org.jzeisel.app_test.components.interfaces.widget

interface NodeWidget: Widget {
    val children: MutableList<Widget>
    val parent: Widget?
    fun addChild(child: Widget)
}