package com.example.scribble.communityChat.ui;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class ChatMessageCell extends ListCell<String> {

    private HBox content;
    private TextFlow textFlow;
    private Label bubble;

    public ChatMessageCell() {
        textFlow = new TextFlow();
        bubble = new Label();
        bubble.setWrapText(true);
        bubble.setPadding(new Insets(14)); // Increased padding
        bubble.setMaxWidth(500); // Wider bubble
        bubble.setStyle("-fx-background-color: #C8E6C9; -fx-background-radius: 15;");

        content = new HBox();
        content.setSpacing(10);
        content.getChildren().add(bubble);
    }

    @Override
    protected void updateItem(String message, boolean empty) {
        super.updateItem(message, empty);

        if (empty || message == null) {
            setGraphic(null);
        } else {
            // Clear previous text
            textFlow.getChildren().clear();

            // Parse message: expected format "[username] message (time)"
            String username = "";
            String messageContent = message;
            String time = "";

            // Extract username (in brackets)
            if (message.startsWith("[")) {
                int endBracket = message.indexOf("]");
                if (endBracket != -1) {
                    username = message.substring(0, endBracket + 1); // e.g., "[me]"
                    messageContent = message.substring(endBracket + 1).trim();
                }
            }

            // Extract time (in parentheses)
            if (messageContent.contains("(") && messageContent.endsWith(")")) {
                int startParen = messageContent.lastIndexOf("(");
                time = messageContent.substring(startParen); // e.g., "(12:34)"
                messageContent = messageContent.substring(0, startParen).trim();
            }

            // Create styled Text nodes
            Text usernameText = new Text(username);
            usernameText.setFont(Font.font("Arial", FontWeight.BOLD, 12));
            usernameText.setFill(javafx.scene.paint.Color.GRAY);

            Text messageText = new Text(messageContent + " ");
            messageText.setFont(Font.font("Arial", FontWeight.NORMAL, 16));
            messageText.setFill(javafx.scene.paint.Color.BLACK);

            Text timeText = new Text(time);
            timeText.setFont(Font.font("Arial", FontWeight.NORMAL, 12));
            timeText.setFill(javafx.scene.paint.Color.GRAY);

            // Add Text nodes to TextFlow
            textFlow.getChildren().addAll(usernameText, messageText, timeText);

            // Set TextFlow as the bubble's graphic
            bubble.setGraphic(textFlow);
            bubble.setStyle(message.startsWith("[me]") ?
                    "-fx-background-color: #C8E6C9; -fx-background-radius: 15;" :
                    "-fx-background-color: #E1F5FE; -fx-background-radius: 15;");

            // Align left/right based on sender
            content.setStyle(message.startsWith("[me]") ?
                    "-fx-alignment: CENTER_LEFT;" :
                    "-fx-alignment: CENTER_RIGHT;");

            setGraphic(content);
        }
    }
}