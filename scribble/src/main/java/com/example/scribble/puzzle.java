package com.example.scribble;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

public class puzzle extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(puzzle.class.getResource("/com/example/scribble/puzzle.fxml"));
            if (fxmlLoader.getLocation() == null) {
                throw new IOException("FXML file not found at /com/example/scribble/puzzle.fxml");
            }
            Scene scene = new Scene(fxmlLoader.load(), 1000, 500);
            stage.setTitle("Puzzle Game");
            stage.setScene(scene);
            stage.setResizable(false); // Prevent window resizing
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        }
    }

    public static void main(String[] args) {
        launch();
    }
}