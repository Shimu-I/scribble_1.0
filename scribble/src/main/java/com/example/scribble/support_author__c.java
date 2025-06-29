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
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Logger;

public class support_author__c {

    private static final Logger LOGGER = Logger.getLogger(support_author__c.class.getName());

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

            // Navigate to dummy transaction page in a new window
            navigateToTransactionPage(quantity, amount, message);

        } catch (NumberFormatException e) {
            showAlert("Error", "Please enter a valid number for flowers.");
        } catch (Exception e) {
            LOGGER.severe("Unexpected error in handleSendButton: " + e.getMessage());
            showAlert("Error", "An unexpected error occurred: " + e.getMessage());
        }
    }

    @FXML
    private void handle_back_button(ActionEvent actionEvent) {
        if (mainController == null) {
            LOGGER.severe("Main controller is null, cannot navigate back from support author page");
            showAlert("Error", "Navigation failed: main controller is not initialized.");
            return;
        }

        try {
            // Retrieve previous FXML and bookId from AppState
            String previousFXML = AppState.getInstance().getPreviousFXML();
            int currentBookId = AppState.getInstance().getCurrentBookId();
            LOGGER.info("Navigating back to: " + previousFXML + " with bookId: " + currentBookId);

            if (previousFXML == null || previousFXML.isEmpty()) {
                previousFXML = "/com/example/scribble/read_book.fxml"; // Fallback
                LOGGER.warning("No previous FXML set in AppState, using default: " + previousFXML);
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource(previousFXML));
            Parent page = loader.load();
            Object controller = loader.getController();

            // Set bookId and mainController on the target controller
            if (controller instanceof read_book__c readBookController) {
                readBookController.setBookId(currentBookId);
                readBookController.setMainController(mainController);
            } else {
                LOGGER.warning("Unexpected controller type for " + previousFXML);
            }

            mainController.getCenterPane().getChildren().setAll(page);
            LOGGER.info("Navigated back to " + previousFXML + " with bookId: " + currentBookId);
        } catch (IOException e) {
            LOGGER.severe("Failed to navigate back to " + AppState.getInstance().getPreviousFXML() + ": " + e.getMessage());
            showAlert("Error", "Failed to navigate back: " + e.getMessage());
        }
    }

    private boolean isUserAuthor() {
        if (bookId <= 0 || !UserSession.getInstance().isLoggedIn()) {
            LOGGER.warning("Invalid bookId or user not logged in: bookId=" + bookId);
            return false;
        }
        String sql = "SELECT COUNT(*) FROM book_authors WHERE book_id = ? AND user_id = ? AND role = 'Owner'";
        try (Connection conn = db_connect.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, bookId);
            pstmt.setInt(2, UserSession.getInstance().getCurrentUserId());
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                boolean isAuthor = rs.getInt(1) > 0;
                LOGGER.info("User " + UserSession.getInstance().getCurrentUserId() + " is " + (isAuthor ? "" : "not ") + "the author of bookId=" + bookId);
                return isAuthor;
            }
        } catch (SQLException e) {
            LOGGER.severe("Error checking author status: " + e.getMessage());
        }
        return false;
    }

    public boolean saveSupportToDatabase(int quantity, double amount, String message) {
        if (!UserSession.getInstance().isLoggedIn()) {
            showAlert("Error", "You must be logged in to send support.");
            return false;
        }
        if (bookId <= 0) {
            showAlert("Error", "Invalid book selected.");
            return false;
        }
        if (isUserAuthor()) {
            showAlert("Error", "You cannot support your own book.");
            return false;
        }
        String sql = "INSERT INTO support (user_id, author_id, book_id, amount, message) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = db_connect.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, UserSession.getInstance().getCurrentUserId());
            pstmt.setInt(2, authorId);
            pstmt.setInt(3, bookId);
            pstmt.setDouble(4, amount);
            pstmt.setString(5, message.isEmpty() ? null : message);
            int rowsAffected = pstmt.executeUpdate();
            LOGGER.info("Support saved: userId=" + UserSession.getInstance().getCurrentUserId() + ", authorId=" + authorId + ", bookId=" + bookId + ", amount=" + amount);
            return rowsAffected > 0;
        } catch (SQLException e) {
            LOGGER.severe("Database error in saveSupportToDatabase: " + e.getMessage());
            showAlert("Error", "Database error: " + e.getMessage());
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

    public void clearFields() {
        flower_number.clear();
        taka_amount.setText("0.00");
        message_box.clear();
    }

    private void showAlert(String title, String content) {
        Alert.AlertType alertType = title.equals("Error") ? Alert.AlertType.ERROR : Alert.AlertType.INFORMATION;
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void navigateToTransactionPage(int quantity, double amount, String message) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/scribble/transaction_page.fxml"));
            Parent page = loader.load();
            transaction_page__c controller = loader.getController();
            controller.setSupportController(this);
            controller.setSupportDetails(quantity, amount, message);

            // Create a new modal window
            Stage transactionStage = new Stage();
            transactionStage.setTitle("Dummy Transaction");
            transactionStage.initModality(Modality.APPLICATION_MODAL); // Make the window modal
            transactionStage.setScene(new Scene(page));
            transactionStage.setResizable(false);
            transactionStage.showAndWait(); // Show and wait until the window is closed

            LOGGER.info("Displayed transaction_page.fxml in a new window");
        } catch (IOException e) {
            LOGGER.severe("Failed to open transaction page window: " + e.getMessage());
            showAlert("Error", "Failed to open transaction page: " + e.getMessage());
        }
    }

    private void updateAuthorName() {
        if (bookId <= 0) {
            author_name.setText("for Unknown Author");
            LOGGER.warning("No valid bookId provided for author name update");
            return;
        }
        authorId = getAuthorIdFromBook(bookId);
        if (authorId != -1) {
            String authorName = getAuthorName(authorId);
            author_name.setText("for " + authorName);
            LOGGER.info("Author name updated: " + authorName + " for bookId: " + bookId);
        } else {
            author_name.setText("for Unknown Author");
            showAlert("Warning", "Author not found for this book.");
            LOGGER.warning("Author not found for bookId: " + bookId);
        }
    }

    private String getAuthorName(int authorId) {
        if (authorId <= 0) {
            LOGGER.warning("Invalid authorId: " + authorId);
            return "Unknown Author";
        }
        String sql = "SELECT username FROM users WHERE user_id = ?";
        try (Connection conn = db_connect.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, authorId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                String username = rs.getString("username");
                LOGGER.info("Fetched author username: " + username + " for authorId: " + authorId);
                return username != null ? username : "Unknown Author";
            }
        } catch (SQLException e) {
            LOGGER.severe("Error fetching author username: " + e.getMessage());
        }
        return "Unknown Author";
    }

    private int getAuthorIdFromBook(int bookId) {
        if (bookId <= 0) {
            LOGGER.warning("Invalid bookId: " + bookId);
            return -1;
        }
        String sql = "SELECT user_id FROM book_authors WHERE book_id = ? AND role = 'Owner'";
        try (Connection conn = db_connect.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, bookId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                int authorId = rs.getInt("user_id");
                LOGGER.info("Fetched authorId: " + authorId + " for bookId: " + bookId);
                return authorId;
            }
        } catch (SQLException e) {
            LOGGER.severe("Error fetching author ID from book: " + e.getMessage());
        }
        return -1;
    }

    public void setUserId(int userId) {
        this.userId = userId;
        LOGGER.info("User ID set: " + userId);
    }

    public void setBookId(int bookId) {
        this.bookId = bookId;
        LOGGER.info("Book ID set: " + bookId);
        updateAuthorName();
    }

    public int getBookId() {
        return bookId;
    }
}