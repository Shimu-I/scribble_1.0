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
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Logger;
import com.example.scribble.AppState_c;

public class contest_read_entry__c {

    private static final Logger LOGGER = Logger.getLogger(contest_read_entry__c.class.getName());


    @FXML
    private Button back_button;

    @FXML
    private Label book_tittle;

    @FXML
    private Label genre_name;

    @FXML
    private ImageView cover_photo;

    @FXML
    private TextArea writing_area;

    private int entryId;
    private int contestId;
    private String genre;
    private int userId;
    private String username;

    @FXML private nav_bar__c mainController;

    public void setMainController(nav_bar__c mainController) {
        this.mainController = mainController;
        LOGGER.info("Set mainController in contest_read_entry__c");
    }

    public void initData(int entryId) {
        this.entryId = entryId;
        this.userId = UserSession.getInstance().getUserId();
        this.username = UserSession.getInstance().getUsername();
        loadEntryDetails();
    }

    private void loadEntryDetails() {
        if (!UserSession.getInstance().isLoggedIn()) {
            showErrorAlert("Session Error", "You must be logged in to view contest entries.");
            return;
        }

        try (Connection conn = db_connect.getConnection()) {
            if (conn == null) {
                showErrorAlert("Database Error", "Failed to connect to the database.");
                return;
            }

            String query = "SELECT ce.entry_title, ce.content, ce.cover_photo, u.username, c.genre, c.contest_id " +
                    "FROM contest_entries ce " +
                    "JOIN users u ON ce.user_id = u.user_id " +
                    "JOIN contests c ON ce.contest_id = c.contest_id " +
                    "WHERE ce.entry_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setInt(1, entryId);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    book_tittle.setText(rs.getString("entry_title"));
                    genre_name.setText("(genre: " + rs.getString("genre") + ")");
                    writing_area.setText(rs.getString("content"));
                    writing_area.setEditable(false);
                    String coverPhotoUrl = rs.getString("cover_photo");
                    if (coverPhotoUrl != null && !coverPhotoUrl.isEmpty()) {
                        String resourcePath = "/images/contest_book_cover/" + coverPhotoUrl;
                        URL resourceUrl = getClass().getResource(resourcePath);
                        if (resourceUrl != null) {
                            try {
                                cover_photo.setImage(new Image(resourceUrl.toExternalForm()));
                            } catch (Exception e) {
                                System.err.println("Failed to load cover photo: " + resourcePath + ", Error: " + e.getMessage());
                                cover_photo.setImage(new Image(getClass().getResource("/images/contest_book_cover/demo_cover_photo.png").toExternalForm()));
                            }
                        } else {
                            System.err.println("Resource not found: " + resourcePath);
                            cover_photo.setImage(new Image(getClass().getResource("/images/contest_book_cover/demo_cover_photo.png").toExternalForm()));
                        }
                    } else {
                        System.err.println("Cover photo is null or empty for entry_id: " + entryId);
                        cover_photo.setImage(new Image(getClass().getResource("/images/contest_book_cover/demo_cover_photo.png").toExternalForm()));
                    }
                    this.contestId = rs.getInt("contest_id");
                    this.genre = rs.getString("genre");
                } else {
                    showErrorAlert("Data Error", "Contest entry not found for ID: " + entryId);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showErrorAlert("Database Error", "Failed to load contest entry details: " + e.getMessage());
        }
    }

    @FXML
    private void handle_back_button(ActionEvent event) {
        if (mainController == null) {
            LOGGER.severe("Main controller is null, cannot navigate back from contest read entry page");
            showErrorAlert("Error", "Navigation failed: main controller is not initialized.");
            return;
        }

        try {
            String previousFXML = AppState_c.getInstance().getPreviousFXML();
            if (previousFXML == null || previousFXML.isEmpty()) {
                previousFXML = "/com/example/scribble/contest_entries.fxml";
                LOGGER.warning("No previous FXML set in AppState_c, using default: " + previousFXML);
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource(previousFXML));
            if (loader.getLocation() == null) {
                LOGGER.severe("Resource not found: " + previousFXML);
                showErrorAlert("Resource Error", "Previous page resource not found: " + previousFXML);
                return;
            }

            Parent root = loader.load();
            Object controller = loader.getController();

            if (controller instanceof contest_entries__c entriesController) {
                entriesController.initData(contestId, genre, userId, username);
                entriesController.setMainController(mainController);
                LOGGER.info("Initialized contest_entries__c with contestId=" + contestId + ", genre=" + genre);
            } else if (controller instanceof contest__c contestController) {
                contestController.setMainController(mainController);
                LOGGER.info("Initialized contest__c");
            } else {
                LOGGER.info("Target controller does not support setMainController: " + controller.getClass().getName());
            }

            mainController.getCenterPane().getChildren().setAll(root);
            LOGGER.info("Navigated back to " + previousFXML);
        } catch (IOException e) {
            LOGGER.severe("Failed to navigate back to previous page: " + e.getMessage());
            showErrorAlert("Navigation Error", "Failed to return to the previous page: " + e.getMessage());
        }
    }

    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void setEntryId(int entryId) {
        this.entryId = entryId;
        loadEntryDetails();
    }
}