package com.example.scribble;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Logger;

public class colab_sent_received__c {
    private static final Logger LOGGER = Logger.getLogger(colab_sent_received__c.class.getName());
    private Connection conn;

    @FXML private VBox colabSentContainer;
    @FXML private VBox colabReceivedContainer;
    @FXML private Label total_sent_record;
    @FXML private Label total_received_record;

    private int[] getRecordCounts() {
        int sentCount = 0;
        int receivedCount = 0;
        int currentUserId = UserSession.getInstance().getCurrentUserId();

        try (PreparedStatement stmt = conn.prepareStatement(
                "SELECT COUNT(*) FROM collaboration_invites WHERE inviter_id = ?")) {
            stmt.setInt(1, currentUserId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                sentCount = rs.getInt(1);
            }
        } catch (SQLException e) {
            LOGGER.severe("Failed to count Sent requests: " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to count Sent requests: " + e.getMessage());
        }

        try (PreparedStatement stmt = conn.prepareStatement(
                "SELECT COUNT(*) FROM collaboration_invites WHERE invitee_email = (SELECT email FROM users WHERE user_id = ?)")) {
            stmt.setInt(1, currentUserId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                receivedCount = rs.getInt(1);
            }
        } catch (SQLException e) {
            LOGGER.severe("Failed to count Received requests: " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to count Received requests: " + e.getMessage());
        }

        return new int[]{sentCount, receivedCount};
    }

    @FXML
    public void initialize() {
        if (!UserSession.getInstance().isLoggedIn()) {
            showAlert(Alert.AlertType.WARNING, "Login Required", "Please log in to view requests.");
            return;
        }
        conn = db_connect.getConnection();
        if (conn == null) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to connect to the database.");
            return;
        }

        colabSentContainer.setStyle("-fx-background-color: #005D4D;");
        colabSentContainer.setAlignment(Pos.TOP_CENTER);
        colabSentContainer.setPrefHeight(289.0);
        colabSentContainer.setPrefWidth(307.0);
        colabSentContainer.setSpacing(10.0);
        colabReceivedContainer.setStyle("-fx-background-color: #005D4D;");
        colabReceivedContainer.setAlignment(Pos.TOP_CENTER);
        colabReceivedContainer.setPrefHeight(289.0);
        colabReceivedContainer.setPrefWidth(307.0);
        colabReceivedContainer.setSpacing(10.0);

        int[] counts = getRecordCounts();
        total_sent_record.setText("(" + counts[0] + ")");
        total_received_record.setText("(" + counts[1] + ")");
        LOGGER.info("Record counts: Sent (" + counts[0] + "), Received (" + counts[1] + ")");

        loadSentRequests();
        loadReceivedRequests();
    }

    public void setConnection(Connection conn) {
        this.conn = conn;
    }

    private Image loadBookCoverImage(String coverPath) {
        if (coverPath != null && !coverPath.isEmpty()) {
            try {
                // Try loading from filesystem first
                java.io.File uploadFile = new java.io.File("Uploads/book_covers/" + coverPath);
                if (uploadFile.exists()) {
                    Image image = new Image("file:" + uploadFile.getAbsolutePath(), true);
                    if (!image.isError()) {
                        LOGGER.info("Loaded book cover from filesystem: file:" + uploadFile.getAbsolutePath());
                        return image;
                    } else {
                        LOGGER.warning("Failed to load book cover from filesystem (image error): " + coverPath);
                    }
                } else {
                    // Fall back to classpath
                    java.net.URL resource = getClass().getResource("/images/book_covers/" + coverPath);
                    if (resource != null) {
                        Image image = new Image(resource.toExternalForm(), true);
                        if (!image.isError()) {
                            LOGGER.info("Loaded book cover from classpath: " + resource.toExternalForm());
                            return image;
                        } else {
                            LOGGER.warning("Failed to load book cover from classpath (image error): " + coverPath);
                        }
                    } else {
                        LOGGER.warning("Book cover not found: " + coverPath);
                    }
                }
            } catch (Exception e) {
                LOGGER.severe("Failed to load book cover: " + coverPath + " - " + e.getMessage());
            }
        } else {
            LOGGER.info("No cover path provided, using default book cover");
        }
        // Default to hollow_rectangle.png
        Image defaultImage = new Image(getClass().getResource("/images/book_covers/hollow_rectangle.png").toExternalForm());
        LOGGER.info("Loaded default book cover: hollow_rectangle.png");
        return defaultImage;
    }

    private void loadSentRequests() {
        int currentUserId = UserSession.getInstance().getCurrentUserId();
        try (PreparedStatement stmt = conn.prepareStatement(
                "SELECT ci.invite_id, ci.book_id, ci.invitee_email, ci.status, ci.message, b.title, b.cover_photo " +
                        "FROM collaboration_invites ci LEFT JOIN books b ON ci.book_id = b.book_id " +
                        "WHERE ci.inviter_id = ? ORDER BY ci.created_at DESC")) {
            stmt.setInt(1, currentUserId);
            ResultSet rs = stmt.executeQuery();
            colabSentContainer.getChildren().clear();
            if (!rs.isBeforeFirst()) {
                colabSentContainer.getChildren().add(new Label("No sent requests found."));
            }
            while (rs.next()) {
                int inviteId = rs.getInt("invite_id");
                String title = rs.getString("title") != null ? rs.getString("title") : "Unknown Book";
                String inviteeEmail = rs.getString("invitee_email");
                String status = rs.getString("status");
                String message = rs.getString("message") != null ? rs.getString("message") : "No message provided";
                String coverPath = rs.getString("cover_photo");

                HBox requestHBox = new HBox();
                requestHBox.setAlignment(Pos.CENTER);
                requestHBox.setStyle("-fx-background-color: #F28888; -fx-background-radius: 5; -fx-border-color: #fff; -fx-border-radius: 5; -fx-padding: 5;");
                requestHBox.setPrefSize(270, 105);
                requestHBox.setMaxSize(270, 105);
                requestHBox.setMinSize(270, 105);

                Region spacer = new Region();
                spacer.setPrefWidth(26.0);
                spacer.setPrefHeight(104.0);

                ImageView bookCover = new ImageView();
                bookCover.setFitHeight(76);
                bookCover.setFitWidth(50);
                bookCover.setPreserveRatio(true);
                bookCover.setPickOnBounds(true);
                bookCover.setImage(loadBookCoverImage(coverPath));
                HBox.setMargin(bookCover, new Insets(5, 5, 5, 5));

                VBox details = new VBox(5);
                details.setPrefHeight(76.0);
                details.setPrefWidth(141.0);
                details.setPadding(new Insets(0, 10, 0, 10));
                HBox.setMargin(details, new Insets(5, 5, 5, 5));
                HBox.setHgrow(details, javafx.scene.layout.Priority.ALWAYS);

                Label titleLabel = new Label(title);
                titleLabel.setTextFill(javafx.scene.paint.Color.WHITE);
                titleLabel.setWrapText(true);
                titleLabel.setMaxWidth(122);
                titleLabel.setPrefHeight(37.0);
                titleLabel.setFont(Font.font("System", FontWeight.BOLD, 12.0));

                Label emailLabel = new Label("To: " + inviteeEmail);
                emailLabel.setTextFill(javafx.scene.paint.Color.WHITE);
                emailLabel.setStyle("-fx-font-size: 9;");

                Label statusLabel = new Label("Status: " + status);
                statusLabel.setTextFill(javafx.scene.paint.Color.WHITE);
                statusLabel.setStyle("-fx-font-size: 9;");

                Button editButton = new Button("Edit Message");
                editButton.setStyle("-fx-background-color: #D9D9D9; -fx-border-radius: 5; -fx-font-size: 9;");
                editButton.setPrefSize(80, 18);
                editButton.setOnAction(e -> handleEditMessage(inviteId, message));

                details.getChildren().addAll(titleLabel, emailLabel, statusLabel, editButton);

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
                deleteButton.setUserData(inviteId);
                deleteButton.setOnAction(e -> deleteRecord(inviteId));

                deleteButtonBox.getChildren().add(deleteButton);

                requestHBox.getChildren().addAll(spacer, bookCover, details, deleteButtonBox);
                colabSentContainer.getChildren().add(requestHBox);
            }
        } catch (SQLException e) {
            LOGGER.severe("Error loading sent requests: " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load sent requests: " + e.getMessage());
        }
    }

    private void loadReceivedRequests() {
        int currentUserId = UserSession.getInstance().getCurrentUserId();
        try (PreparedStatement stmt = conn.prepareStatement(
                "SELECT ci.invite_id, ci.book_id, ci.inviter_id, ci.status, ci.message, u.username, u.email, u.profile_picture, b.title, b.cover_photo " +
                        "FROM collaboration_invites ci " +
                        "JOIN users u ON ci.inviter_id = u.user_id " +
                        "LEFT JOIN books b ON ci.book_id = b.book_id " +
                        "WHERE ci.invitee_email = (SELECT email FROM users WHERE user_id = ?) ORDER BY ci.created_at DESC")) {
            stmt.setInt(1, currentUserId);
            ResultSet rs = stmt.executeQuery();
            colabReceivedContainer.getChildren().clear();
            if (!rs.isBeforeFirst()) {
                colabReceivedContainer.getChildren().add(new Label("No received requests found."));
            }
            while (rs.next()) {
                int inviteId = rs.getInt("invite_id");
                int bookId = rs.getInt("book_id");
                int inviterId = rs.getInt("inviter_id");
                String status = rs.getString("status");
                String username = rs.getString("username");
                String title = rs.getString("title") != null ? rs.getString("title") : "Unknown Book";
                String coverPath = rs.getString("cover_photo");

                HBox requestHBox = new HBox();
                requestHBox.setAlignment(Pos.CENTER);
                requestHBox.setStyle("-fx-background-color: #F28888; -fx-background-radius: 5; -fx-border-color: #fff; -fx-border-radius: 5; -fx-padding: 5;");
                requestHBox.setPrefSize(270, 105);
                requestHBox.setMaxSize(270, 105);
                requestHBox.setMinSize(270, 105);

                Region spacer = new Region();
                spacer.setPrefWidth(26.0);
                spacer.setPrefHeight(104.0);

                ImageView bookCover = new ImageView();
                bookCover.setFitHeight(76);
                bookCover.setFitWidth(50);
                bookCover.setPreserveRatio(true);
                bookCover.setPickOnBounds(true);
                bookCover.setImage(loadBookCoverImage(coverPath));
                HBox.setMargin(bookCover, new Insets(5, 5, 5, 5));

                VBox details = new VBox(5);
                details.setPrefHeight(76.0);
                details.setPrefWidth(141.0);
                details.setPadding(new Insets(0, 10, 0, 10));
                HBox.setMargin(details, new Insets(5, 5, 5, 5));
                HBox.setHgrow(details, javafx.scene.layout.Priority.ALWAYS);

                Label titleLabel = new Label(title);
                titleLabel.setTextFill(javafx.scene.paint.Color.WHITE);
                titleLabel.setWrapText(true);
                titleLabel.setMaxWidth(122);
                titleLabel.setPrefHeight(37.0);
                titleLabel.setFont(Font.font("System", FontWeight.BOLD, 12.0));

                Label userLabel = new Label("From: " + username);
                userLabel.setTextFill(javafx.scene.paint.Color.WHITE);
                userLabel.setStyle("-fx-font-size: 9;");

                Button viewRequestButton = new Button("View Request");
                viewRequestButton.setStyle("-fx-border-radius: 5; -fx-border-color: #fff; -fx-background-color: " +
                        (status.equals("Pending") ? "#D9D9D9" : status.equals("Accepted") ? "#4CAF50" : "#F44336") + "; -fx-font-size: 8;");
                viewRequestButton.setPrefSize(80, 18);

                Button editStatusButton = new Button("Edit Status");
                editStatusButton.setStyle("-fx-background-color: #D9D9D9; -fx-border-radius: 5; -fx-font-size: 8;");
                editStatusButton.setPrefSize(80, 18);

                try (PreparedStatement ownerStmt = conn.prepareStatement(
                        "SELECT user_id FROM book_authors WHERE book_id = ? AND role = 'Owner'")) {
                    ownerStmt.setInt(1, bookId);
                    ResultSet ownerRs = ownerStmt.executeQuery();
                    if (ownerRs.next() && ownerRs.getInt("user_id") == currentUserId) {
                        viewRequestButton.setOnAction(e -> handleOpenRequest(inviteId, bookId, inviterId));
                        editStatusButton.setOnAction(e -> handleEditStatus(inviteId, status, bookId, inviterId));
                    } else {
                        viewRequestButton.setDisable(true);
                        editStatusButton.setDisable(true);
                    }
                }

                details.getChildren().addAll(titleLabel, userLabel, viewRequestButton, editStatusButton);

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
                deleteButton.setUserData(inviteId);
                deleteButton.setOnAction(e -> deleteRecord(inviteId));

                deleteButtonBox.getChildren().add(deleteButton);

                requestHBox.getChildren().addAll(spacer, bookCover, details, deleteButtonBox);
                colabReceivedContainer.getChildren().add(requestHBox);
            }
        } catch (SQLException e) {
            LOGGER.severe("Error loading received requests: " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load received requests: " + e.getMessage());
        }
    }


    private void deleteRecord(int inviteId) {
        boolean autoCommit = true;
        try (Connection conn = db_connect.getConnection()) {
            autoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false); // Start transaction

            // Step 1: Retrieve the book_id, inviter_id, and status for the invite
            int bookId = -1;
            int inviterId = -1;
            String status = null;
            try (PreparedStatement stmt = conn.prepareStatement(
                    "SELECT book_id, inviter_id, status FROM collaboration_invites WHERE invite_id = ?")) {
                stmt.setInt(1, inviteId);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    bookId = rs.getInt("book_id");
                    inviterId = rs.getInt("inviter_id");
                    status = rs.getString("status");
                } else {
                    LOGGER.warning("No collaboration invite found for invite_id " + inviteId);
                    showAlert(Alert.AlertType.ERROR, "Error", "No collaboration request found to delete.");
                    conn.rollback();
                    return;
                }
            }

            // Step 2: If the status is "Accepted", remove the co-author from book_authors
            if ("Accepted".equals(status)) {
                try (PreparedStatement checkStmt = conn.prepareStatement(
                        "SELECT 1 FROM book_authors WHERE book_id = ? AND user_id = ? AND role = 'Co-Author'")) {
                    checkStmt.setInt(1, bookId);
                    checkStmt.setInt(2, inviterId);
                    ResultSet rs = checkStmt.executeQuery();
                    if (rs.next()) {
                        // Co-author exists, proceed to delete
                        try (PreparedStatement deleteStmt = conn.prepareStatement(
                                "DELETE FROM book_authors WHERE book_id = ? AND user_id = ? AND role = 'Co-Author'")) {
                            deleteStmt.setInt(1, bookId);
                            deleteStmt.setInt(2, inviterId);
                            int rowsAffected = deleteStmt.executeUpdate();
                            if (rowsAffected > 0) {
                                LOGGER.info("Removed co-author for book_id " + bookId + " and user_id " + inviterId);
                                // Update works_created_count
                                try (PreparedStatement updateStmt = conn.prepareStatement(
                                        "UPDATE users SET works_created_count = GREATEST(works_created_count - 1, 0) WHERE user_id = ?")) {
                                    updateStmt.setInt(1, inviterId);
                                    updateStmt.executeUpdate();
                                }
                            }
                        }
                    }
                }
            }

            // Step 3: Delete the collaboration invite
            try (PreparedStatement stmt = conn.prepareStatement(
                    "DELETE FROM collaboration_invites WHERE invite_id = ?")) {
                stmt.setInt(1, inviteId);
                int rowsAffected = stmt.executeUpdate();
                if (rowsAffected > 0) {
                    LOGGER.info("Deleted collaboration invite with invite_id " + inviteId);
                    // Step 4: Commit the transaction
                    conn.commit();
                    // Step 5: Refresh UI and counts
                    loadSentRequests();
                    loadReceivedRequests();
                    int[] counts = getRecordCounts();
                    total_sent_record.setText("(" + counts[0] + ")");
                    total_received_record.setText("(" + counts[1] + ")");
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Collaboration request deleted successfully.");
                } else {
                    LOGGER.warning("No collaboration invite found for invite_id " + inviteId);
                    showAlert(Alert.AlertType.ERROR, "Error", "No collaboration request found to delete.");
                    conn.rollback();
                    return;
                }
            }
        } catch (SQLException e) {
            LOGGER.severe("Failed to delete collaboration invite: " + e.getMessage());
            try {
                conn.rollback();
                LOGGER.info("Transaction rolled back due to error.");
            } catch (SQLException rollbackEx) {
                LOGGER.severe("Error during rollback: " + rollbackEx.getMessage());
            }
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete collaboration request: " + e.getMessage());
        } finally {
            try {
                conn.setAutoCommit(autoCommit);
            } catch (SQLException e) {
                LOGGER.severe("Error restoring auto-commit: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handle_delete(ActionEvent actionEvent) {
        Button button = (Button) actionEvent.getSource();
        Object userData = button.getUserData();
        if (userData instanceof Integer) {
            int inviteId = (Integer) userData;
            deleteRecord(inviteId);
        } else {
            LOGGER.warning("No valid data found for delete action");
            showAlert(Alert.AlertType.ERROR, "Error", "No valid data selected for deletion.");
        }
    }

    private void handleOpenRequest(int inviteId, int bookId, int inviterId) {
        try (PreparedStatement stmt = conn.prepareStatement(
                "SELECT ci.message, u.username, u.email, u.profile_picture " +
                        "FROM collaboration_invites ci JOIN users u ON ci.inviter_id = u.user_id " +
                        "WHERE ci.invite_id = ?")) {
            stmt.setInt(1, inviteId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String message = rs.getString("message") != null ? rs.getString("message") : "No message provided";
                String username = rs.getString("username");
                String email = rs.getString("email");
                String profilePicture = rs.getString("profile_picture");

                Stage popupStage = new Stage();
                popupStage.initModality(Modality.APPLICATION_MODAL);
                popupStage.setTitle("Collaboration Request Details");

                VBox vbox = new VBox(10);
                vbox.setAlignment(Pos.CENTER);
                Label usernameLabel = new Label("Username: " + username);
                usernameLabel.setStyle("-fx-font-size: 14; -fx-font-weight: bold;");
                Label emailLabel = new Label("Email: " + email);
                emailLabel.setStyle("-fx-font-size: 12;");
                ImageView profileImageView = new ImageView();
                profileImageView.setFitWidth(100);
                profileImageView.setFitHeight(100);
                profileImageView.setImage(loadImage(profilePicture != null && !profilePicture.isEmpty() ?
                        "/images/profiles/" + profilePicture : "/images/profiles/demo_profile.png"));
                Label messageLabel = new Label("Message: " + message);
                messageLabel.setWrapText(true);
                messageLabel.setMaxWidth(300);
                messageLabel.setStyle("-fx-font-size: 12;");

                Button closeButton = new Button("Close");
                closeButton.setStyle("-fx-background-color: #D9D9D9; -fx-border-radius: 5;");
                closeButton.setOnAction(e -> popupStage.close());

                vbox.getChildren().addAll(profileImageView, usernameLabel, emailLabel, messageLabel, closeButton);
                vbox.setPadding(new Insets(10));

                Scene scene = new Scene(vbox, 400, 300);
                popupStage.setScene(scene);
                popupStage.show();
            }
        } catch (SQLException e) {
            LOGGER.severe("Error fetching request details: " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load request details: " + e.getMessage());
        }
    }

    private void handleEditMessage(int inviteId, String currentMessage) {
        Stage popupStage = new Stage();
        popupStage.initModality(Modality.APPLICATION_MODAL);
        popupStage.setTitle("Edit Collaboration Request Message");

        VBox vbox = new VBox(10);
        vbox.setAlignment(Pos.CENTER);
        Label messageLabel = new Label("Edit Message:");
        messageLabel.setStyle("-fx-font-size: 14; -fx-font-weight: bold;");
        TextArea messageArea = new TextArea(currentMessage);
        messageArea.setWrapText(true);
        messageArea.setPrefSize(300, 100);
        Button saveButton = new Button("Save");
        saveButton.setStyle("-fx-background-color: #4CAF50; -fx-border-radius: 5; -fx-text-fill: white;");
        saveButton.setOnAction(e -> {
            try (PreparedStatement stmt = conn.prepareStatement(
                    "UPDATE collaboration_invites SET message = ? WHERE invite_id = ?")) {
                stmt.setString(1, messageArea.getText());
                stmt.setInt(2, inviteId);
                stmt.executeUpdate();
                loadSentRequests();
                showAlert(Alert.AlertType.INFORMATION, "Success", "Message updated successfully!");
                popupStage.close();
            } catch (SQLException ex) {
                LOGGER.severe("Error updating message: " + ex.getMessage());
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to update message: " + ex.getMessage());
            }
        });
        vbox.getChildren().addAll(messageLabel, messageArea, saveButton);
        vbox.setPadding(new Insets(10));

        Scene scene = new Scene(vbox, 350, 200);
        popupStage.setScene(scene);
        popupStage.show();
    }

    private void handleEditStatus(int inviteId, String currentStatus, int bookId, int inviterId) {
        Stage popupStage = new Stage();
        popupStage.initModality(Modality.APPLICATION_MODAL);
        popupStage.setTitle("Edit Collaboration Request Status");

        VBox vbox = new VBox(10);
        vbox.setAlignment(Pos.CENTER);
        Label statusLabel = new Label("Edit Status:");
        statusLabel.setStyle("-fx-font-size: 14; -fx-font-weight: bold;");
        ChoiceBox<String> statusChoiceBox = new ChoiceBox<>();
        statusChoiceBox.getItems().addAll("Pending", "Accepted", "Declined");
        statusChoiceBox.setValue(currentStatus);
        Button saveButton = new Button("Save");
        saveButton.setStyle("-fx-background-color: #4CAF50; -fx-border-radius: 5; -fx-text-fill: white;");
        saveButton.setOnAction(e -> {
            String newStatus = statusChoiceBox.getValue();
            if (currentStatus.equals("Accepted") && (newStatus.equals("Declined") || newStatus.equals("Pending"))) {
                removeCoAuthor(bookId, inviterId);
            }
            try (PreparedStatement stmt = conn.prepareStatement(
                    "UPDATE collaboration_invites SET status = ? WHERE invite_id = ?")) {
                stmt.setString(1, newStatus);
                stmt.setInt(2, inviteId);
                stmt.executeUpdate();
                loadSentRequests();
                loadReceivedRequests();
                showAlert(Alert.AlertType.INFORMATION, "Success", "Status updated to " + newStatus);
                popupStage.close();
                if ("Accepted".equals(newStatus)) {
                    addCoAuthor(bookId, inviterId);
                }
            } catch (SQLException ex) {
                LOGGER.severe("Error updating status: " + ex.getMessage());
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to update status: " + ex.getMessage());
            }
        });
        vbox.getChildren().addAll(statusLabel, statusChoiceBox, saveButton);
        vbox.setPadding(new Insets(10));

        Scene scene = new Scene(vbox, 300, 150);
        popupStage.setScene(scene);
        popupStage.show();
    }

    private void updateInviteStatus(int inviteId, int bookId, int inviterId, String status) {
        boolean autoCommit = true;
        try {
            autoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);

            try (PreparedStatement checkStmt = conn.prepareStatement(
                    "SELECT status FROM collaboration_invites WHERE invite_id = ?")) {
                checkStmt.setInt(1, inviteId);
                ResultSet rs = checkStmt.executeQuery();
                String currentStatus = rs.next() ? rs.getString("status") : null;
                if (currentStatus != null && currentStatus.equals("Accepted") &&
                        (status.equals("Declined") || status.equals("Pending"))) {
                    removeCoAuthor(bookId, inviterId);
                }
            }

            try (PreparedStatement stmt = conn.prepareStatement(
                    "UPDATE collaboration_invites SET status = ? WHERE invite_id = ?")) {
                stmt.setString(1, status);
                stmt.setInt(2, inviteId);
                int rowsAffected = stmt.executeUpdate();
                if (rowsAffected == 0) {
                    throw new SQLException("No collaboration invite found for invite_id: " + inviteId);
                }
                if (status.equals("Accepted")) {
                    addCoAuthor(bookId, inviterId);
                }
            }

            conn.commit();
            loadSentRequests();
            loadReceivedRequests();
            showAlert(Alert.AlertType.INFORMATION, "Success", "Request status updated to " + status);
        } catch (SQLException e) {
            try {
                conn.rollback();
                LOGGER.severe("Rolled back transaction due to error: " + e.getMessage());
            } catch (SQLException rollbackEx) {
                LOGGER.severe("Error during rollback: " + rollbackEx.getMessage());
            }
            LOGGER.severe("Error updating invite status: " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to update request status: " + e.getMessage());
        } finally {
            try {
                conn.setAutoCommit(autoCommit);
            } catch (SQLException e) {
                LOGGER.severe("Error restoring auto-commit: " + e.getMessage());
            }
        }
    }

    private void addCoAuthor(int bookId, int userId) {
        try (PreparedStatement checkStmt = conn.prepareStatement(
                "SELECT 1 FROM book_authors WHERE book_id = ? AND user_id = ?")) {
            checkStmt.setInt(1, bookId);
            checkStmt.setInt(2, userId);
            if (checkStmt.executeQuery().next()) {
                showAlert(Alert.AlertType.WARNING, "Duplicate", "User is already a co-author.");
                return;
            }
        } catch (SQLException e) {
            LOGGER.severe("Error checking co-author: " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to check co-author status: " + e.getMessage());
            return;
        }

        try (PreparedStatement stmt = conn.prepareStatement(
                "INSERT INTO book_authors (book_id, user_id, role, created_at) VALUES (?, ?, 'Co-Author', NOW())")) {
            stmt.setInt(1, bookId);
            stmt.setInt(2, userId);
            stmt.executeUpdate();

            try (PreparedStatement updateStmt = conn.prepareStatement(
                    "UPDATE users SET works_created_count = works_created_count + 1 WHERE user_id = ?")) {
                updateStmt.setInt(1, userId);
                updateStmt.executeUpdate();
            }
            showAlert(Alert.AlertType.INFORMATION, "Success", "User added as co-author!");
        } catch (SQLException e) {
            LOGGER.severe("Error adding co-author: " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to add co-author: " + e.getMessage());
        }
    }

    private void removeCoAuthor(int bookId, int userId) {
        try (PreparedStatement checkStmt = conn.prepareStatement(
                "SELECT 1 FROM book_authors WHERE book_id = ? AND user_id = ? AND role = 'Co-Author'")) {
            checkStmt.setInt(1, bookId);
            checkStmt.setInt(2, userId);
            if (!checkStmt.executeQuery().next()) {
                return;
            }
        } catch (SQLException e) {
            LOGGER.severe("Error checking co-author: " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to check co-author status: " + e.getMessage());
            return;
        }

        try (PreparedStatement stmt = conn.prepareStatement(
                "DELETE FROM book_authors WHERE book_id = ? AND user_id = ? AND role = 'Co-Author'")) {
            stmt.setInt(1, bookId);
            stmt.setInt(2, userId);
            stmt.executeUpdate();

            try (PreparedStatement updateStmt = conn.prepareStatement(
                    "UPDATE users SET works_created_count = GREATEST(works_created_count - 1, 0) WHERE user_id = ?")) {
                updateStmt.setInt(1, userId);
                updateStmt.executeUpdate();
            }
            showAlert(Alert.AlertType.INFORMATION, "Success", "User removed as co-author.");
        } catch (SQLException e) {
            LOGGER.severe("Error removing co-author: " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to remove co-author: " + e.getMessage());
        }
    }

    private Image loadImage(String path) {
        try {
            Image image = new Image(getClass().getResource(path).toExternalForm());
            if (image.isError()) {
                LOGGER.warning("Image load error for path: " + path + ", using fallback");
                return new Image(getClass().getResource("/images/profiles/demo_profile.png").toExternalForm());
            }
            LOGGER.info("Successfully loaded image: " + path);
            return image;
        } catch (NullPointerException | IllegalArgumentException e) {
            LOGGER.warning("Failed to load image: " + path + ", using fallback. Error: " + e.getMessage());
            return new Image(getClass().getResource("/images/profiles/demo_profile.png").toExternalForm());
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
