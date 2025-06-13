package com.example.scribble;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.logging.Logger;

public class my_works_my_drafts__c implements nav_bar__cAware {

    private static final Logger LOGGER = Logger.getLogger(my_works_my_drafts__c.class.getName());

    @FXML
    private VBox my_work_my_draft__vbox;
    @FXML
    private VBox myWorkContainer;
    @FXML
    private VBox myDraftContainer;
    @FXML
    private Button history_library_button;
    @FXML
    private Button my_work_my_draft_button;
    @FXML
    private Button colab_sent_received_button;
    @FXML
    private Button groups_joined_owned_button;
    @FXML
    private Button open_draft;
    @FXML
    private Label total_my_works_record;
    @FXML
    private Label total_my_drafts_record;

    private nav_bar__c mainController;
    private int userId;

    private int[] getRecordCounts() {
        int worksCount = 0;
        int draftsCount = 0;

        try (Connection conn = db_connect.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT COUNT(*) FROM book_authors WHERE user_id = ?")) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                worksCount = rs.getInt(1);
            }
        } catch (SQLException e) {
            LOGGER.severe("Failed to count My Works records: " + e.getMessage());
            showAlert("Error", "Failed to count My Works records: " + e.getMessage());
        }

        try (Connection conn = db_connect.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT COUNT(*) FROM draft_chapters WHERE author_id = ?")) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                draftsCount = rs.getInt(1);
            }
        } catch (SQLException e) {
            LOGGER.severe("Failed to count My Drafts records: " + e.getMessage());
            showAlert("Error", "Failed to count My Drafts records: " + e.getMessage());
        }

        return new int[]{worksCount, draftsCount};
    }

    @FXML
    public void initialize() {
        userId = UserSession.getInstance().getUserId();
        if (userId == 0) {
            showAlert("Error", "No user logged in");
            return;
        }
        my_work_my_draft__vbox.setStyle("-fx-background-color: #005D4D;");
        myWorkContainer.setStyle("-fx-background-color: #005D4D;");
        myDraftContainer.setStyle("-fx-background-color: #005D4D;");
        LOGGER.info("Initialized my_work_my_draft__vbox and containers with FXML styles");

        int[] counts = getRecordCounts();
        total_my_works_record.setText("(" + counts[0] + ")");
        total_my_drafts_record.setText("(" + counts[1] + ")");
        LOGGER.info("Record counts: My Works (" + counts[0] + "), My Drafts (" + counts[1] + ")");

        populateMyWorks();
        populateMyDrafts();
    }

    private void populateMyWorks() {
        myWorkContainer.getChildren().clear();
        try (Connection conn = db_connect.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT b.book_id, b.title, b.cover_photo, b.created_at " +
                             "FROM book_authors ba JOIN books b ON ba.book_id = b.book_id " +
                             "WHERE ba.user_id = ? ORDER BY b.created_at DESC")) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                myWorkContainer.getChildren().add(createBookCard(
                        rs.getInt("book_id"),
                        rs.getString("title"),
                        rs.getString("cover_photo"),
                        new SimpleDateFormat("dd/MM/yyyy").format(rs.getTimestamp("created_at")),
                        "posted on"));
            }
        } catch (SQLException e) {
            LOGGER.severe("Failed to load user works: " + e.getMessage());
            showAlert("Error", "Failed to load user works: " + e.getMessage());
        }
    }

    private void populateMyDrafts() {
        myDraftContainer.getChildren().clear();
        try (Connection conn = db_connect.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT b.book_id, b.title, b.cover_photo, dc.chapter_number, dc.updated_at " +
                             "FROM draft_chapters dc JOIN books b ON dc.book_id = b.book_id " +
                             "WHERE dc.author_id = ? ORDER BY dc.updated_at DESC")) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                myDraftContainer.getChildren().add(createDraftCard(
                        rs.getInt("book_id"),
                        rs.getString("title"),
                        rs.getString("cover_photo"),
                        rs.getInt("chapter_number"),
                        new SimpleDateFormat("dd/MM/yyyy").format(rs.getTimestamp("updated_at"))));
            }
        } catch (SQLException e) {
            LOGGER.severe("Failed to load drafts: " + e.getMessage());
            showAlert("Error", "Failed to load drafts: " + e.getMessage());
        }
    }

    private HBox createBookCard(int bookId, String title, String coverPath, String date, String dateLabel) {
        HBox card = new HBox();
        card.setAlignment(Pos.CENTER);
        card.setStyle("-fx-background-color: #F28888; -fx-border-color: white; -fx-border-radius: 5; -fx-background-radius: 5;");
        card.setPrefHeight(105.0);
        card.setPrefWidth(270.0);
        card.setMaxHeight(Double.NEGATIVE_INFINITY);
        card.setMaxWidth(Double.NEGATIVE_INFINITY);
        card.setMinHeight(Double.NEGATIVE_INFINITY);
        card.setMinWidth(Double.NEGATIVE_INFINITY);
        card.setSpacing(10.0);
        card.setPadding(new Insets(0, 0, 0, 10.0));

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
        textBox.setPrefHeight(76.0);
        textBox.setPrefWidth(133.0);
        textBox.setSpacing(5.0);
        textBox.setPadding(new Insets(0, 10.0, 0, 10.0));
        textBox.setAlignment(Pos.CENTER);

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
        dateLabelText.setPrefWidth(122.0);
        dateLabelText.setTextAlignment(TextAlignment.CENTER);
        dateLabelText.setTextFill(javafx.scene.paint.Color.WHITE);
        dateLabelText.setWrapText(true);
        dateLabelText.setFont(new Font(10.0));

        textBox.getChildren().addAll(titleLabel, dateLabelText);
        card.getChildren().addAll(coverImage, textBox);

        LOGGER.info("Created book card for book_id: " + bookId);
        return card;
    }

    private HBox createDraftCard(int bookId, String title, String coverPath, int chapterNumber, String date) {
        HBox card = new HBox();
        card.setAlignment(Pos.CENTER);
        card.setStyle("-fx-background-color: #F28888; -fx-border-color: white; -fx-border-radius: 5; -fx-background-radius: 5;");
        card.setPrefHeight(105.0);
        card.setPrefWidth(270.0);
        card.setMaxHeight(Double.NEGATIVE_INFINITY);
        card.setMaxWidth(Double.NEGATIVE_INFINITY);
        card.setMinHeight(Double.NEGATIVE_INFINITY);
        card.setMinWidth(Double.NEGATIVE_INFINITY);
        card.setSpacing(10.0);
        card.setPadding(new Insets(0, 0, 0, 8.0));

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

        VBox textBox = new VBox();
        textBox.setStyle("-fx-background-color: transparent;");
        textBox.setMaxHeight(Double.NEGATIVE_INFINITY);
        textBox.setMaxWidth(Double.NEGATIVE_INFINITY);
        textBox.setMinHeight(Double.NEGATIVE_INFINITY);
        textBox.setMinWidth(Double.NEGATIVE_INFINITY);
        textBox.setPrefHeight(76.0);
        textBox.setPrefWidth(133.0);
        textBox.setSpacing(5.0);
        textBox.setPadding(new Insets(0, 10.0, 0, 10.0));
        textBox.setAlignment(Pos.TOP_LEFT);

        Label titleLabel = new Label(title);
        titleLabel.setMaxHeight(Double.NEGATIVE_INFINITY);
        titleLabel.setMaxWidth(Double.NEGATIVE_INFINITY);
        titleLabel.setMinWidth(Double.NEGATIVE_INFINITY);
        titleLabel.setPrefHeight(37.0);
        titleLabel.setPrefWidth(122.0);
        titleLabel.setTextFill(javafx.scene.paint.Color.WHITE);
        titleLabel.setWrapText(true);
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 12.0));

        Label dateLabel = new Label("Updated on " + date);
        dateLabel.setPrefHeight(24.0);
        dateLabel.setPrefWidth(122.0);
        dateLabel.setTextAlignment(TextAlignment.LEFT);
        dateLabel.setTextFill(javafx.scene.paint.Color.WHITE);
        dateLabel.setWrapText(true);
        dateLabel.setFont(new Font(10.0));

        Button openDraftButton = new Button("Chapter " + chapterNumber);
        openDraftButton.setMaxHeight(Double.NEGATIVE_INFINITY);
        openDraftButton.setMaxWidth(Double.NEGATIVE_INFINITY);
        openDraftButton.setMinHeight(Double.NEGATIVE_INFINITY);
        openDraftButton.setMinWidth(Double.NEGATIVE_INFINITY);
        openDraftButton.setPrefHeight(18.0);
        openDraftButton.setPrefWidth(80.0);
        openDraftButton.setStyle("-fx-border-radius: 5; -fx-border-color: #fff;-fx-text-fill: black;");
        openDraftButton.setTextFill(javafx.scene.paint.Color.WHITE);
        openDraftButton.setFont(new Font(8.0));
        openDraftButton.setOnAction(event -> openDraft(bookId, chapterNumber));

        textBox.getChildren().addAll(titleLabel, dateLabel, openDraftButton);
        card.getChildren().addAll(coverImage, textBox);

        LOGGER.info("Created draft card for book_id: " + bookId + ", chapter: " + chapterNumber);
        return card;
    }

    private void openBook(int bookId) {
        if (mainController == null) {
            showAlert("Error", "Navigation failed: mainController is null");
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/đọc_book.fxml"));
            Parent readBookPage = loader.load();
            read_book__c readBookController = loader.getController();
            readBookController.setBookId(bookId);
            mainController.loadFXML(String.valueOf(readBookPage));
            Stage stage = (Stage) my_work_my_draft__vbox.getScene().getWindow();
            stage.setResizable(true);
        } catch (IOException e) {
            LOGGER.severe("Failed to open book: " + e.getMessage());
            showAlert("Error", "Failed to open book: " + e.getMessage());
        }
    }

    @FXML
    private void handle_open_draft(ActionEvent event) {
        showAlert("Info", "Please click the specific chapter button in the draft card to open a draft.");
    }

    private void openDraft(int bookId, int chapterNumber) {
        if (mainController == null) {
            showAlert("Error", "Navigation failed: mainController is null");
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/scribble/draft_edit.fxml"));
            Parent draftEditPage = loader.load();
            mainController.loadFXML(String.valueOf(draftEditPage));
            Stage stage = (Stage) my_work_my_draft__vbox.getScene().getWindow();
            stage.setResizable(true);
        } catch (IOException e) {
            LOGGER.severe("Failed to open draft: " + e.getMessage());
            showAlert("Error", "Failed to open draft: " + e.getMessage());
        }
    }

    @FXML
    private void handle_history_library(ActionEvent event) {
        if (mainController == null) {
            showAlert("Error", "Navigation failed: mainController is null");
            return;
        }
        try {
            mainController.loadFXML("history_library.fxml");
            Stage stage = (Stage) history_library_button.getScene().getWindow();
            stage.setResizable(true);
        } catch (Exception e) {
            LOGGER.severe("Failed to navigate to history_library: " + e.getMessage());
            showAlert("Error", "Failed to navigate to history & library: " + e.getMessage());
        }
    }

    @FXML
    private void handle_my_work_my_draft(ActionEvent event) {
        LOGGER.info("My Works & My Drafts button clicked, already on this page");
    }

    @FXML
    private void handle_colab_sent_received(ActionEvent event) {
        if (mainController == null) {
            showAlert("Error", "Navigation failed: mainController is null");
            return;
        }
        try {
            mainController.loadFXML("colab_sent_received.fxml");
            Stage stage = (Stage) colab_sent_received_button.getScene().getWindow();
            stage.setResizable(true);
        } catch (Exception e) {
            LOGGER.severe("Failed to navigate to colab_sent_received: " + e.getMessage());
            showAlert("Error", "Failed to navigate to collaboration sent & received: " + e.getMessage());
        }
    }

    @FXML
    private void handle_groups_joined_owned(ActionEvent event) {
        if (mainController == null) {
            showAlert("Error", "Navigation failed: mainController is null");
            return;
        }
        try {
            mainController.loadFXML("groups_joined_owned.fxml");
            Stage stage = (Stage) groups_joined_owned_button.getScene().getWindow();
            stage.setResizable(true);
        } catch (Exception e) {
            LOGGER.severe("Failed to navigate to groups_joined_owned: " + e.getMessage());
            showAlert("Error", "Failed to navigate to groups joined & owned: " + e.getMessage());
        }
    }

    @Override
    public void setMainController(nav_bar__c mainController) {
        this.mainController = mainController;
        LOGGER.info("setMainController called with: " + (mainController != null ? "set" : "null"));
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}