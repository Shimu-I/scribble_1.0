package com.example.scribble;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.logging.Logger;

public class author_profile__c implements nav_bar__cAware {

    private static final Logger LOGGER = Logger.getLogger(author_profile__c.class.getName());

    @FXML private ImageView cover_photo;
    @FXML private Label author_name;
    @FXML private Label author_email;
    @FXML private Label joined_at;
    @FXML private Label total_number_of_author_works;
    @FXML private VBox vbox_container;
    @FXML private Button back_button;

    private int authorId;
    private int userId;
    private int previousBookId; // Store the bookId to return to
    private boolean userIdSetExternally = false;

    private nav_bar__c mainController;

    @FXML
    public void initialize() {
        if (!userIdSetExternally) {
            userId = UserSession.getInstance().getUserId();
            LOGGER.info("Using UserSession userId: " + userId);
        }

        if (userId == 0) {
            showAlert("Error", "No user specified");
            return;
        }

        loadAuthorInfo();
        populateAuthorWorks();
    }

    public void setUserId(int userId) {
        this.userId = userId;
        this.userIdSetExternally = true;
        LOGGER.info("setUserId called with user_id: " + userId);
        if (author_name != null) {
            loadAuthorInfo();
            populateAuthorWorks();
        }
    }

    public void setAuthorId(int authorId) {
        this.authorId = authorId;
        this.userId = authorId; // Sync userId with authorId
        this.userIdSetExternally = true;
        LOGGER.info("setAuthorId called with authorId: " + authorId + ", setting userId to " + userId);
        if (author_name != null) {
            loadAuthorInfo();
            populateAuthorWorks();
        }
    }

    public void setPreviousBookId(int previousBookId) {
        this.previousBookId = previousBookId;
        LOGGER.info("setPreviousBookId called with bookId: " + previousBookId);
    }

    private void loadAuthorInfo() {
        try (Connection conn = db_connect.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT username, email, created_at FROM users WHERE user_id = ?")) { // Removed profile_photo
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                author_name.setText(rs.getString("username"));
                author_email.setText(rs.getString("email"));
                String joinDate = new SimpleDateFormat("MMMM d, yyyy").format(rs.getTimestamp("created_at"));
                joined_at.setText("Member since " + joinDate);

                // Default image since profile_photo column is missing
                cover_photo.setImage(new Image(getClass().getResource("/images/profiles/hollow_circle.png").toExternalForm()));
                LOGGER.info("Loaded default profile photo for user_id " + userId + ": hollow_circle.png");
            } else {
                showAlert("Error", "User not found for ID: " + userId);
                author_name.setText("Unknown User");
            }
        } catch (SQLException e) {
            LOGGER.severe("Failed to load author info for user_id " + userId + ": " + e.getMessage());
            showAlert("Error", "Failed to load author info: " + e.getMessage());
        }
    }

    private void populateAuthorWorks() {
        vbox_container.getChildren().clear();
        int worksCount = 0;

        try (Connection conn = db_connect.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT b.book_id, b.title, b.cover_photo, b.created_at " +
                             "FROM book_authors ba JOIN books b ON ba.book_id = b.book_id " +
                             "WHERE ba.user_id = ? ORDER BY b.created_at DESC")) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            HBox currentRow = null;
            int columnCount = 0;

            while (rs.next()) {
                if (columnCount == 0 || columnCount >= 3) {
                    currentRow = new HBox();
                    currentRow.setAlignment(Pos.CENTER_LEFT);
                    currentRow.setSpacing(30.0);
                    currentRow.setPadding(new Insets(0, 20.0, 0, 20.0));
                    currentRow.setStyle("-fx-background-color: #005D4D;");
                    vbox_container.getChildren().add(currentRow);
                    columnCount = 0;
                }

                HBox bookCard = createBookCard(
                        rs.getInt("book_id"),
                        rs.getString("title"),
                        rs.getString("cover_photo"),
                        new SimpleDateFormat("dd/MM/yyyy").format(rs.getTimestamp("created_at"))
                );
                currentRow.getChildren().add(bookCard);
                columnCount++;
                worksCount++;
            }

            total_number_of_author_works.setText("(" + worksCount + ")");
            LOGGER.info("Loaded " + worksCount + " author works for user_id " + userId);
        } catch (SQLException e) {
            LOGGER.severe("Failed to load author works for user_id " + userId + ": " + e.getMessage());
            showAlert("Error", "Failed to load author works: " + e.getMessage());
        }
    }

    private HBox createBookCard(int bookId, String title, String coverPath, String date) {
        HBox card = new HBox();
        card.setAlignment(Pos.CENTER);
        card.setStyle("-fx-background-color: #005D4D; -fx-border-color: white; -fx-border-radius: 5; -fx-background-radius: 5;");
        card.setPrefHeight(154.0);
        card.setPrefWidth(306.0);
        card.setMaxHeight(Double.NEGATIVE_INFINITY);
        card.setMaxWidth(Double.NEGATIVE_INFINITY);
        card.setMinHeight(Double.NEGATIVE_INFINITY);
        card.setMinWidth(Double.NEGATIVE_INFINITY);
        card.setSpacing(10.0);

        ImageView coverImage = new ImageView();
        coverImage.setFitWidth(80.0);
        coverImage.setFitHeight(118.0);
        coverImage.setPreserveRatio(true);
        coverImage.setPickOnBounds(true);
        if (coverPath != null && !coverPath.isEmpty()) {
            try {
                URL url = getClass().getResource("/images/book_covers/" + coverPath);
                if (url == null) {
                    LOGGER.warning("Book cover URL not found: /images/book_covers/" + coverPath);
                }
                coverImage.setImage(new Image(url.toExternalForm()));
                LOGGER.info("Loaded book cover for book_id " + bookId + ": " + coverPath);
            } catch (Exception e) {
                LOGGER.warning("Book cover not found for book_id " + bookId + ": " + coverPath + ", error: " + e.getMessage());
                coverImage.setImage(new Image(getClass().getResource("/images/book_covers/hollow_rectangle.png").toExternalForm()));
            }
        } else {
            coverImage.setImage(new Image(getClass().getResource("/images/book_covers/hollow_rectangle.png").toExternalForm()));
            LOGGER.info("Loaded default book cover for book_id " + bookId + ": hollow_rectangle.png");
        }

        coverImage.setOnMouseClicked(event -> openBook(bookId));

        VBox textBox = new VBox();
        textBox.setStyle("-fx-background-color: transparent;");
        textBox.setMaxHeight(Double.NEGATIVE_INFINITY);
        textBox.setMaxWidth(Double.NEGATIVE_INFINITY);
        textBox.setMinHeight(Double.NEGATIVE_INFINITY);
        textBox.setMinWidth(Double.NEGATIVE_INFINITY);
        textBox.setPrefHeight(114.0);
        textBox.setPrefWidth(182.0);
        textBox.setSpacing(5.0);
        textBox.setPadding(new Insets(0, 10.0, 0, 10.0));
        textBox.setAlignment(Pos.CENTER);

        Label titleLabel = new Label(title);
        titleLabel.setMaxHeight(Double.NEGATIVE_INFINITY);
        titleLabel.setMaxWidth(Double.NEGATIVE_INFINITY);
        titleLabel.setMinWidth(Double.NEGATIVE_INFINITY);
        titleLabel.setPrefHeight(57.0);
        titleLabel.setPrefWidth(122.0);
        titleLabel.setTextFill(javafx.scene.paint.Color.WHITE);
        titleLabel.setWrapText(true);
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 18.0));

        Label dateLabel = new Label("posted on " + date);
        dateLabel.setPrefHeight(42.0);
        dateLabel.setPrefWidth(160.0);
        dateLabel.setTextAlignment(TextAlignment.CENTER);
        dateLabel.setTextFill(javafx.scene.paint.Color.WHITE);
        dateLabel.setWrapText(true);
        dateLabel.setFont(new Font(12.0));

        textBox.getChildren().addAll(titleLabel, dateLabel);
        card.getChildren().addAll(coverImage, textBox);

        LOGGER.info("Created book card for book_id: " + bookId);
        return card;
    }

    private void openBook(int bookId) {
        if (mainController == null) {
            showAlert("Error", "Navigation failed: mainController is null");
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/scribble/read_book__c.fxml"));
            Parent readBookPage = loader.load();
            read_book__c readBookController = loader.getController();
            readBookController.setBookId(bookId);
            mainController.loadFXML(String.valueOf(readBookPage)); // Adjust based on nav_bar__c implementation
            Stage stage = (Stage) cover_photo.getScene().getWindow();
            stage.setResizable(true);
            LOGGER.info("Navigated to read_book__c.fxml for book_id: " + bookId);
        } catch (IOException e) {
            LOGGER.severe("Failed to open book_id " + bookId + ": " + e.getMessage());
            showAlert("Error", "Failed to open book: " + e.getMessage());
        }
    }

    @FXML
    private void handle_back_button(ActionEvent event) {
        if (mainController != null) {
            try {
                if (previousBookId == 0) {
                    LOGGER.warning("No previous bookId set, using default 1");
                    previousBookId = 1; // Fallback if not set
                }
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/scribble/read_book__c.fxml"));
                Parent readBookPage = loader.load();
                read_book__c readBookController = loader.getController();
                readBookController.setBookId(previousBookId);
                mainController.loadFXML(String.valueOf(readBookPage));
                LOGGER.info("Navigated back to read_book__c.fxml for bookId: " + previousBookId + " from author profile for authorId: " + authorId);
            } catch (IOException e) {
                LOGGER.severe("Failed to navigate back to read_book__c.fxml: " + e.getMessage());
                showAlert("Error", "Failed to navigate back: " + e.getMessage());
            }
        } else {
            LOGGER.warning("Cannot navigate back: mainController is null");
            showAlert("Error", "Navigation failed: mainController is null");
        }
    }

    @Override
    public void setMainController(nav_bar__c mainController) {
        this.mainController = mainController;
        LOGGER.info("setMainController called with: " + (mainController != null ? "set" : "null"));
        // Apply background color
        BorderPane root = (BorderPane) back_button.getScene().getRoot();
        if (root != null) {
            root.setStyle("-fx-background-color: #005D4D;");
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}