package com.example.scribble;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.io.IOException;

public class contest_write extends Application {

    @Override
    public void start(Stage primaryStage) throws IOException {
        // Load the FXML file (assuming contest.fxml as per earlier context)
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/scribble/contest_write.fxml"));
        BorderPane root = loader.load();

        // Set up the scene with dimensions matching the FXML (1400x660)
        Scene scene = new Scene(root, 1400, 660);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Pen Wars - Contest Page");
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}