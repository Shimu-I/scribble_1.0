/*package ui;

import dao.ChatMessageDAO;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import models.ChatMessage;

import java.util.List;

public class ChatAreaController {
    @FXML private VBox chatMessagesContainer;
    @FXML private TextField messageInput;
    @FXML private Button sendButton;

    private int groupId; // Group ID for filtering messages
    private int userId;  // Current user's ID

    public void initialize(int groupId, int userId) {
        this.groupId = groupId;
        this.userId = userId;
        loadChatMessages();
    }

    private void loadChatMessages() {
        chatMessagesContainer.getChildren().clear();
        List<ChatMessage> messages = ChatMessageDAO.getMessages(groupId);

        for (ChatMessage message : messages) {
            displayMessage(message);
        }
    }

    private void displayMessage(ChatMessage message) {
        Text text = new Text(message.getMessage());
        TextFlow textFlow = new TextFlow(text);

        if (message.getSenderId() == userId) {
            textFlow.setStyle("-fx-background-color: lightblue; -fx-padding: 10px; -fx-background-radius: 10px;");
        } else {
            textFlow.setStyle("-fx-background-color: lightgray; -fx-padding: 10px; -fx-background-radius: 10px;");
        }

        chatMessagesContainer.getChildren().add(textFlow);
    }

    @FXML
    private void sendMessage() {
        String text = messageInput.getText().trim();
        if (!text.isEmpty()) {
            ChatMessageDAO.addMessage(groupId, userId, text);
            loadChatMessages(); // Reload chat to display new message
            messageInput.clear();
        }
    }
}
package ui;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.io.*;
import java.net.*;

public class ChatAreaController {
    @FXML private TextField groupIdField;
    @FXML private TextField userIdField;
    @FXML private ListView<String> chatListView;
    @FXML private TextField messageField;

    private PrintWriter out;
    private BufferedReader in;
    private Socket socket;

    public void joinGroup() {
        try {
            int groupId = Integer.parseInt(groupIdField.getText());
            int senderId = Integer.parseInt(userIdField.getText());

            socket = new Socket("localhost", 12345);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // Send groupId,senderId as the first message
            out.println(groupId + "," + senderId);

            // Start a thread to listen for messages
            new Thread(this::receiveMessages).start();

            showMessage("✅ Joined Group " + groupId + " as User " + senderId);

        } catch (Exception e) {
            showMessage("❌ Error joining group: " + e.getMessage());
        }
    }

    public void sendMessage() {
        if (out == null) {
            showMessage("⚠️ Please join a group first!");
            return;
        }

        String message = messageField.getText();
        if (!message.isEmpty()) {
            out.println(message);
            messageField.clear();
        }
    }

    private void receiveMessages() {
        try {
            String message;
            while ((message = in.readLine()) != null) {
                showMessage(message);
            }
        } catch (IOException e) {
            showMessage("❌ Disconnected from server.");
        }
    }

    private void showMessage(String message) {
        chatListView.getItems().add(message);
    }
}




 */


package com.example.scribble.communityChat.ui;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ChatAreaController {

    @FXML private TextField groupIdField;
    @FXML private TextField userIdField;
    @FXML private TextField messageField;
    @FXML private ListView<String> chatList;

    private PrintWriter out;
    private BufferedReader in;
    private int groupId;
    private int userId;

    public void connectToServer() {
        try {
            Socket socket = new Socket("localhost", 12345);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            groupId = Integer.parseInt(groupIdField.getText().trim());
            userId = Integer.parseInt(userIdField.getText().trim());

            out.println(groupId + "," + userId); // handshake to server

            // Start listening thread
            new Thread(() -> {
                try {
                    String message;
                    while ((message = in.readLine()) != null) {
                        String displayMsg = "New message: " + message;
                        Platform.runLater(() -> chatList.getItems().add(displayMsg));
                    }
                } catch (IOException e) {
                    Platform.runLater(() -> chatList.getItems().add("Disconnected from server."));
                }
            }).start();

            chatList.getItems().add("Connected to server as User ID " + userId);

        } catch (IOException e) {
            chatList.getItems().add("Error connecting: " + e.getMessage());
        }
    }

    public void sendMessage() {
        String text = messageField.getText().trim();
        if (!text.isEmpty() && out != null) {
            out.println(text); // Send to server

            // Create ChatMessage using the constructor with parameters
            //Timestamp timestamp = Timestamp.valueOf(LocalDateTime.now());
            //ChatMessage msg = new ChatMessage(0, groupId, userId, text, timestamp); // Use constructor with 5 parameters

            // Use the DAO's addMessage() method to save to the DB
            //ChatMessageDAO.addMessage(groupId, userId, text); // Save the message

            // Optionally, update your chat UI
            //chatList.getItems().add("Me: " + text);
            messageField.clear();
        }
    }
}



