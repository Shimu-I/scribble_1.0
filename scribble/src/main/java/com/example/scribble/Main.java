package com.example.scribble;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        // Clear session to start logged out
        UserSession.getInstance().clearSession();
        UserSession.getInstance().saveToFile();
        System.out.println("Application started. Session cleared: loggedIn=" + UserSession.getInstance().isLoggedIn());

        // Load nav_bar.fxml
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/scribble/nav_bar.fxml"));
        Parent root = loader.load();

        // Get nav_bar__c controller and load Home.fxml
        nav_bar__c navController = loader.getController();
        navController.loadFXML("Home.fxml");

        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Scribble");
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}