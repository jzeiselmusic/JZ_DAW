package org.jzeisel.app_test.components.interfaces

import javafx.event.EventHandler
import javafx.scene.input.MouseEvent

interface PressableButton {
    val mousePressEvent: EventHandler<MouseEvent>
    val mouseReleaseEvent: EventHandler<MouseEvent>
}