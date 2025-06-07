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
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class contest_write__c {

    @FXML
    private Button back_button;

    @FXML
    private Button upload_button;

    @FXML
    private Button cover_photo_button;

    @FXML
    private TextField book_tittle;

    @FXML
    private Label genre_name;

    @FXML
    private ImageView cover_photo;

    @FXML
    private TextArea writing_area;

    private int contestId;
    private String genre;
    private int userId;
    private String username;
    private String userPhotoPath;
    private String selectedCoverPhotoPath;

    public void initData(int contestId, String genre, int userId, String username, String userPhotoPath) {
        this.contestId = contestId;
        this.genre = genre;
        this.userId = userId;
        this.username = username;
        this.userPhotoPath = userPhotoPath;

        book_tittle.setText("");
        book_tittle.setPromptText("write the tittle of this book");
        genre_name.setText("(genre: " + genre.toLowerCase() + ")");
        selectedCoverPhotoPath = null;
        try {
            String defaultImagePath = "/images/contest_book_cover/demo_cover_photo.png";
            java.net.URL resource = getClass().getResource(defaultImagePath);
            if (resource != null) {
                cover_photo.setImage(new Image(resource.toExternalForm()));
            } else {
                showErrorAlert("Resource Error", "Default cover photo not found in classpath.");
                cover_photo.setImage(null);
            }
        } catch (Exception e) {
            e.printStackTrace();
            showErrorAlert("Resource Error", "Failed to load default cover photo.");
            cover_photo.setImage(null);
        }
        writing_area.setText("");
    }

    @FXML
    private void handle_back_button(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/scribble/contest.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) back_button.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Pen Wars Contests");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Navigation Error", "Failed to return to the contest page.");
        }
    }

    @FXML
    private void handle_upload_button(ActionEvent event) {
        System.out.println("Upload button clicked at " + new java.util.Date());
        String entryTitle = book_tittle.getText().trim();
        String content = writing_area.getText().trim();

        if (entryTitle.isEmpty() || content.isEmpty()) {
            showErrorAlert("Input Error", "Please provide a title and content for your entry.");
            System.out.println("Validation failed: Empty title or content");
            return;
        }

        if (!UserSession.getInstance().isLoggedIn()) {
            showErrorAlert("Session Error", "You must be logged in to submit an entry.");
            return;
        }

        try (Connection conn = db_connect.getConnection()) {
            System.out.println("Database connection established: " + (conn != null));
            conn.setAutoCommit(false);

            int contestId = getOrCreateContestId(conn, genre);
            if (contestId == -1) {
                conn.rollback();
                showErrorAlert("Contest Error", "Failed to retrieve or create contest for genre: " + genre);
                System.out.println("Contest creation failed, rolling back");
                return;
            }

            String insertEntrySQL = "INSERT INTO contest_entries (contest_id, user_id, entry_title, content, cover_photo) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(insertEntrySQL)) {
                stmt.setInt(1, contestId);
                stmt.setInt(2, userId);
                stmt.setString(3, entryTitle);
                stmt.setString(4, content);
                stmt.setString(5, selectedCoverPhotoPath);
                System.out.println("Executing entry insert with contestId: " + contestId + ", userId: " + userId);
                int rowsAffected = stmt.executeUpdate();
                System.out.println("Rows affected for entry: " + rowsAffected);
            }

            conn.commit();
            System.out.println("Transaction committed successfully");
            showInfoAlert("Success", "Your entry has been submitted successfully!");
            clearForm();
        } catch (SQLException e) {
            e.printStackTrace();
            showErrorAlert("Database Error", "Failed to submit your entry. Error: " + e.getMessage());
            System.out.println("SQLException caught: " + e.getMessage());
        }
    }

    private int getOrCreateContestId(Connection conn, String genre) throws SQLException {
        int contestId = -1;
        String selectSQL = "SELECT contest_id FROM contests WHERE genre = ? COLLATE utf8mb4_bin"; // Case-sensitive
        try (PreparedStatement selectStmt = conn.prepareStatement(selectSQL)) {
            selectStmt.setString(1, genre);
            System.out.println("Checking for existing contest with genre: " + genre);
            ResultSet rs = selectStmt.executeQuery();
            if (rs.next()) {
                contestId = rs.getInt("contest_id");
                System.out.println("Found existing contestId: " + contestId + " for genre: " + genre);
            } else {
                System.out.println("No existing contest found for genre: " + genre + ", attempting insertion");
                String insertSQL = "INSERT INTO contests (title, genre, created_at) VALUES (?, ?, NOW())";
                try (PreparedStatement insertStmt = conn.prepareStatement(insertSQL, PreparedStatement.RETURN_GENERATED_KEYS)) {
                    insertStmt.setString(1, genre + " Writing Contest");
                    insertStmt.setString(2, genre);
                    System.out.println("Executing insert for new contest: " + genre + " Writing Contest");
                    int rowsAffected = insertStmt.executeUpdate();
                    System.out.println("Rows affected by insert: " + rowsAffected);
                    if (rowsAffected > 0) {
                        try (ResultSet generatedKeys = insertStmt.getGeneratedKeys()) {
                            if (generatedKeys.next()) {
                                contestId = generatedKeys.getInt(1);
                                System.out.println("New contestId created: " + contestId);
                            } else {
                                System.out.println("Error: No generated keys returned after insert");
                            }
                        }
                    } else {
                        System.out.println("Error: Insert failed, no rows affected");
                    }
                } catch (SQLException e) {
                    System.out.println("SQL Exception during insert: " + e.getMessage());
                    throw e; // Re-throw to be caught by the outer try-catch
                }
            }
        }
        if (contestId == -1) {
            System.out.println("Warning: Returning default contestId -1");
        }
        return contestId;
    }

    @FXML
    private void handle_cover_photo_button(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Cover Photo");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );

        File selectedFile = fileChooser.showOpenDialog(cover_photo_button.getScene().getWindow());
        if (selectedFile != null) {
            String filename = selectedFile.getName();
            Path destinationPath = Path.of(System.getProperty("user.home"), "scribble/uploads", filename);

            try {
                Files.createDirectories(destinationPath.getParent());
                Files.copy(selectedFile.toPath(), destinationPath, StandardCopyOption.REPLACE_EXISTING);
                selectedCoverPhotoPath = filename; // Store filename only
                cover_photo.setImage(new Image("file:" + destinationPath.toString())); // Load full path for display
            } catch (IOException e) {
                e.printStackTrace();
                showErrorAlert("Image Error", "Failed to copy the selected cover photo.");
                selectedCoverPhotoPath = null;
            }
        }
    }

    private void clearForm() {
        book_tittle.setText("");
        writing_area.setText("");
        selectedCoverPhotoPath = null;
        try {
            String defaultImagePath = "/images/contest_book_cover/demo_cover_photo.png";
            java.net.URL resource = getClass().getResource(defaultImagePath);
            if (resource != null) {
                cover_photo.setImage(new Image(resource.toExternalForm()));
            } else {
                showErrorAlert("Resource Error", "Default cover photo not found in classpath.");
                cover_photo.setImage(null);
            }
        } catch (Exception e) {
            e.printStackTrace();
            showErrorAlert("Resource Error", "Failed to load default cover photo.");
            cover_photo.setImage(null);
        }
    }

    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfoAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}