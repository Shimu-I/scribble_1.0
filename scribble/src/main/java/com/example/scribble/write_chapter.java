package com.example.scribble;


import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.util.Objects;

public class write_chapter extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Load the FXML file
        BorderPane root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("write_chapter.fxml"))); // Adjust the file name if necessary
        Scene scene = new Scene(root, 1400, 660);

        // Set up the stage (window)

        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}