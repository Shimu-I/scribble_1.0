package com.example.scribble;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
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
import java.util.logging.Logger;


public class contest__c {
    private static final Logger LOGGER = Logger.getLogger(contest__c.class.getName());

    @FXML
    public Button spwr_button;

    @FXML
    private Button fantasy_button;

    @FXML
    private Button thriller_mystery_button;

    @FXML
    private Button youth_fiction_button;

    @FXML
    private Button crime_horror_button;

    @FXML private nav_bar__c mainController;

    public void setMainController(nav_bar__c mainController) {
        this.mainController = mainController;
        LOGGER.info("Set mainController in contest__c");
    }

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
            LOGGER.severe("User not logged in, cannot navigate to contest entries for genre: " + genre);
            return;
        }

        int contestId = getContestIdForGenre(genre);
        if (contestId == -1) {
            showErrorAlert("Contest Not Found", "The " + genre + " contest is not available.");
            LOGGER.warning("No contest found for genre: " + genre);
            return;
        }

        navigateToContestEntries(contestId, genre);
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

            // Update AppState to track the previous FXML
            AppState.getInstance().setPreviousFXML("/com/example/scribble/contest.fxml");

            // Load into centerPane instead of replacing the scene
            mainController.getCenterPane().getChildren().setAll(root);
        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Navigation Error", "Failed to load the contest writing page: " + e.getMessage());
        }
    }

    private void navigateToContestEntries(int contestId, String genre) {
        if (mainController == null) {
            LOGGER.severe("Main controller is null in contest__c, cannot navigate to contest_entries.fxml");
            showErrorAlert("Error", "Navigation failed: main controller is not initialized in contest__c.");
            return;
        }

        LOGGER.info("Initiating navigation to contest_entries.fxml with mainController: " + mainController);
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/scribble/contest_entries.fxml"));
            if (loader.getLocation() == null) {
                LOGGER.severe("Resource not found: /com/example/scribble/contest_entries.fxml");
                showErrorAlert("Resource Error", "Contest entries page resource not found.");
                return;
            }
            Parent root = loader.load();
            contest_entries__c controller = loader.getController();
            controller.initData(contestId, genre, UserSession.getInstance().getUserId(), UserSession.getInstance().getUsername(), true); // Default to current week
            controller.setMainController(mainController);
            LOGGER.info("mainController set in contest_entries__c: " + mainController + ", contestId=" + contestId + ", genre=" + genre + ", isCurrentWeekView=true");

            // Set previous FXML in AppState_c
            AppState_c.getInstance().setPreviousFXML("/com/example/scribble/contest.fxml");
            LOGGER.info("Set previousFXML to /com/example/scribble/contest.fxml");

            // Load into centerPane
            mainController.getCenterPane().getChildren().setAll(root);
            LOGGER.info("Successfully navigated to contest_entries.fxml");
        } catch (IOException e) {
            e.printStackTrace();
            LOGGER.severe("Failed to navigate to contest_entries.fxml: " + e.getMessage());
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

    public void handle_spwr_button(ActionEvent actionEvent) {
        if (mainController == null) {
            LOGGER.severe("Main controller is null in contest__c, cannot navigate to c.fxml");
            showErrorAlert("Error", "Navigation failed: main controller is not initialized in contest__c.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/scribble/c.fxml"));
            if (loader.getLocation() == null) {
                LOGGER.severe("Resource not found: /com/example/scribble/c.fxml");
                showErrorAlert("Resource Error", "contest_weekly_results resource not found.");
                return;
            }

            Parent root = loader.load();
            c__c controller = loader.getController();
            controller.setMainController(mainController);
            LOGGER.info("mainController set in c__c: " + mainController);

            // Set previous FXML in AppState_c
            AppState_c.getInstance().setPreviousFXML("/com/example/scribble/contest.fxml");
            LOGGER.info("Set previousFXML to /com/example/scribble/contest.fxml");

            // Load into centerPane
            mainController.getCenterPane().getChildren().setAll(root);
            LOGGER.info("Successfully navigated to c.fxml");
        } catch (IOException e) {
            LOGGER.severe("Error loading c.fxml: " + e.getMessage());
            showErrorAlert("Error", "Failed to load contest_weekly_results: " + e.getMessage());
        }
    }

}