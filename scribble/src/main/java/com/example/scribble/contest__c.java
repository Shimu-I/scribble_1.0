package com.example.scribble;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.event.ActionEvent;

public class contest__c {

    @FXML
    private Button fantasy_button;

    @FXML
    private Button mystery_button;

    @FXML
    private Button fiction_button;

    @FXML
    private Button horror_button;

    @FXML
    private void handleFantasyButtonAction(ActionEvent event) {
        System.out.println("Fantasy genre selected!");
        // Add logic to navigate to Fantasy content or start Fantasy contest
    }

    @FXML
    private void handleMysteryButtonAction(ActionEvent event) {
        System.out.println("Thriller Mystery genre selected!");
        // Add logic to navigate to Thriller Mystery content or start Mystery contest
    }

    @FXML
    private void handleFictionButtonAction(ActionEvent event) {
        System.out.println("Youth Fiction genre selected!");
        // Add logic to navigate to Youth Fiction content or start Fiction contest
    }

    @FXML
    private void handleHorrorButtonAction(ActionEvent event) {
        System.out.println("Crime Horror genre selected!");
        // Add logic to navigate to Crime Horror content or start Horror contest
    }
}