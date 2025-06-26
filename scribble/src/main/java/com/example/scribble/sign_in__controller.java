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

public class sign_in__controller {

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button signInButton;

    @FXML
    private Button signUpButton;

    @FXML
    private void handleSignUp(ActionEvent event) {
        navigateToSignUp(event);
    }

    private void navigateToSignUp(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/scribble/sign_up.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Unable to open Sign Up page.");
        }
    }

    @FXML
    private void handleSignIn(ActionEvent event) {
        String email = emailField.getText().trim();
        String password = passwordField.getText();

        if (email.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Input Error", "Please enter email and password.");
            return;
        }

        if (authenticateUser(email, password)) {
            showAlert(Alert.AlertType.INFORMATION, "Login Successful", "Welcome back!");

            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/scribble/nav_bar.fxml"));
                Parent root = loader.load();
                nav_bar__c navController = loader.getController();
                navController.loadFXML("Home.fxml");

                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.show();

                System.out.println("Sign-in successful; redirected to Home page.");
            } catch (IOException e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Navigation Error", "Unable to open home page.");
            }
        } else {
            showAlert(Alert.AlertType.ERROR, "Login Failed", "Invalid email or password.");
        }
    }

    private boolean authenticateUser(String email, String password) {
        String query = "SELECT user_id, username, email, password, profile_picture, role FROM Users WHERE email = ? AND password = ?";

        try (Connection connection = db_connect.getConnection()) {
            if (connection == null) {
                showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to connect to the database.");
                return false;
            }

            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setString(1, email);
                preparedStatement.setString(2, password); // TODO: Hash passwords in production

                ResultSet resultSet = preparedStatement.executeQuery();

                if (resultSet.next()) {
                    int userId = resultSet.getInt("user_id");
                    String username = resultSet.getString("username");
                    String role = resultSet.getString("role");
                    String profilePicture = resultSet.getString("profile_picture");
                    String userPhotoPath = (profilePicture == null || profilePicture.trim().isEmpty())
                            ? "/images/profiles/demo_profile.png"
                            : profilePicture;
                    UserSession.initialize(userId, username, role, userPhotoPath);
                    System.out.println("UserSession set: userId=" + userId + ", username=" + username + ", role=" + role + ", photoPath=" + userPhotoPath);
                    return true;
                }
            }
        } catch (SQLException e) {
            System.err.println("Database error: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to connect to the database.");
        }
        return false;
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}