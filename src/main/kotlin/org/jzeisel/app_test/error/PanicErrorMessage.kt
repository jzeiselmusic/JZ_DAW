package org.jzeisel.app_test.error

import javafx.scene.layout.StackPane
import org.jzeisel.app_test.components.interfaces.widget.Widget
import org.jzeisel.app_test.components.interfaces.WindowElement
import org.jzeisel.app_test.components.interfaces.widget.SingularWidget
import org.jzeisel.app_test.util.BroadcastType

class PanicErrorMessage(type: ErrorType): SingularWidget, WindowElement {
    /* this class should create a popup that alerts the user to an error
        and allows them to either close the app or ignore the error */
    override fun addMeToScene(root: StackPane) {
        TODO("Not yet implemented")
    }

    override fun removeMeFromScene(root: StackPane) {
        TODO("Not yet implemented")
    }

    override fun respondToHeightChange(old: Double, new: Double) {
        TODO("Not yet implemented")
    }

    override fun respondToWidthChange(old: Double, new: Double) {
        TODO("Not yet implemented")
    }

    override fun respondToChange(broadcastType: BroadcastType, old: Double, new: Double) {
        TODO("Not yet implemented")
    }

    override fun registerForBroadcasts() {
        TODO("Not yet implemented")
    }

    override fun unregisterForBroadcasts() {
        TODO("Not yet implemented")
    }

}