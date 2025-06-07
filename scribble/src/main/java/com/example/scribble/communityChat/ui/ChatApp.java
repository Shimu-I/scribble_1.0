/* package ui;

import javafx.application.Application;
import javafx.stage.Stage;

public class ChatApp extends Application {
    @Override
    public void start(Stage primaryStage) {
        int groupId = 1;  // Default group ID
        int userId = 101; // Default user ID

        // Check if command-line arguments are provided
        Parameters params = getParameters();
        if (params.getRaw().size() >= 2) {
            try {
                groupId = Integer.parseInt(params.getRaw().get(0));
                userId = Integer.parseInt(params.getRaw().get(1));
            } catch (NumberFormatException e) {
                System.out.println("Invalid arguments. Using default groupId and userId.");
            }
        }

        // Start the chat UI with groupId and userId
        MainController mainController = new MainController();
        mainController.startChatUI(primaryStage, groupId, userId);
    }

    public static void main(String[] args) {
        launch(args);
    }
}

package ui;

import javafx.application.Application;
import javafx.stage.Stage;

public class ChatApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        // Default values in case the user doesn't provide input
        int groupId = -1;
        int userId = -1;

        // Get the parameters passed via command line
        Parameters params = getParameters();
        if (params.getRaw().size() >= 2) {
            try {
                // Parse the provided arguments for groupId and userId
                groupId = Integer.parseInt(params.getRaw().get(0)); // First argument: groupId
                userId = Integer.parseInt(params.getRaw().get(1)); // Second argument: userId
            } catch (NumberFormatException e) {
                System.out.println("Invalid arguments. Please enter valid integers for groupId and userId.");
                return;
            }
        }

        // Check if groupId or userId are still -1 (meaning they were not provided)
        if (groupId == -1 || userId == -1) {
            System.out.println("Please provide valid groupId and userId.");
            return;
        }

        // Start the chat UI with the provided groupId and userId
        MainController mainController = new MainController();
        mainController.startChatUI(primaryStage, groupId, userId);
    }

    public static void main(String[] args) {
        launch(args);
    }
} */


package com.example.scribble.communityChat.ui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class ChatApp extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/chat_area.fxml"));
        BorderPane root = loader.load();
        Scene scene = new Scene(root, 600, 400);
        primaryStage.setTitle("Community Chat UI");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}



