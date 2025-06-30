package com.example.scribble;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.util.Random;
import java.util.logging.Logger;

public class transaction_page__c {

    private static final Logger LOGGER = Logger.getLogger(transaction_page__c.class.getName());

    @FXML
    private TextField account_number;
    @FXML
    private TextField pin;
    @FXML
    private Button transaction_send_button;
    @FXML
    private Button cancel_button;

    private support_author__c supportController;
    private int quantity;
    private double amount;
    private String message;

    public void setSupportController(support_author__c supportController) {
        this.supportController = supportController;
    }

    public void setSupportDetails(int quantity, double amount, String message) {
        this.quantity = quantity;
        this.amount = amount;
        this.message = message;
    }

    @FXML
    private void handleTransactionSend(ActionEvent event) {
        String accountNumber = account_number.getText().trim();
        String pinText = pin.getText().trim();

        if (accountNumber.isEmpty() || pinText.isEmpty()) {
            showAlert("Error", "Please enter both account number and PIN.");
            return;
        }

        // Print account number and PIN to terminal (dummy transaction)
        System.out.println("Transaction Confirmation:");
        System.out.println("Account Number: " + accountNumber);
        System.out.println("PIN: " + pinText);

        // Generate a random 10-character transaction ID
        String transactionId = generateTransactionId();

        // Call the support controller's method to save to database
        try {
            if (supportController.saveSupportToDatabase(quantity, amount, message)) {
                // Show success alert with transaction ID
                showAlert("Success", "Transaction confirmed and support sent successfully!\nTransaction ID: " + transactionId);
                supportController.clearFields();
                // Close the transaction window
                Stage stage = (Stage) transaction_send_button.getScene().getWindow();
                stage.close();
            } else {
                showAlert("Error", "Failed to confirm transaction and send support. Please try again.");
            }
        } catch (Exception e) {
            LOGGER.severe("Error processing transaction confirmation: " + e.getMessage());
            showAlert("Error", "An unexpected error occurred during transaction confirmation: " + e.getMessage());
        }
    }

    @FXML
    private void handleCancel(ActionEvent event) {
        // Close the transaction window
        Stage stage = (Stage) cancel_button.getScene().getWindow();
        stage.close();
    }

    private String generateTransactionId() {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        Random random = new Random();
        StringBuilder transactionId = new StringBuilder();
        for (int i = 0; i < 10; i++) {
            transactionId.append(characters.charAt(random.nextInt(characters.length())));
        }
        return transactionId.toString();
    }

    private void showAlert(String title, String content) {
        Alert.AlertType alertType = title.equals("Error") ? Alert.AlertType.ERROR : Alert.AlertType.INFORMATION;
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}