package org.jzeisel.app_test.error

import javafx.scene.layout.StackPane
import org.jzeisel.app_test.components.interfaces.Widget

class PanicErrorMessage(type: ErrorType): Widget {
    /* this class should create a popup that alerts the user to an error
        and allows them to either close the app or ignore the error */
    override val children: MutableList<Widget> = mutableListOf()
    override val parent: Widget? = null
    override fun addChild(child: Widget) {
    }

    override fun addMeToScene(root: StackPane) {
        TODO("Not yet implemented")
    }

    override fun removeMeFromScene(root: StackPane) {
        TODO("Not yet implemented")
    }

}