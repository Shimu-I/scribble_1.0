package com.example.scribble;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.Node;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.logging.Logger;

public class history_library__c implements nav_bar__cAware {

    private static final Logger LOGGER = Logger.getLogger(history_library__c.class.getName());

    @FXML
    private VBox history_library_vbox;
    @FXML
    private VBox historyContainer;
    @FXML
    private VBox libraryContainer;

    @FXML
    private Label total_history_record; // Added to match FXML
    @FXML
    private Label total_library_record; // Added to match FXML

    private nav_bar__c mainController;
    private int userId;


    // Modified to return counts separately
    private int[] getRecordCounts() {
        int historyCount = 0;
        int libraryCount = 0;

        // Count history records
        try (Connection conn = db_connect.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT COUNT(*) FROM book_visits WHERE user_id = ?")) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                historyCount = rs.getInt(1);
            }
        } catch (SQLException e) {
            LOGGER.severe("Failed to count history records: " + e.getMessage());
            showAlert("Error", "Failed to count history records: " + e.getMessage());
        }

        // Count library records
        try (Connection conn = db_connect.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT COUNT(*) FROM reading_list WHERE reader_id = ?")) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                libraryCount = rs.getInt(1);
            }
        } catch (SQLException e) {
            LOGGER.severe("Failed to count library records: " + e.getMessage());
            showAlert("Error", "Failed to count library records: " + e.getMessage());
        }

        return new int[]{historyCount, libraryCount};
    }


    @FXML
    public void initialize() {
        userId = UserSession.getInstance().getUserId();
        if (userId == 0) {
            showAlert("Error", "No user logged in");
            return;
        }
        // Apply FXML styles to history_library_vbox
        history_library_vbox.setPrefHeight(332.0);
        history_library_vbox.setPrefWidth(1400.0);
        history_library_vbox.setStyle("-fx-background-color: #005D4D;");
        history_library_vbox.setAlignment(javafx.geometry.Pos.TOP_CENTER);

        // Apply FXML styles to containers
        historyContainer.setStyle("-fx-background-color: #005D4D;");
        historyContainer.setAlignment(javafx.geometry.Pos.TOP_CENTER);
        historyContainer.setPrefHeight(289.0);
        historyContainer.setPrefWidth(307.0); // Match FXML
        historyContainer.setSpacing(10.0);

        libraryContainer.setStyle("-fx-background-color: #005D4D;");
        libraryContainer.setAlignment(javafx.geometry.Pos.TOP_CENTER);
        libraryContainer.setPrefHeight(289.0);
        libraryContainer.setPrefWidth(307.0); // Match FXML
        libraryContainer.setSpacing(10.0);

        LOGGER.info("Initialized history_library_vbox and containers with FXML styles");

        // Get and set record counts
        int[] counts = getRecordCounts();
        total_history_record.setText("(" + counts[0] + ")");
        total_library_record.setText("(" + counts[1] + ")");
        LOGGER.info("Record counts: History (" + counts[0] + "), Library (" + counts[1] + ")");

        populateHistory();
        populateLibrary();
    }

    private void populateHistory() {
        historyContainer.getChildren().clear();
        try (Connection conn = db_connect.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT b.book_id, b.title, b.cover_photo, bv.visited_at " +
                             "FROM book_visits bv JOIN books b ON bv.book_id = b.book_id " +
                             "WHERE bv.user_id = ? ORDER BY bv.visited_at DESC LIMIT 5")) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                historyContainer.getChildren().add(createBookCard(
                        rs.getInt("book_id"),
                        rs.getString("title"),
                        rs.getString("cover_photo"),

                        new SimpleDateFormat("dd/MM/yyyy").format(rs.getTimestamp("visited_at")),
                        "last visited at",
                        null));
            }
        } catch (SQLException e) {
            LOGGER.severe("Failed to load history: " + e.getMessage());
            showAlert("Error", "Failed to load history: " + e.getMessage());
        }
    }

    private void populateLibrary() {
        libraryContainer.getChildren().clear();
        try (Connection conn = db_connect.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT b.book_id, b.title, b.cover_photo, rl.added_at, rl.reading_status " +
                             "FROM reading_list rl JOIN books b ON rl.listed_book_id = b.book_id " +
                             "WHERE rl.reader_id = ?")) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                libraryContainer.getChildren().add(createBookCard(
                        rs.getInt("book_id"),
                        rs.getString("title"),
                        rs.getString("cover_photo"),
                        new SimpleDateFormat("dd/MM/yyyy").format(rs.getTimestamp("added_at")),
                        "saved at",
                        rs.getString("reading_status")));
            }
        } catch (SQLException e) {
            LOGGER.severe("Failed to load library: " + e.getMessage());
            showAlert("Error", "Failed to load library: " + e.getMessage());
        }
    }

    private HBox createBookCard(int bookId, String title, String coverPath, String date, String dateLabel, String readingStatus) {
        HBox card = new HBox();
        card.setStyle("-fx-background-color: #005D4D; -fx-background-radius: 5; -fx-border-color: #fff; -fx-border-radius: 5;");
        card.setAlignment(javafx.geometry.Pos.CENTER);
        card.setPrefHeight(105.0); // Increased to accommodate ChoiceBox below labels
        card.setPrefWidth(270.0);
        card.setMaxHeight(Double.NEGATIVE_INFINITY);
        card.setMaxWidth(Double.NEGATIVE_INFINITY);
        card.setMinHeight(Double.NEGATIVE_INFINITY);
        card.setMinWidth(Double.NEGATIVE_INFINITY);

        ImageView coverImage = new ImageView();
        coverImage.setFitWidth(50.0);
        coverImage.setFitHeight(76.0);
        coverImage.setPreserveRatio(true);
        coverImage.setPickOnBounds(true);
        if (coverPath != null && !coverPath.isEmpty()) {
            try {
                coverImage.setImage(new Image(getClass().getResource("/images/book_covers/" + coverPath).toExternalForm()));
                LOGGER.info("Loaded book cover: " + coverPath);
            } catch (NullPointerException e) {
                LOGGER.warning("Book cover not found: " + coverPath);
                coverImage.setImage(new Image(getClass().getResource("/images/book_covers/hollow_rectangle.png").toExternalForm()));
            }
        } else {
            coverImage.setImage(new Image(getClass().getResource("/images/book_covers/hollow_rectangle.png").toExternalForm()));
            LOGGER.info("Loaded default book cover: hollow_rectangle.png");
        }

        coverImage.setOnMouseClicked(event -> openBook(bookId));

        VBox textBox = new VBox();
        textBox.setStyle("-fx-background-color: transparent;");
        textBox.setMaxHeight(Double.NEGATIVE_INFINITY);
        textBox.setMaxWidth(Double.NEGATIVE_INFINITY);
        textBox.setMinHeight(Double.NEGATIVE_INFINITY);
        textBox.setMinWidth(Double.NEGATIVE_INFINITY);
        textBox.setPrefHeight(110.0); // Increased to accommodate ChoiceBox below labels
        textBox.setPrefWidth(133.0);
        textBox.setSpacing(5.0);
        textBox.setPadding(new Insets(0, 10.0, 0, 10.0));
        textBox.setAlignment(javafx.geometry.Pos.CENTER);

        Label titleLabel = new Label(title);
        titleLabel.setMaxHeight(Double.NEGATIVE_INFINITY);
        titleLabel.setMaxWidth(Double.NEGATIVE_INFINITY);
        titleLabel.setMinWidth(Double.NEGATIVE_INFINITY);
        titleLabel.setPrefHeight(37.0);
        titleLabel.setPrefWidth(122.0);
        titleLabel.setTextFill(javafx.scene.paint.Color.WHITE);
        titleLabel.setWrapText(true);
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 12.0));

        Label dateLabelText = new Label(dateLabel + " " + date);
        dateLabelText.setPrefHeight(24.0);
        dateLabelText.setPrefWidth("last visited at".equals(dateLabel) ? 123.0 : 118.0);
        dateLabelText.setTextAlignment(TextAlignment.CENTER);
        dateLabelText.setTextFill(javafx.scene.paint.Color.WHITE);
        dateLabelText.setWrapText(true);
        dateLabelText.setFont(new Font(10.0));

        // Add labels first
        textBox.getChildren().addAll(titleLabel, dateLabelText);

        // Add ChoiceBox only for library cards, below labels
        if (readingStatus != null) {
            ChoiceBox<String> statusChoiceBox = new ChoiceBox<>();
            statusChoiceBox.getItems().addAll("Reading", "Completed", "Dropped", "SavedForLater");
            statusChoiceBox.setValue(readingStatus);
            statusChoiceBox.setPrefWidth(80.0); // Reduced width
            statusChoiceBox.setPrefHeight(20.0); // Set smaller height
            statusChoiceBox.setMinHeight(20.0);
            statusChoiceBox.setMaxHeight(20.0);
            statusChoiceBox.setStyle("-fx-font-size: 9px;");


            statusChoiceBox.setOnAction(event -> updateReadingStatus(bookId, statusChoiceBox.getValue()));
            textBox.getChildren().add(statusChoiceBox);
        }

        card.getChildren().addAll(coverImage, textBox);

        LOGGER.info("Created book card with FXML-matched styles");
        return card;
    }

    private void openBook(int bookId) {
        if (mainController == null) {
            showAlert("Error", "Navigation failed: mainController is null");
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/scribble/read_book.fxml"));
            Parent readBookPage = loader.load();
            read_book__c readBookController = loader.getController();
            readBookController.setBookId(bookId);
            mainController.loadFXML(String.valueOf(readBookPage));
            Stage stage = (Stage) history_library_vbox.getScene().getWindow();
            stage.setResizable(true);
        } catch (IOException e) {
            LOGGER.severe("Failed to open book: " + e.getMessage());
            showAlert("Error", "Failed to open book: " + e.getMessage());
        }
    }

    private void updateReadingStatus(int bookId, String newStatus) {
        try (Connection conn = db_connect.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "UPDATE reading_list SET reading_status = ? WHERE listed_book_id = ? AND reader_id = ?")) {
            stmt.setString(1, newStatus);
            stmt.setInt(2, bookId);
            stmt.setInt(3, userId);
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                LOGGER.info("Updated reading status for book_id " + bookId + " to " + newStatus);
                showAlert("Success", "Reading status updated to " + newStatus);
            } else {
                LOGGER.warning("No rows updated for book_id " + bookId);
                showAlert("Error", "Failed to update reading status: You may not have permission");
            }
        } catch (SQLException e) {
            LOGGER.severe("Failed to update reading status: " + e.getMessage());
            showAlert("Error", "Failed to update reading status: " + e.getMessage());
        }
    }

    @Override
    public void setMainController(nav_bar__c mainController) {
        this.mainController = mainController;
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}