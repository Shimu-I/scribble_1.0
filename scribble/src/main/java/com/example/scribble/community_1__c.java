package com.example.scribble;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

public class community_1__c {

    @FXML
    private Label book_name;

    @FXML
    private Label date;

    @FXML
    private Label author_name;

    @FXML
    private Label member_no;

    @FXML
    private Label user_no;

    @FXML
    private Label user;

    @FXML
    private Label u_message;

    @FXML
    private Label author;

    @FXML
    private Label a_message;

    @FXML
    private TextField type_text;

    @FXML
    private Button send_button;

    @FXML
    private Button leave_group_button;

    @FXML
    private VBox sender_vbox;

    @FXML
    private VBox receiver_vbox;

    @FXML
    private VBox text_bar;

    @FXML
    public void initialize() {
        // Allow sending messages by pressing Enter
        type_text.setOnAction(this::handle_send_button);
    }

    @FXML
    private void handle_send_button(ActionEvent event) {
        String message = type_text.getText().trim();
        if (!message.isEmpty()) {
            // Create a new message container for the sender
            VBox newMessage = new VBox(5);
            newMessage.setPrefWidth(190);
            newMessage.setPrefHeight(80);

            // Add username and message
            Label usernameLabel = new Label("user name");
            Label messageLabel = new Label(message);

            newMessage.getChildren().addAll(usernameLabel, messageLabel);
            sender_vbox.getChildren().add(newMessage);

            // Clear the text field
            type_text.clear();

            // Scroll to the bottom
            ScrollPane scrollPane = (ScrollPane) sender_vbox.getParent().getParent();
            scrollPane.setVvalue(1.0);
        }
    }

    @FXML
    private void handle_leave_group_button(ActionEvent event) {
        // Placeholder: Close the window
        leave_group_button.getScene().getWindow().hide();
    }
}