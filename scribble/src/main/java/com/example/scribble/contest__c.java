package com.example.scribble;


import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class contest__c {

    @FXML
    private Button fantasy_button;

    @FXML
    private Button thriller_mystery_button;

    @FXML
    private Button youth_fiction_button;

    @FXML
    private Button crime_horror_button;

    // Handle Fantasy button click
    @FXML
    private void handle_fantasy_button(ActionEvent event) {
        navigateToContestWrite("Fantasy");
    }

    // Handle Thriller Mystery button click
    @FXML
    private void handle_thriller_mystery_button(ActionEvent event) {
        navigateToContestWrite("Thriller Mystery");
    }

    // Handle Youth Fiction button click
    @FXML
    private void handle_youth_fiction_button(ActionEvent event) {
        navigateToContestWrite("Youth Fiction");
    }

    // Handle Crime Horror button click
    @FXML
    private void handle_crime_horror_button(ActionEvent event) {
        navigateToContestWrite("Crime Horror");
    }

    // Retrieve contest_id for a given genre from the contests table
    private int getContestIdForGenre(String genre) {
        try (Connection conn = db_connect.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT contest_id FROM contests WHERE genre = ?")) {
            stmt.setString(1, genre);
            System.out.println("Executing query for genre: " + genre);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                int contestId = rs.getInt("contest_id");
                System.out.println("Found contestId: " + contestId);
                return contestId;
            } else {
                System.out.println("No contest found for genre: " + genre);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showErrorAlert("Database Error", "Failed to retrieve contest information.");
        }
        return -1; // Invalid genre or database error
    }

    // Helper method to navigate to contest_write.fxml
    private void navigateToContestWrite(String genre) {
        // Check if user is logged in
        UserSession session = UserSession.getInstance();
        if (!session.isLoggedIn()) {
            showErrorAlert("Session Error", "You must be logged in to participate in a contest.");
            return;
        }

        // Validate genre and get contest_id
        int contestId = getContestIdForGenre(genre);
        if (contestId == -1) {
            showErrorAlert("Contest Not Found", "The " + genre + " contest is not available.");
            return;
        }

        try {
            // Get user session details
            int userId = session.getUserId();
            String username = session.getUsername();
            String userPhotoPath = session.getUserPhotoPath();

            // Load contest_write.fxml
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/scribble/contest_write.fxml"));
            Parent root = loader.load();

            // Get the controller for contest_write.fxml
            contest_write__c writeController = loader.getController();

            // Pass contest and user details to the next controller
            writeController.initData(contestId, genre, userId, username, userPhotoPath);

            // Set up the new scene
            Stage stage = (Stage) fantasy_button.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Write for " + genre + " Contest");
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Navigation Error", "Failed to load the contest writing page.");
        }
    }

    // Helper method to show error alerts
    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}