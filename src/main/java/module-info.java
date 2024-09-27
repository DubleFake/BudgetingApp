module org.dfproductions.budgeting {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    //requires eu.hansolo.tilesfx;

    opens org.dfproductions.budgeting to javafx.fxml;
    exports org.dfproductions.budgeting;
    exports org.dfproductions.budgeting.controllers;
    opens org.dfproductions.budgeting.controllers to javafx.fxml;
}