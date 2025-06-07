package com.example.scribble;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class sign_up extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Load the FXML file for the sign-up page
        FXMLLoader loader = new FXMLLoader(getClass().getResource("sign_up.fxml"));
        BorderPane root = loader.load();

        // Set up the scene
        Scene scene = new Scene(root, 1400, 660);

        // Set the title and scene of the primary stage
        //primaryStage.setTitle("Sign Up");
        primaryStage.setScene(scene);

        // Show the stage
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
