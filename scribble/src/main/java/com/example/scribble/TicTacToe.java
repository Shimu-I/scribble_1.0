package com.example.scribble;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

public class TicTacToe extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(TicTacToe.class.getResource("/com/example/scribble/TicTacToe.fxml"));
            if (fxmlLoader.getLocation() == null) {
                throw new IOException("FXML file not found at /com/example/scribble/TicTacToe.fxml");
            }
            Scene scene = new Scene(fxmlLoader.load(), 444, 542); // Set exact size to 600x400 pixels
            stage.setTitle("TicTacToe");
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