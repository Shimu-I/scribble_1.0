package com.example.scribble;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import java.io.IOException;

public class sign_in extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            // Load the FXML file for the sign-in page
            FXMLLoader loader = new FXMLLoader(getClass().getResource("sign_in.fxml"));
            BorderPane root = loader.load();

            // Set up the scene
            Scene scene = new Scene(root, 1400, 660);

            // Set the title and scene of the primary stage
            //primaryStage.setTitle("Sign In");
            primaryStage.setScene(scene);

            // Show the stage
            primaryStage.show();
        } catch (IOException e) {
            // Handle error loading the FXML file
            System.out.println("Error loading FXML: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
