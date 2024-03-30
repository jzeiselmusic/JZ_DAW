module org.jzeisel.app_test {
    requires javafx.controls;
    requires javafx.fxml;
    requires kotlin.stdlib;

    requires org.controlsfx.controls;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires java.desktop;
    requires kolor;
    requires kotlinx.coroutines.core.jvm;
    requires com.sun.jna;
    requires kotlinx.serialization.json.jvm;
    requires kotlinx.serialization.core.jvm;

    opens org.jzeisel.app_test to javafx.fxml;
    exports org.jzeisel.app_test;
}