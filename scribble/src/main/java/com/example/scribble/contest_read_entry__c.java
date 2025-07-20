package com.example.scribble;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Logger;

public class contest_read_entry__c {

    private static final Logger LOGGER = Logger.getLogger(contest_read_entry__c.class.getName());
    private static final String CLASSPATH_IMAGE_PATH = "/images/contest_book_cover/";
    private static final String FILESYSTEM_IMAGE_PATH = "Uploads/contest_book_cover/";
    private static final String DEFAULT_COVER_PHOTO = "/images/contest_book_cover/demo_cover_photo.png";

    @FXML private Button back_button;
    @FXML private Label book_tittle;
    @FXML private Label genre_name;
    @FXML private ImageView cover_photo;
    @FXML private TextArea writing_area;

    private int entryId;
    private int contestId;
    private String genre;
    private int userId;
    private String username;
    private boolean isCurrentWeekView; // Added to store week view state

    @FXML private nav_bar__c mainController;

    @FXML
    public void initialize() {
        if (cover_photo != null) {
            cover_photo.setVisible(true);
            LOGGER.info("cover_photo ImageView initialized: visible=" + cover_photo.isVisible() +
                    ", fitWidth=" + cover_photo.getFitWidth() +
                    ", fitHeight=" + cover_photo.getFitHeight());
        } else {
            LOGGER.severe("cover_photo ImageView is null in initialize!");
        }
    }

    public void setMainController(nav_bar__c mainController) {
        this.mainController = mainController;
        LOGGER.info("Set mainController in contest_read_entry__c");
    }

    public void initData(int entryId, boolean isCurrentWeekView) { // Updated to accept isCurrentWeekView
        this.entryId = entryId;
        this.isCurrentWeekView = isCurrentWeekView; // Store the week view state
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
                    LOGGER.info("Loaded cover_photo from database for entryId=" + entryId + ": " + coverPhotoUrl);
                    updateCoverPhoto(coverPhotoUrl);
                    this.contestId = rs.getInt("contest_id");
                    this.genre = rs.getString("genre");
                } else {
                    showErrorAlert("Data Error", "Contest entry not found for ID: " + entryId);
                    loadDefaultCoverPhoto();
                }
            }
        } catch (SQLException e) {
            LOGGER.severe("Failed to load contest entry details for entryId=" + entryId + ": " + e.getMessage());
            showErrorAlert("Database Error", "Failed to load contest entry details: " + e.getMessage());
        }
    }

    private void updateCoverPhoto(String coverPhotoUrl) {
        if (coverPhotoUrl != null && !coverPhotoUrl.isEmpty()) {
            if (!coverPhotoUrl.matches(".*\\.(png|jpg|jpeg|gif|bmp)$")) {
                LOGGER.warning("Invalid image format for cover photo: " + coverPhotoUrl + " for entryId=" + entryId);
                loadDefaultCoverPhoto();
                return;
            }

            // Try loading from Uploads directory first
            File uploadFile = new File(FILESYSTEM_IMAGE_PATH + coverPhotoUrl);
            if (uploadFile.exists()) {
                try {
                    Image image = new Image("file:" + uploadFile.getAbsolutePath());
                    cover_photo.setImage(image);
                    LOGGER.info("Cover photo loaded from filesystem for entryId=" + entryId + ": file:" + uploadFile.getAbsolutePath());
                    return;
                } catch (Exception e) {
                    LOGGER.severe("Failed to load cover photo from filesystem: " + uploadFile.getAbsolutePath() + ", Error: " + e.getMessage());
                }
            } else {
                LOGGER.info("Cover photo not found in filesystem: " + uploadFile.getAbsolutePath() + ", trying classpath");
            }

            // Fallback to classpath
            String resourcePath = CLASSPATH_IMAGE_PATH + coverPhotoUrl;
            URL resourceUrl = getClass().getResource(resourcePath);
            if (resourceUrl != null) {
                try {
                    Image image = new Image(resourceUrl.toExternalForm());
                    cover_photo.setImage(image);
                    LOGGER.info("Cover photo loaded from classpath for entryId=" + entryId + ": " + resourceUrl.toExternalForm());
                } catch (Exception e) {
                    LOGGER.severe("Failed to load cover photo from classpath: " + resourcePath + ", Error: " + e.getMessage());
                    loadDefaultCoverPhoto();
                }
            } else {
                LOGGER.warning("Resource not found in classpath: " + resourcePath + " for entryId=" + entryId);
                loadDefaultCoverPhoto();
            }
        } else {
            LOGGER.info("Cover photo is null or empty for entryId=" + entryId + ", loading default image");
            loadDefaultCoverPhoto();
        }
    }

    private void loadDefaultCoverPhoto() {
        URL defaultUrl = getClass().getResource(DEFAULT_COVER_PHOTO);
        if (defaultUrl != null) {
            Image defaultImage = new Image(defaultUrl.toExternalForm());
            cover_photo.setImage(defaultImage);
            LOGGER.info("Loaded default cover photo for entryId=" + entryId + ": " + DEFAULT_COVER_PHOTO);
        } else {
            LOGGER.severe("Default cover photo not found: " + DEFAULT_COVER_PHOTO + " for entryId=" + entryId);
            cover_photo.setImage(null);
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
                entriesController.initData(contestId, genre, userId, username, isCurrentWeekView); // Pass isCurrentWeekView
                entriesController.setMainController(mainController);
                LOGGER.info("Initialized contest_entries__c with contestId=" + contestId + ", genre=" + genre + ", isCurrentWeekView=" + isCurrentWeekView);
            } else if (controller instanceof contest__c contestController) {
                contestController.setMainController(mainController);
                LOGGER.info("Initialized contest__c");
            } else {
                LOGGER.info("Target controller does not support setMainController: " + (controller != null ? controller.getClass().getName() : "null"));
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