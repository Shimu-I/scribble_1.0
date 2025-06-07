package com.example.scribble;


import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.ImageView;

public class contest_write__c {

    @FXML
    private Button back_button;

    @FXML
    private Button upload_button;

    @FXML
    private Label book_tittle;

    @FXML
    private Label genre_name;

    @FXML
    private ImageView cover_photo;

    @FXML
    private Button cover_photo_button;

    @FXML
    private TextArea writing_area;

    @FXML
    private void handle_back_button(ActionEvent event) {
        System.out.println("Back button clicked!");
        // Add logic to navigate back to the previous screen
    }

    @FXML
    private void handle_upload_button(ActionEvent event) {
        System.out.println("Upload button clicked!");
        // Add logic to upload the entry (e.g., save book title, genre, cover photo, and writing content)
    }

    @FXML
    private void handle_cover_photo_button(ActionEvent event) {
        System.out.println("Cover photo button clicked!");
        // Add logic to open a file chooser and set a new cover photo
    }
}
