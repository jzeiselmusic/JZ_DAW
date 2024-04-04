package org.jzeisel.app_test.components.interfaces.widget

import javafx.scene.layout.StackPane

interface Widget {
    fun addMeToScene(root: StackPane)
    fun removeMeFromScene(root: StackPane)
}