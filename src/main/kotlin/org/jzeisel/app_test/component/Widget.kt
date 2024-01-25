package org.jzeisel.app_test.component

import javafx.scene.layout.StackPane

interface Widget {
    /* open class for all components */
    /* every widget will have a single parent and a list of children */
    val children: MutableList<Widget>
    val parent: Widget?

    fun addChild(child: Widget)

    fun addMeToScene(root: StackPane)

    fun removeMeFromScene(root: StackPane)
}