module com.example.scribble {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires java.logging;
    requires mysql.connector.j;
    requires java.desktop;

    // Allow JavaFX to access these packages via reflection
    opens com.example.scribble to javafx.graphics, javafx.fxml;
    opens com.example.scribble.communityChat.ui to javafx.fxml; // Added for ChatAreaController

    // Allow other modules to use these public classes
    exports com.example.scribble;
    exports com.example.scribble.communityChat.chat;
}