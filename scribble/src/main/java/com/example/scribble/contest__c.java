package com.example.scribble;


import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;

public class contest__c {

    @FXML
    private Button fantasy_button;

    @FXML
    private Button thriller_mystery_button;

    @FXML
    private Button youth_fiction_button;

    @FXML
    private Button crime_horror_button;

    @FXML
    private void handle_fantasy_button(ActionEvent event) {
        System.out.println("Fantasy category selected!");
        // Add logic to handle Fantasy category selection
    }

    @FXML
    private void handle_thriller_mystery_button(ActionEvent event) {
        System.out.println("Thriller Mystery category selected!");
        // Add logic to handle Thriller Mystery category selection
    }

    @FXML
    private void handle_youth_fiction_button(ActionEvent event) {
        System.out.println("Youth Fiction category selected!");
        // Add logic to handle Youth Fiction category selection
    }

    @FXML
    private void handle_criime_horror_button(ActionEvent event) {
        System.out.println("Crime Horror category selected!");
        // Add logic to handle Crime Horror category selection
    }
}