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
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
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
    private Label total_history_record;
    @FXML
    private Label total_library_record;

    private nav_bar__c mainController;
    private int userId;

    private int[] getRecordCounts() {
        int historyCount = 0;
        int libraryCount = 0;

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
        history_library_vbox.setPrefHeight(332.0);
        history_library_vbox.setPrefWidth(1400.0);
        history_library_vbox.setStyle("-fx-background-color: #005D4D;");
        history_library_vbox.setAlignment(javafx.geometry.Pos.TOP_CENTER);

        historyContainer.setStyle("-fx-background-color: #005D4D;");
        historyContainer.setAlignment(javafx.geometry.Pos.TOP_CENTER);
        historyContainer.setPrefHeight(289.0);
        historyContainer.setPrefWidth(307.0);
        historyContainer.setSpacing(10.0);

        libraryContainer.setStyle("-fx-background-color: #005D4D;");
        libraryContainer.setAlignment(javafx.geometry.Pos.TOP_CENTER);
        libraryContainer.setPrefHeight(289.0);
        libraryContainer.setPrefWidth(307.0);
        libraryContainer.setSpacing(10.0);

        LOGGER.info("Initialized history_library_vbox and containers with FXML styles");

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
                             "WHERE bv.user_id = ? ORDER BY bv.visited_at DESC")) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            int recordCount = 0;
            while (rs.next()) {
                recordCount++;
                int bookId = rs.getInt("book_id");
                String title = rs.getString("title");
                String coverPath = rs.getString("cover_photo");
                String visitedAt = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(rs.getTimestamp("visited_at"));
                LOGGER.info("History record " + recordCount + ": bookId=" + bookId + ", title=" + title + ", visited_at=" + visitedAt);
                historyContainer.getChildren().add(createBookCard(
                        bookId,
                        title,
                        coverPath,
                        visitedAt,
                        "last visited at",
                        null));
            }
            LOGGER.info("Populated history with " + recordCount + " records for userId: " + userId);
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
                             "WHERE rl.reader_id = ? ORDER BY rl.added_at DESC")) {
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
        card.setStyle("-fx-background-color: #F28888; -fx-background-radius: 5; -fx-border-color: #fff; -fx-border-radius: 5; -fx-padding: 5;");
        card.setAlignment(javafx.geometry.Pos.CENTER);
        card.setPrefHeight(105.0);
        card.setPrefWidth(270.0);
        card.setMaxHeight(Double.NEGATIVE_INFINITY);
        card.setMaxWidth(Double.NEGATIVE_INFINITY);
        card.setMinHeight(Double.NEGATIVE_INFINITY);
        card.setMinWidth(Double.NEGATIVE_INFINITY);

        // Spacer region to match FXML
        Region spacer = new Region();
        spacer.setPrefWidth(26.0);
        spacer.setPrefHeight(104.0);

        // ImageView setup
        ImageView coverImage = new ImageView();
        coverImage.setFitWidth(50.0);
        coverImage.setFitHeight(76.0);
        coverImage.setPreserveRatio(true);
        coverImage.setPickOnBounds(true);

        if (coverPath != null && !coverPath.isEmpty()) {
            try {
                // Try loading from filesystem first
                java.io.File uploadFile = new java.io.File("Uploads/book_covers/" + coverPath);
                if (uploadFile.exists()) {
                    Image image = new Image("file:" + uploadFile.getAbsolutePath());
                    if (!image.isError()) {
                        coverImage.setImage(image);
                        LOGGER.info("Loaded book cover from filesystem: file:" + uploadFile.getAbsolutePath());
                    } else {
                        LOGGER.warning("Failed to load book cover from filesystem (image error): " + coverPath);
                        coverImage.setImage(new Image(getClass().getResource("/images/book_covers/hollow_rectangle.png").toExternalForm()));
                        LOGGER.info("Loaded default book cover: hollow_rectangle.png");
                    }
                } else {
                    // Fall back to classpath
                    java.net.URL resource = getClass().getResource("/images/book_covers/" + coverPath);
                    if (resource != null) {
                        Image image = new Image(resource.toExternalForm());
                        if (!image.isError()) {
                            coverImage.setImage(image);
                            LOGGER.info("Loaded book cover from classpath: " + resource.toExternalForm());
                        } else {
                            LOGGER.warning("Failed to load book cover from classpath (image error): " + coverPath);
                            coverImage.setImage(new Image(getClass().getResource("/images/book_covers/hollow_rectangle.png").toExternalForm()));
                            LOGGER.info("Loaded default book cover: hollow_rectangle.png");
                        }
                    } else {
                        LOGGER.warning("Book cover not found: " + coverPath);
                        coverImage.setImage(new Image(getClass().getResource("/images/book_covers/hollow_rectangle.png").toExternalForm()));
                        LOGGER.info("Loaded default book cover: hollow_rectangle.png");
                    }
                }
            } catch (Exception e) {
                LOGGER.severe("Failed to load book cover: " + coverPath + " - " + e.getMessage());
                coverImage.setImage(new Image(getClass().getResource("/images/book_covers/hollow_rectangle.png").toExternalForm()));
                LOGGER.info("Loaded default book cover: hollow_rectangle.png");
            }
        } else {
            coverImage.setImage(new Image(getClass().getResource("/images/book_covers/hollow_rectangle.png").toExternalForm()));
            LOGGER.info("Loaded default book cover: hollow_rectangle.png");
        }
        coverImage.setOnMouseClicked(event -> openBook(bookId));
        HBox.setMargin(coverImage, new Insets(5, 5, 5, 5));

        // VBox for text content
        VBox textBox = new VBox();
        textBox.setPrefHeight(76.0);
        textBox.setPrefWidth(141.0);
        textBox.setSpacing(5.0);
        textBox.setPadding(new Insets(0, 10, 0, 10));
        HBox.setMargin(textBox, new Insets(5, 5, 5, 5));
        HBox.setHgrow(textBox, javafx.scene.layout.Priority.ALWAYS);

        // Title label
        Label titleLabel = new Label(title);
        titleLabel.setPrefHeight(37.0);
        titleLabel.setPrefWidth(122.0);
        titleLabel.setTextFill(javafx.scene.paint.Color.WHITE);
        titleLabel.setWrapText(true);
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 12.0));

        // Date label
        Label dateLabelText = new Label(dateLabel + " " + date);
        dateLabelText.setPrefHeight(24.0);
        dateLabelText.setPrefWidth(123.0);
        dateLabelText.setTextAlignment(TextAlignment.CENTER);
        dateLabelText.setTextFill(javafx.scene.paint.Color.WHITE);
        dateLabelText.setWrapText(true);
        dateLabelText.setFont(new Font(8.0));

        // Add title and date to textBox
        textBox.getChildren().addAll(titleLabel, dateLabelText);

        // Delete button VBox
        VBox deleteButtonBox = new VBox();
        deleteButtonBox.setAlignment(javafx.geometry.Pos.TOP_RIGHT);
        deleteButtonBox.setPrefHeight(104.0);
        deleteButtonBox.setPrefWidth(22.0);
        deleteButtonBox.setPadding(new Insets(5.0, 0, 0, 0));

        // Delete button
        Button deleteButton = new Button();
        deleteButton.setId("delete_record");
        deleteButton.setPrefHeight(18.0);
        deleteButton.setPrefWidth(18.0);
        deleteButton.setMaxHeight(Double.NEGATIVE_INFINITY);
        deleteButton.setMaxWidth(Double.NEGATIVE_INFINITY);
        deleteButton.setMinHeight(Double.NEGATIVE_INFINITY);
        deleteButton.setMinWidth(Double.NEGATIVE_INFINITY);
        deleteButton.setStyle("-fx-background-color: #F82020; -fx-background-radius: 50;");
        ImageView deleteIcon = new ImageView(new Image(getClass().getResource("/images/icons/cross.png").toExternalForm()));
        deleteIcon.setFitHeight(15.0);
        deleteIcon.setFitWidth(15.0);
        deleteIcon.setPickOnBounds(true);
        deleteIcon.setPreserveRatio(true);
        deleteButton.setGraphic(deleteIcon);
        deleteButton.setOnAction(e -> deleteRecord(bookId, readingStatus != null));
        deleteButton.setUserData(bookId); // Set bookId for handle_delete

        // Add delete button to its VBox
        deleteButtonBox.getChildren().add(deleteButton);

        // Add elements to card
        card.getChildren().addAll(spacer, coverImage, textBox, deleteButtonBox);

        // Add ChoiceBox only for library cards
        if (readingStatus != null) {
            ChoiceBox<String> statusChoiceBox = new ChoiceBox<>();
            statusChoiceBox.getItems().addAll("Reading", "Completed", "Dropped", "SavedForLater");
            statusChoiceBox.setValue(readingStatus);
            statusChoiceBox.setPrefWidth(80.0);
            statusChoiceBox.setPrefHeight(20.0);
            statusChoiceBox.setMinHeight(20.0);
            statusChoiceBox.setMaxHeight(20.0);
            statusChoiceBox.setStyle("-fx-font-size: 9px;");
            statusChoiceBox.setOnAction(event -> updateReadingStatus(bookId, statusChoiceBox.getValue()));
            textBox.getChildren().add(statusChoiceBox);
        }

        LOGGER.info("Created book card with FXML-matched styles for bookId: " + bookId);
        return card;
    }

    private void deleteRecord(int bookId, boolean isLibrary) {
        try (Connection conn = db_connect.getConnection()) {
            if (isLibrary) {
                String deleteQuery = "DELETE FROM reading_list WHERE listed_book_id = ? AND reader_id = ?";
                try (PreparedStatement stmt = conn.prepareStatement(deleteQuery)) {
                    stmt.setInt(1, bookId);
                    stmt.setInt(2, userId);
                    int rowsAffected = stmt.executeUpdate();
                    if (rowsAffected > 0) {
                        LOGGER.info("Deleted library record for book_id " + bookId + " and user_id " + userId);
                        populateLibrary();
                        int[] counts = getRecordCounts();
                        total_library_record.setText("(" + counts[1] + ")");
                        showAlert("Success", "Book removed from library.");
                    } else {
                        LOGGER.warning("No library record found for book_id " + bookId + " and user_id " + userId);
                        showAlert("Error", "No record found to delete.");
                    }
                }
            } else {
                String deleteQuery = "DELETE FROM book_visits WHERE book_id = ? AND user_id = ?";
                try (PreparedStatement stmt = conn.prepareStatement(deleteQuery)) {
                    stmt.setInt(1, bookId);
                    stmt.setInt(2, userId);
                    int rowsAffected = stmt.executeUpdate();
                    if (rowsAffected > 0) {
                        LOGGER.info("Deleted history record for book_id " + bookId + " and user_id " + userId);
                        populateHistory();
                        int[] counts = getRecordCounts();
                        total_history_record.setText("(" + counts[0] + ")");
                        showAlert("Success", "Visit history removed.");
                    } else {
                        LOGGER.warning("No history record found for book_id " + bookId + " and user_id " + userId);
                        showAlert("Error", "No record found to delete.");
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.severe("Failed to delete record: " + e.getMessage());
            showAlert("Error", "Failed to delete record: " + e.getMessage());
        }
    }

    @FXML
    public void handle_delete(ActionEvent actionEvent) {
        Button button = (Button) actionEvent.getSource();
        Integer bookId = (Integer) button.getUserData();
        if (bookId != null) {
            boolean isLibrary = button.getParent().getParent() instanceof HBox &&
                    ((HBox) button.getParent().getParent()).getParent() == libraryContainer;
            deleteRecord(bookId, isLibrary);
        } else {
            LOGGER.warning("No book ID found for delete action");
            showAlert("Error", "No book selected for deletion.");
        }
    }

    private void addBookToLibrary(int bookId, String readingStatus) {
        try (Connection conn = db_connect.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "INSERT INTO reading_list (reader_id, listed_book_id, reading_status, added_at) VALUES (?, ?, ?, CURRENT_TIMESTAMP)")) {
            stmt.setInt(1, userId);
            stmt.setInt(2, bookId);
            stmt.setString(3, readingStatus);
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                LOGGER.info("Added book_id " + bookId + " to library for user_id " + userId + " with status " + readingStatus);
                populateLibrary();
                int[] counts = getRecordCounts();
                total_library_record.setText("(" + counts[1] + ")");
                showAlert("Success", "Book added to library.");
            } else {
                LOGGER.warning("Failed to add book_id " + bookId + " to library for user_id " + userId);
                showAlert("Error", "Failed to add book to library.");
            }
        } catch (SQLException e) {
            LOGGER.severe("Failed to add book to library: " + e.getMessage());
            showAlert("Error", "Failed to add book to library: " + e.getMessage());
        }
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
                     "UPDATE reading_list SET reading_status = ?, added_at = CURRENT_TIMESTAMP WHERE listed_book_id = ? AND reader_id = ?")) {
            stmt.setString(1, newStatus);
            stmt.setInt(2, bookId);
            stmt.setInt(3, userId);
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                LOGGER.info("Updated reading status for book_id " + bookId + " to " + newStatus + " and reset added_at timestamp");
                populateLibrary(); // Refresh the library to reflect the new order
                int[] counts = getRecordCounts();
                total_library_record.setText("(" + counts[1] + ")");
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
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}