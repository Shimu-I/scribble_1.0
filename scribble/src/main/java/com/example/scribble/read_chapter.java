package com.example.scribble;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class read_chapter extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Load the FXML file
        BorderPane root = FXMLLoader.load(getClass().getResource("read_chapter.fxml")); // Make sure your FXML file is correctly named and located
        Scene scene = new Scene(root, 1400, 760);

        // Set up the stage (window)
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
