package com.example.scribble.communityChat.ui;/*package ui;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainController {
    public void startChatUI(Stage primaryStage, int groupId, int userId) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/chat_area.fxml"));
            Parent root = loader.load();

            // Get ChatAreaController and pass groupId, userId
            ChatAreaController chatController = loader.getController();
            chatController.initialize(groupId, userId); // ✅ Now this method exists

            primaryStage.setScene(new Scene(root, 600, 400));
            primaryStage.setTitle("Community Chat");
            primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
} */
