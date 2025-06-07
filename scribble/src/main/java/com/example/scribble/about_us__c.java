package com.example.scribble;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;

public class about_us__c {

    @FXML
    private Button back_button;

    @FXML
    private nav_bar__c mainController;

    @FXML
    private void handle_back_button(ActionEvent event) {
        System.out.println("Back button clicked");
        if (mainController != null) {
            System.out.println("Calling loadFXML for Home.fxml");
            mainController.loadFXML("Home.fxml"); // Use capital 'H'
        } else {
            System.out.println("mainController is null, cannot load Home.fxml");
        }
    }

    public void setMainController(nav_bar__c mainController) {
        this.mainController = mainController;
        System.out.println("setMainController called in about_us__c");
    }
}