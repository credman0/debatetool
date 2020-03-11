module org.debatetool.debatetool.main {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;
    requires javafx.graphics;
    requires trove4j;
    requires java.desktop;
    requires org.apache.logging.log4j;
    requires com.dlsc.preferencesfx;
    requires poi.ooxml.schemas;
    requires jdk.jsobject;
    requires java.prefs;

    opens org.debatetool.gui.cardediting to javafx.fxml, javafx.graphics;
    opens org.debatetool.gui to javafx.fxml, javafx.graphics;
    opens org.debatetool.gui.locationtree to javafx.base;
    opens org.debatetool.gui.blockediting to javafx.fxml;
    opens org.debatetool.gui.speechtools to javafx.fxml;
    opens org.debatetool.gui.timer to javafx.fxml;
    exports org.debatetool.main;
}
