module org.jzeisel.app_test {
    requires javafx.controls;
    requires javafx.fxml;
    requires kotlin.stdlib;

    requires org.controlsfx.controls;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires java.desktop;
    requires kolor;

    opens org.jzeisel.app_test to javafx.fxml;
    exports org.jzeisel.app_test;
}