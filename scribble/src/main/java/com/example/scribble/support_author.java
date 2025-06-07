package com.example.scribble;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import java.io.IOException;

public class support_author extends Application {

    private static int currentBookId; // Store the bookId from the reading page

    @Override
    public void start(Stage primaryStage) throws IOException {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("support_author.fxml"));
            BorderPane root = loader.load();

            // Get the controller and set bookId and userId
            support_author__c controller = loader.getController();
            controller.setBookId(currentBookId); // Set the bookId from the reading page
            controller.setUserId(5);            // Logged-in user, e.g., from session

            Scene scene = new Scene(root, 1400, 760);

            primaryStage.setResizable(false);
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (IOException e) {
            System.err.println("Error loading FXML: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    // Method to launch the support page with a specific bookId
    public static void launchSupportPage(int bookId) {
        currentBookId = bookId;
        launch();
    }

    public static void main(String[] args) {
        launch(args);
    }
}