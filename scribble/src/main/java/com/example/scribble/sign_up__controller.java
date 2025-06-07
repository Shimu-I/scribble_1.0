package com.example.scribble;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class sign_up__controller {

    @FXML
    private TextField nameField;

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button signUpButton;

    @FXML
    private Button signInButton;

    // Handle Sign-Up Button Click
    @FXML
    private void handleSignUp(ActionEvent event) {
        String name = nameField.getText().trim();
        String email = emailField.getText().trim();
        String password = passwordField.getText();

        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Input Error", "All fields are required.");
            return;
        }

        int userId = registerUser(name, email, password);
        if (userId != -1) {
            // Store user data in UserSession with the default photo path
            String userPhotoPath = "/images/profiles/demo_profile.png";
            UserSession.getInstance().setUser(userId, name, userPhotoPath);
            System.out.println("UserSession set: userId=" + userId + ", username=" + name + ", photoPath=" + userPhotoPath);
            showAlert(Alert.AlertType.INFORMATION, "Success", "Account created successfully!");

            // Navigate to home page (nav_bar.fxml)
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("nav_bar.fxml"));
                Parent root = loader.load();
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.show();
            } catch (IOException e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Navigation Error", "Unable to open home page.");
            }
        } else {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to register. Try again.");
        }
    }

    // Handle Sign-In Button Click (Navigates to Sign In Page)
    @FXML
    private void handleSignIn(ActionEvent event) {
        navigateToSignIn(event);
    }

    // Method to insert user data into the database and return user_id
    private int registerUser(String name, String email, String password) {
        String query = "INSERT INTO users (username, email, password, profile_picture) VALUES (?, ?, ?, ?)";

        try (Connection connection = db_connect.getConnection()) {
            if (connection == null) {
                showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to connect to the database.");
                return -1;
            }

            try (PreparedStatement preparedStatement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
                preparedStatement.setString(1, name);
                preparedStatement.setString(2, email);
                preparedStatement.setString(3, password); // Plain-text password (insecure for production)
                preparedStatement.setString(4, "/images/profiles/demo_profile.png"); // Default profile picture

                int rowsInserted = preparedStatement.executeUpdate();
                if (rowsInserted > 0) {
                    // Retrieve generated user_id
                    ResultSet generatedKeys = preparedStatement.getGeneratedKeys();
                    if (generatedKeys.next()) {
                        return generatedKeys.getInt(1); // user_id as int
                    }
                }
                return -1;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Error connecting to the database.");
            return -1;
        }
    }

    // Method to navigate to Sign In page
    private void navigateToSignIn(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("sign_in.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Unable to open Sign In page.");
        }
    }

    // Method to show alerts
    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}