package com.example.scribble;


import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class contest_entries__c {

    @FXML
    private Button back_button;

    @FXML
    private Button add_entry;

    @FXML
    private VBox entryContainer;

    @FXML
    private Label entry_no;

    @FXML
    private Label title_of_the_content;

    @FXML
    private Label author_name;

    @FXML
    private Label submited_date;

    @FXML
    private Button not_voted_button;

    @FXML
    private Button voted_button;

    @FXML
    private Label voted_by_no_of_people;

    @FXML
    private Button open_entry;

    @FXML
    private void handle_back_button(ActionEvent event) {
        System.out.println("Back button clicked!");
        // Add logic to navigate back to the previous screen
    }

    @FXML
    private void handle_add_entry(ActionEvent event) {
        System.out.println("Add entry button clicked!");
        // Add logic to create a new entry
    }

    @FXML
    private void handle_not_voted_button(ActionEvent event) {
        System.out.println("Not voted button clicked for entry!");
        // Add logic to handle upvoting an entry
    }

    @FXML
    private void handle_voted_button(ActionEvent event) {
        System.out.println("Voted button clicked for entry!");
        // Add logic to handle removing a vote or showing voted state
    }

    @FXML
    private void handle_open_entry(ActionEvent event) {
        System.out.println("Open entry button clicked!");
        // Add logic to open the selected entry
    }
}