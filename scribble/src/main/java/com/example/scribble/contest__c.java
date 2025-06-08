package com.example.scribble;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceDialog;
import javafx.stage.Stage;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;

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
        handleGenreSelection("Fantasy");
    }

    @FXML
    private void handle_thriller_mystery_button(ActionEvent event) {
        handleGenreSelection("Thriller Mystery");
    }

    @FXML
    private void handle_youth_fiction_button(ActionEvent event) {
        handleGenreSelection("Youth Fiction");
    }

    @FXML
    private void handle_crime_horror_button(ActionEvent event) {
        handleGenreSelection("Crime Horror");
    }

    private void handleGenreSelection(String genre) {
        UserSession session = UserSession.getInstance();
        if (!session.isLoggedIn()) {
            showErrorAlert("Session Error", "You must be logged in to participate in a contest.");
            return;
        }

        int contestId = getContestIdForGenre(genre);
        if (contestId == -1) {
            showErrorAlert("Contest Not Found", "The " + genre + " contest is not available.");
            return;
        }

        ChoiceDialog<String> dialog = new ChoiceDialog<>("Add Contest Entry", Arrays.asList("Add Contest Entry", "View Contest Entries"));
        dialog.setTitle("Contest Action");
        dialog.setHeaderText("What would you like to do for the " + genre + " contest?");
        dialog.setContentText("Choose an action:");
        dialog.showAndWait().ifPresent(choice -> {
            if (choice.equals("Add Contest Entry")) {
                navigateToContestWrite(contestId, genre);
            } else {
                navigateToContestEntries(contestId, genre);
            }
        });
    }

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
        return -1;
    }

    private void navigateToContestWrite(int contestId, String genre) {
        try {
            UserSession session = UserSession.getInstance();
            int userId = session.getUserId();
            String username = session.getUsername();
            String userPhotoPath = session.getUserPhotoPath() != null ? session.getUserPhotoPath() : "";

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/scribble/contest_write.fxml"));
            if (loader.getLocation() == null) {
                System.err.println("FXML file not found: /com/example/scribble/contest_write.fxml");
                showErrorAlert("Resource Error", "Contest writing page resource not found.");
                return;
            }
            Parent root = loader.load();
            contest_write__c writeController = loader.getController();
            writeController.initData(contestId, genre, userId, username, userPhotoPath);
            Stage stage = (Stage) fantasy_button.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Write for " + genre + " Contest");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Navigation Error", "Failed to load the contest writing page: " + e.getMessage());
        }
    }

    private void navigateToContestEntries(int contestId, String genre) {
        try {
            UserSession session = UserSession.getInstance();
            int userId = session.getUserId();
            String username = session.getUsername();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/scribble/contest_entries.fxml"));
            if (loader.getLocation() == null) {
                System.err.println("FXML file not found: /com/example/scribble/contest_entries.fxml");
                showErrorAlert("Resource Error", "Contest entries page resource not found.");
                return;
            }
            Parent root = loader.load();
            contest_entries__c entriesController = loader.getController();
            entriesController.initData(contestId, genre, userId, username);
            Stage stage = (Stage) fantasy_button.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Contest Entries for " + genre);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Navigation Error", "Failed to load the contest entries page: " + e.getMessage());
        }
    }

    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}