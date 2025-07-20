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
import javafx.scene.layout.Region;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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
        myWorkContainer.setAlignment(Pos.TOP_CENTER);
        myWorkContainer.setPrefHeight(289.0);
        myWorkContainer.setPrefWidth(307.0);
        myWorkContainer.setSpacing(10.0);
        myDraftContainer.setStyle("-fx-background-color: #005D4D;");
        myDraftContainer.setAlignment(Pos.TOP_CENTER);
        myDraftContainer.setPrefHeight(289.0);
        myDraftContainer.setPrefWidth(307.0);
        myDraftContainer.setSpacing(10.0);
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
        card.setStyle("-fx-background-color: #F28888; -fx-background-radius: 5; -fx-border-color: #fff; -fx-border-radius: 5; -fx-padding: 5;");
        card.setPrefHeight(105.0);
        card.setPrefWidth(270.0);
        card.setMaxHeight(Double.NEGATIVE_INFINITY);
        card.setMaxWidth(Double.NEGATIVE_INFINITY);
        card.setMinHeight(Double.NEGATIVE_INFINITY);
        card.setMinWidth(Double.NEGATIVE_INFINITY);

        Region spacer = new Region();
        spacer.setPrefWidth(26.0);
        spacer.setPrefHeight(104.0);

        ImageView coverImage = new ImageView();
        coverImage.setFitWidth(50.0);
        coverImage.setFitHeight(76.0);
        coverImage.setPreserveRatio(true);
        coverImage.setPickOnBounds(true);
        if (coverPath != null && !coverPath.isEmpty()) {
            try {
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

        VBox textBox = new VBox();
        textBox.setPrefHeight(76.0);
        textBox.setPrefWidth(141.0);
        textBox.setSpacing(5.0);
        textBox.setPadding(new Insets(0, 10, 0, 10));
        HBox.setMargin(textBox, new Insets(5, 5, 5, 5));
        HBox.setHgrow(textBox, javafx.scene.layout.Priority.ALWAYS);

        Label titleLabel = new Label(title);
        titleLabel.setPrefHeight(37.0);
        titleLabel.setPrefWidth(122.0);
        titleLabel.setTextFill(javafx.scene.paint.Color.WHITE);
        titleLabel.setWrapText(true);
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 12.0));

        Label dateLabelText = new Label(dateLabel + " " + date);
        dateLabelText.setPrefHeight(24.0);
        dateLabelText.setPrefWidth(123.0);
        dateLabelText.setTextAlignment(TextAlignment.CENTER);
        dateLabelText.setTextFill(javafx.scene.paint.Color.WHITE);
        dateLabelText.setWrapText(true);
        dateLabelText.setFont(new Font(9.0));

        textBox.getChildren().addAll(titleLabel, dateLabelText);

        VBox deleteButtonBox = new VBox();
        deleteButtonBox.setAlignment(Pos.TOP_RIGHT);
        deleteButtonBox.setPrefHeight(104.0);
        deleteButtonBox.setPrefWidth(22.0);
        deleteButtonBox.setPadding(new Insets(5.0, 0, 0, 0));

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
        deleteButton.setUserData(bookId);
        deleteButton.setOnAction(e -> deleteRecord(bookId, true, 0));

        deleteButtonBox.getChildren().add(deleteButton);

        card.getChildren().addAll(spacer, coverImage, textBox, deleteButtonBox);

        LOGGER.info("Created book card for book_id: " + bookId);
        return card;
    }

    private HBox createDraftCard(int bookId, String title, String coverPath, int chapterNumber, String date) {
        HBox card = new HBox();
        card.setAlignment(Pos.CENTER);
        card.setStyle("-fx-background-color: #F28888; -fx-background-radius: 5; -fx-border-color: #fff; -fx-border-radius: 5; -fx-padding: 5;");
        card.setPrefHeight(105.0);
        card.setPrefWidth(270.0);
        card.setMaxHeight(Double.NEGATIVE_INFINITY);
        card.setMaxWidth(Double.NEGATIVE_INFINITY);
        card.setMinHeight(Double.NEGATIVE_INFINITY);
        card.setMinWidth(Double.NEGATIVE_INFINITY);

        Region spacer = new Region();
        spacer.setPrefWidth(26.0);
        spacer.setPrefHeight(104.0);

        ImageView coverImage = new ImageView();
        coverImage.setFitWidth(50.0);
        coverImage.setFitHeight(76.0);
        coverImage.setPreserveRatio(true);
        coverImage.setPickOnBounds(true);
        if (coverPath != null && !coverPath.isEmpty()) {
            try {
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
        HBox.setMargin(coverImage, new Insets(5, 5, 5, 5));

        VBox textBox = new VBox();
        textBox.setPrefHeight(76.0);
        textBox.setPrefWidth(141.0);
        textBox.setSpacing(5.0);
        textBox.setPadding(new Insets(0, 10, 0, 10));
        HBox.setMargin(textBox, new Insets(5, 5, 5, 5));
        HBox.setHgrow(textBox, javafx.scene.layout.Priority.ALWAYS);

        Label titleLabel = new Label(title);
        titleLabel.setPrefHeight(37.0);
        titleLabel.setPrefWidth(122.0);
        titleLabel.setTextFill(javafx.scene.paint.Color.WHITE);
        titleLabel.setWrapText(true);
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 12.0));

        Label dateLabel = new Label("Updated on " + date);
        dateLabel.setPrefHeight(24.0);
        dateLabel.setPrefWidth(123.0);
        dateLabel.setTextAlignment(TextAlignment.CENTER);
        dateLabel.setTextFill(javafx.scene.paint.Color.WHITE);
        dateLabel.setWrapText(true);
        dateLabel.setFont(new Font(9.0));

        Button openDraftButton = new Button("Chapter " + chapterNumber);
        openDraftButton.setPrefHeight(18.0);
        openDraftButton.setPrefWidth(80.0);
        openDraftButton.setStyle("-fx-border-radius: 5; -fx-border-color: #fff; -fx-background-color: #fff;");
        openDraftButton.setFont(new Font(9.0));
        openDraftButton.setOnAction(event -> openDraft(bookId, chapterNumber));

        textBox.getChildren().addAll(titleLabel, dateLabel, openDraftButton);

        VBox deleteButtonBox = new VBox();
        deleteButtonBox.setAlignment(Pos.TOP_RIGHT);
        deleteButtonBox.setPrefHeight(104.0);
        deleteButtonBox.setPrefWidth(22.0);
        deleteButtonBox.setPadding(new Insets(5.0, 0, 0, 0));

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
        deleteButton.setUserData(new int[]{bookId, chapterNumber});
        deleteButton.setOnAction(e -> deleteRecord(bookId, false, chapterNumber));

        deleteButtonBox.getChildren().add(deleteButton);

        card.getChildren().addAll(spacer, coverImage, textBox, deleteButtonBox);

        LOGGER.info("Created draft card for book_id: " + bookId + ", chapter: " + chapterNumber);
        return card;
    }



    private void deleteRecord(int bookId, boolean isWork, int chapterNumber) {
        try (Connection conn = db_connect.getConnection()) {
            if (isWork) {
                // Fetch book title and check if the user is the owner
                String title = "Unknown Book";
                String role = "";
                try (PreparedStatement titleStmt = conn.prepareStatement(
                        "SELECT b.title, ba.role FROM books b JOIN book_authors ba ON b.book_id = ba.book_id WHERE b.book_id = ? AND ba.user_id = ?")) {
                    titleStmt.setInt(1, bookId);
                    titleStmt.setInt(2, userId);
                    ResultSet rs = titleStmt.executeQuery();
                    if (rs.next()) {
                        title = rs.getString("title");
                        role = rs.getString("role");
                    } else {
                        LOGGER.warning("No record found for book_id " + bookId + " and user_id " + userId);
                        showAlert("Error", "No record found for this book and user.");
                        return;
                    }
                } catch (SQLException e) {
                    LOGGER.warning("Failed to fetch book title or role for book_id " + bookId + ": " + e.getMessage());
                    showAlert("Error", "Failed to fetch book details: " + e.getMessage());
                    return;
                }

                if (!role.equals("Owner")) {
                    // Handle co-author removal (as in the previous implementation)
                    int chapterCount = 0;
                    int draftCount = 0;
                    try (PreparedStatement chapterStmt = conn.prepareStatement(
                            "SELECT COUNT(*) FROM chapters WHERE book_id = ? AND author_id = ?")) {
                        chapterStmt.setInt(1, bookId);
                        chapterStmt.setInt(2, userId);
                        ResultSet rs = chapterStmt.executeQuery();
                        if (rs.next()) {
                            chapterCount = rs.getInt(1);
                        }
                    } catch (SQLException e) {
                        LOGGER.warning("Failed to check chapters for book_id " + bookId + ": " + e.getMessage());
                    }

                    try (PreparedStatement draftStmt = conn.prepareStatement(
                            "SELECT COUNT(*) FROM draft_chapters WHERE book_id = ? AND author_id = ?")) {
                        draftStmt.setInt(1, bookId);
                        draftStmt.setInt(2, userId);
                        ResultSet rs = draftStmt.executeQuery();
                        if (rs.next()) {
                            draftCount = rs.getInt(1);
                        }
                    } catch (SQLException e) {
                        LOGGER.warning("Failed to check draft chapters for book_id " + bookId + ": " + e.getMessage());
                    }

                    String warning = "";
                    if (chapterCount > 0 || draftCount > 0) {
                        warning = "\nWARNING: You have " + chapterCount + " published chapter(s) and " + draftCount + " draft chapter(s), which will be deleted.";
                    }
                    Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
                    confirmAlert.setTitle("Confirm Deletion");
                    confirmAlert.setHeaderText("Remove Co-Author from Book");
                    confirmAlert.setContentText("Are you sure you want to remove yourself as a co-author from '" + title + "'?" + warning);
                    Optional<ButtonType> result = confirmAlert.showAndWait();
                    if (result.isEmpty() || result.get() != ButtonType.OK) {
                        LOGGER.info("Co-author deletion cancelled for book_id " + bookId);
                        return;
                    }

                    String deleteQuery = "DELETE FROM book_authors WHERE book_id = ? AND user_id = ? AND role = 'Co-Author'";
                    try (PreparedStatement stmt = conn.prepareStatement(deleteQuery)) {
                        stmt.setInt(1, bookId);
                        stmt.setInt(2, userId);
                        int rowsAffected = stmt.executeUpdate();
                        if (rowsAffected > 0) {
                            LOGGER.info("Deleted co-author record for book_id " + bookId + " and user_id " + userId);
                            // Notify book owner
                            try (PreparedStatement notifyStmt = conn.prepareStatement(
                                    "INSERT INTO notifications (user_id, book_id, action_type, action_user_id, action_id, message) " +
                                            "SELECT ba.user_id, ?, 'CollaborationRequest', ?, 0, " +
                                            "CONCAT((SELECT username FROM users WHERE user_id = ?), ' has been removed as a co-author from your book ', " +
                                            "(SELECT title FROM books WHERE book_id = ?)) " +
                                            "FROM book_authors ba WHERE ba.book_id = ? AND ba.role = 'Owner'")) {
                                notifyStmt.setInt(1, bookId);
                                notifyStmt.setInt(2, userId);
                                notifyStmt.setInt(3, userId);
                                notifyStmt.setInt(4, bookId);
                                notifyStmt.setInt(5, bookId);
                                notifyStmt.executeUpdate();
                                LOGGER.info("Notification sent to book owner for co-author removal, book_id: " + bookId);
                            }
                            populateMyWorks();
                            int[] counts = getRecordCounts();
                            total_my_works_record.setText("(" + counts[0] + ")");
                            showAlert("Success", "You have been removed as a co-author from '" + title + "'.");
                        } else {
                            LOGGER.warning("No co-author record found for book_id " + bookId + " and user_id " + userId);
                            showAlert("Error", "No co-author record found to delete.");
                        }
                    }
                } else {
                    // Handle book deletion by owner
                    int chapterCount = 0;
                    int coAuthorCount = 0;
                    int groupCount = 0;
                    try (PreparedStatement chapterStmt = conn.prepareStatement(
                            "SELECT COUNT(*) FROM chapters WHERE book_id = ?")) {
                        chapterStmt.setInt(1, bookId);
                        ResultSet rs = chapterStmt.executeQuery();
                        if (rs.next()) {
                            chapterCount = rs.getInt(1);
                        }
                    }
                    try (PreparedStatement coAuthorStmt = conn.prepareStatement(
                            "SELECT COUNT(*) FROM book_authors WHERE book_id = ? AND role = 'Co-Author'")) {
                        coAuthorStmt.setInt(1, bookId);
                        ResultSet rs = coAuthorStmt.executeQuery();
                        if (rs.next()) {
                            coAuthorCount = rs.getInt(1);
                        }
                    }
                    try (PreparedStatement groupStmt = conn.prepareStatement(
                            "SELECT COUNT(*) FROM community_groups WHERE book_id = ?")) {
                        groupStmt.setInt(1, bookId);
                        ResultSet rs = groupStmt.executeQuery();
                        if (rs.next()) {
                            groupCount = rs.getInt(1);
                        }
                    }

                    String warning = "\nWARNING: This will delete the book, its " + chapterCount + " chapter(s), " +
                            coAuthorCount + " co-author(s), and " + groupCount + " associated group(s). This action cannot be undone.";
                    Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
                    confirmAlert.setTitle("Confirm Book Deletion");
                    confirmAlert.setHeaderText("Delete Book");
                    confirmAlert.setContentText("Are you sure you want to delete the book '" + title + "' and all its associated data?" + warning);
                    Optional<ButtonType> result = confirmAlert.showAndWait();
                    if (result.isEmpty() || result.get() != ButtonType.OK) {
                        LOGGER.info("Book deletion cancelled for book_id " + bookId);
                        return;
                    }

                    // Delete associated community groups (due to ON DELETE RESTRICT)
                    try (PreparedStatement deleteGroupsStmt = conn.prepareStatement(
                            "DELETE FROM community_groups WHERE book_id = ?")) {
                        deleteGroupsStmt.setInt(1, bookId);
                        deleteGroupsStmt.executeUpdate();
                        LOGGER.info("Deleted community groups for book_id " + bookId);
                    }

                    // Fetch co-authors and group members to notify
                    List<Integer> coAuthorIds = new ArrayList<>();
                    try (PreparedStatement coAuthorStmt = conn.prepareStatement(
                            "SELECT user_id FROM book_authors WHERE book_id = ? AND role = 'Co-Author'")) {
                        coAuthorStmt.setInt(1, bookId);
                        ResultSet rs = coAuthorStmt.executeQuery();
                        while (rs.next()) {
                            coAuthorIds.add(rs.getInt("user_id"));
                        }
                    }
                    List<Integer> groupMemberIds = new ArrayList<>();
                    try (PreparedStatement groupMemberStmt = conn.prepareStatement(
                            "SELECT DISTINCT gm.user_id FROM group_members gm JOIN community_groups cg ON gm.group_id = cg.group_id WHERE cg.book_id = ?")) {
                        groupMemberStmt.setInt(1, bookId);
                        ResultSet rs = groupMemberStmt.executeQuery();
                        while (rs.next()) {
                            groupMemberIds.add(rs.getInt("user_id"));
                        }
                    }

                    // Delete the book
                    String deleteQuery = "DELETE FROM books WHERE book_id = ?";
                    try (PreparedStatement stmt = conn.prepareStatement(deleteQuery)) {
                        stmt.setInt(1, bookId);
                        int rowsAffected = stmt.executeUpdate();
                        if (rowsAffected > 0) {
                            LOGGER.info("Deleted book record for book_id " + bookId);

                            // Notify co-authors
                            for (int coAuthorId : coAuthorIds) {
                                try (PreparedStatement notifyStmt = conn.prepareStatement(
                                        "INSERT INTO notifications (user_id, book_id, action_type, action_user_id, action_id, message) " +
                                                "VALUES (?, ?, 'CollaborationRequest', ?, 0, CONCAT((SELECT username FROM users WHERE user_id = ?), ' deleted the book ', ?))")) {
                                    notifyStmt.setInt(1, coAuthorId);
                                    notifyStmt.setInt(2, bookId);
                                    notifyStmt.setInt(3, userId);
                                    notifyStmt.setInt(4, userId);
                                    notifyStmt.setString(5, title);
                                    notifyStmt.executeUpdate();
                                    LOGGER.info("Notification sent to co-author user_id " + coAuthorId + " for book deletion, book_id: " + bookId);
                                }
                            }

                            // Notify group members
                            for (int memberId : groupMemberIds) {
                                if (memberId != userId && !coAuthorIds.contains(memberId)) {
                                    try (PreparedStatement notifyStmt = conn.prepareStatement(
                                            "INSERT INTO notifications (user_id, book_id, action_type, action_user_id, action_id, message) " +
                                                    "VALUES (?, ?, 'GroupMessage', ?, 0, CONCAT((SELECT username FROM users WHERE user_id = ?), ' deleted the book ', ?))")) {
                                        notifyStmt.setInt(1, memberId);
                                        notifyStmt.setInt(2, bookId);
                                        notifyStmt.setInt(3, userId);
                                        notifyStmt.setInt(4, userId);
                                        notifyStmt.setString(5, title);
                                        notifyStmt.executeUpdate();
                                        LOGGER.info("Notification sent to group member user_id " + memberId + " for book deletion, book_id: " + bookId);
                                    }
                                }
                            }

                            populateMyWorks();
                            int[] counts = getRecordCounts();
                            total_my_works_record.setText("(" + counts[0] + ")");
                            showAlert("Success", "Book '" + title + "' and all associated data have been deleted.");
                        } else {
                            LOGGER.warning("No book record found for book_id " + bookId);
                            showAlert("Error", "No book record found to delete.");
                        }
                    }
                }
            } else {
                // Existing draft deletion logic
                String deleteQuery = "DELETE FROM draft_chapters WHERE book_id = ? AND author_id = ? AND chapter_number = ?";
                try (PreparedStatement stmt = conn.prepareStatement(deleteQuery)) {
                    stmt.setInt(1, bookId);
                    stmt.setInt(2, userId);
                    stmt.setInt(3, chapterNumber);
                    int rowsAffected = stmt.executeUpdate();
                    if (rowsAffected > 0) {
                        LOGGER.info("Deleted draft record for book_id " + bookId + ", chapter_number " + chapterNumber + " and author_id " + userId);
                        populateMyDrafts();
                        int[] counts = getRecordCounts();
                        total_my_drafts_record.setText("(" + counts[1] + ")");
                        showAlert("Success", "Draft chapter removed.");
                    } else {
                        LOGGER.warning("No draft record found for book_id " + bookId + ", chapter_number " + chapterNumber + " and author_id " + userId);
                        showAlert("Error", "No draft record found to delete.");
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.severe("Failed to delete record: " + e.getMessage());
            showAlert("Error", "Failed to delete record: " + e.getMessage());
        }
    }

    @FXML
    private void handle_delete(ActionEvent actionEvent) {
        Button button = (Button) actionEvent.getSource();
        Object userData = button.getUserData();
        if (userData instanceof Integer) {
            int bookId = (Integer) userData;
            deleteRecord(bookId, true, 0);
        } else if (userData instanceof int[]) {
            int[] data = (int[]) userData;
            int bookId = data[0];
            int chapterNumber = data[1];
            deleteRecord(bookId, false, chapterNumber);
        } else {
            LOGGER.warning("No valid data found for delete action");
            showAlert("Error", "No valid data selected for deletion.");
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
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
