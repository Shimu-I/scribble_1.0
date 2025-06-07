package com.example.scribble;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class support_author__c {

    @FXML
    public Label author_name;       // Matches fx:id="author_name"
    @FXML
    private TextField flower_number; // Matches fx:id="flower_number"
    @FXML
    private TextField taka_amount;   // Matches fx:id="taka_amount"
    @FXML
    private TextArea message_box;    // Matches fx:id="message_box"
    @FXML
    private Button send_button;      // Matches fx:id="send_button"
    @FXML
    private Button back_button;      // Matches fx:id="back_button"

    @FXML
    private nav_bar__c mainController;

    private int userId;             // Logged-in user's ID
    private int authorId = -1;      // Author's user ID, derived from bookId
    private int bookId;             // Book ID
    private static final double PRICE_PER_FLOWER = 10.0;

    @FXML
    private void initialize() {
        // Make taka_amount read-only
        taka_amount.setEditable(false);

        // Update taka_amount when flower_number changes
        flower_number.textProperty().addListener((observable, oldValue, newValue) -> {
            calculateTotal(newValue);
        });

        // Set initial value for taka_amount
        taka_amount.setText("0.00");

        // Update author name if bookId is set
        updateAuthorName();
    }

    public void setMainController(nav_bar__c mainController) {
        this.mainController = mainController;
    }

    @FXML
    public void handle_support_author() {
        // This method replaces the original handle_support_author
        handleSendButton();
    }

    @FXML
    public void handleSendButton() {
        try {
            if (!validateInputs()) {
                showAlert("Error", "Please fill all required fields correctly.");
                return;
            }

            if (authorId <= 0) {
                showAlert("Error", "Author not identified. Please select a valid book.");
                return;
            }

            int quantity = Integer.parseInt(flower_number.getText());
            double amount = Double.parseDouble(taka_amount.getText());
            String message = message_box.getText().trim();

            if (saveSupportToDatabase(quantity, amount, message)) {
                showAlert("Success", "Support sent successfully!");
                clearFields();
            } else {
                showAlert("Error", "Failed to send support. Please try again.");
            }

        } catch (NumberFormatException e) {
            showAlert("Error", "Please enter a valid number for flowers.");
        } catch (Exception e) {
            showAlert("Error", "An unexpected error occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    public void handle_back_button(ActionEvent actionEvent) {
        if (mainController != null) {
            mainController.loadFXML("read_book.fxml");
        } else {
            // Fallback: Load read_book.fxml directly
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("read_book.fxml"));
                Parent root = loader.load();
                Scene scene = back_button.getScene();
                scene.setRoot(root);
            } catch (IOException e) {
                e.printStackTrace();
                showAlert("Error", "Failed to go back: " + e.getMessage());
            }
        }
    }

    private boolean saveSupportToDatabase(int quantity, double amount, String message) {
        if (userId == authorId) {
            showAlert("Error", "You cannot support yourself.");
            return false;
        }
        String sql = "INSERT INTO support (user_id, author_id, amount, message) VALUES (?, ?, ?, ?)";
        try (Connection conn = db_connect.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setInt(2, authorId);
            pstmt.setDouble(3, amount);
            pstmt.setString(4, message.isEmpty() ? null : message);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            showAlert("Error", "Database error: " + e.getMessage());
            System.err.println("Database error: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private void calculateTotal(String numberInput) {
        try {
            if (numberInput == null || numberInput.trim().isEmpty()) {
                taka_amount.setText("0.00");
                return;
            }

            int quantity = Integer.parseInt(numberInput);
            if (quantity < 0) {
                taka_amount.setText("0.00");
                showAlert("Warning", "Number of flowers cannot be negative.");
                flower_number.setText("0");
                return;
            }

            double total = calculateSupportAmount(quantity);
            taka_amount.setText(String.format("%.2f", total));

        } catch (NumberFormatException e) {
            taka_amount.setText("0.00");
            showAlert("Error", "Please enter a valid number.");
        }
    }

    private double calculateSupportAmount(int flowerCount) {
        return flowerCount * PRICE_PER_FLOWER;
    }

    private boolean validateInputs() {
        String numberText = flower_number.getText();

        if (numberText.isEmpty()) {
            showAlert("Error", "Please enter the number of flowers.");
            return false;
        }

        try {
            int quantity = Integer.parseInt(numberText);
            if (quantity <= 0) {
                showAlert("Error", "Number of flowers must be greater than zero.");
                return false;
            }
            return true;
        } catch (NumberFormatException e) {
            showAlert("Error", "Please enter a valid number for flowers.");
            return false;
        }
    }

    private void clearFields() {
        flower_number.clear();
        taka_amount.setText("0.00");
        message_box.clear();
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void updateAuthorName() {
        if (bookId <= 0) {
            author_name.setText("for Unknown Author");
            return;
        }
        authorId = getAuthorIdFromBook(bookId);
        if (authorId != -1) {
            String authorName = getAuthorName(authorId);
            author_name.setText("for " + authorName);
        } else {
            author_name.setText("for Unknown Author");
            showAlert("Warning", "Author not found for this book.");
        }
    }

    private String getAuthorName(int authorId) {
        if (authorId <= 0) {
            return "Unknown Author";
        }
        String sql = "SELECT username FROM users WHERE user_id = ?";
        try (Connection conn = db_connect.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, authorId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                String username = rs.getString("username");
                return username != null ? username : "Unknown Author";
            }
        } catch (SQLException e) {
            System.err.println("Error fetching author username: " + e.getMessage());
            e.printStackTrace();
        }
        return "Unknown Author";
    }

    private int getAuthorIdFromBook(int bookId) {
        if (bookId <= 0) {
            return -1;
        }
        String sql = "SELECT user_id FROM book_authors WHERE book_id = ? AND role = 'Owner'";
        try (Connection conn = db_connect.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, bookId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("user_id");
            }
        } catch (SQLException e) {
            System.err.println("Error fetching author ID from book: " + e.getMessage());
            e.printStackTrace();
        }
        return -1;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public void setBookId(int bookId) {
        this.bookId = bookId;
        updateAuthorName();
    }
}