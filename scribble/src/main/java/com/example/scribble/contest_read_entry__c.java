package com.example.scribble;


import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.ImageView;

public class contest_read_entry__c {

    @FXML
    private Button back_button;

    @FXML
    private Label book_tittle;

    @FXML
    private Label genre_name;

    @FXML
    private ImageView cover_photo;

    @FXML
    private TextArea writing_area;

    @FXML
    private void handle_back_button(ActionEvent event) {

    }

    public void initData(int entryId) {
    }
}
