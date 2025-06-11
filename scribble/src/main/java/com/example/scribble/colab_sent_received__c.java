package com.example.scribble;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
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
    @FXML private Label total_sent_record; // Added to match FXML
    @FXML private Label total_received_record; // Added to match FXML


    // New method to get counts
    private int[] getRecordCounts() {
        int sentCount = 0;
        int receivedCount = 0;
        int currentUserId = UserSession.getInstance().getCurrentUserId();

        // Count Sent requests
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

        // Count Received requests
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

        // Get and set record counts
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

    private void loadSentRequests() {
        int currentUserId = UserSession.getInstance().getCurrentUserId();
        try (PreparedStatement stmt = conn.prepareStatement(
                "SELECT ci.invite_id, ci.book_id, ci.invitee_email, ci.status, ci.message, b.title, b.cover_photo " +
                        "FROM collaboration_invites ci LEFT JOIN books b ON ci.book_id = b.book_id " +
                        "WHERE ci.inviter_id = ?")) {
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
                String coverPath = rs.getString("cover_photo"); // Fetch cover_photo

                HBox requestHBox = new HBox(10);
                requestHBox.setStyle("-fx-background-color: #005D4D; -fx-background-radius: 5; -fx-padding: 5;");
                requestHBox.setAlignment(Pos.CENTER_LEFT);
                requestHBox.setStyle("-fx-background-color: #005D4D; -fx-background-radius: 5; -fx-border-color: #fff; -fx-border-radius: 5; -fx-padding: 0 0 0 40;");
                requestHBox.setPrefSize(270, 105);
                requestHBox.setMaxSize(270, 105);
                requestHBox.setMinSize(270, 105);




                ImageView bookCover = new ImageView();
                bookCover.setFitHeight(76);
                bookCover.setFitWidth(50);
                // Use same image loading logic as my_works_my_drafts__c
                if (coverPath != null && !coverPath.isEmpty()) {
                    try {
                        bookCover.setImage(new Image(getClass().getResource("/images/book_covers/" + coverPath).toExternalForm()));
                        LOGGER.info("Loaded book cover: " + coverPath);
                    } catch (NullPointerException e) {
                        LOGGER.warning("Book cover not found: " + coverPath);
                        bookCover.setImage(loadImage("/images/book_covers/hollow_rectangle.png"));
                    }
                } else {
                    bookCover.setImage(loadImage("/images/book_covers/hollow_rectangle.png"));
                    LOGGER.info("Loaded default book cover: hollow_rectangle.png");
                }
                VBox details = new VBox(5);
                Label titleLabel = new Label(title);
                titleLabel.setTextFill(javafx.scene.paint.Color.WHITE);
                titleLabel.setWrapText(true);
                titleLabel.setMaxWidth(122);
                titleLabel.setFont(Font.font("System", FontWeight.BOLD, 13));

                Label emailLabel = new Label("To: " + inviteeEmail);
                emailLabel.setTextFill(javafx.scene.paint.Color.WHITE);
                emailLabel.setStyle("-fx-font-size: 10;");
                Label statusLabel = new Label("Status: " + status);
                statusLabel.setTextFill(javafx.scene.paint.Color.WHITE);
                statusLabel.setStyle("-fx-font-size: 10;");
                Button editButton = new Button("Edit Message");
                editButton.setStyle("-fx-background-color: #D9D9D9; -fx-border-radius: 5; -fx-font-size: 10;");
                editButton.setPrefSize(83, 22);
                editButton.setOnAction(e -> handleEditMessage(inviteId, message));
                details.getChildren().addAll(titleLabel, emailLabel, statusLabel, editButton);
                details.setPadding(new javafx.geometry.Insets(0, 10, 0, 10));
                requestHBox.getChildren().addAll(bookCover, details);
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
                        "WHERE ci.invitee_email = (SELECT email FROM users WHERE user_id = ?)")) {
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
                String coverPath = rs.getString("cover_photo"); // Fetch cover_photo


                HBox requestHBox = new HBox(10);
                requestHBox.setStyle("-fx-background-color: #005D4D; -fx-background-radius: 5; -fx-border-color: #fff; -fx-border-radius: 5; -fx-padding: 5;");
                requestHBox.setAlignment(Pos.CENTER_LEFT);
                requestHBox.setStyle("-fx-background-color: #005D4D; -fx-background-radius: 5; -fx-border-color: #fff; -fx-border-radius: 5; -fx-padding: 0 0 0 40;");
                requestHBox.setPrefSize(270, 105);
                requestHBox.setMaxSize(270, 105);
                requestHBox.setMinSize(270, 105);


                ImageView bookCover = new ImageView();
                bookCover.setFitHeight(76);
                bookCover.setFitWidth(50);
                // Use same image loading logic as my_works_my_drafts__c
                if (coverPath != null && !coverPath.isEmpty()) {
                    try {
                        bookCover.setImage(new Image(getClass().getResource("/images/book_covers/" + coverPath).toExternalForm()));
                        LOGGER.info("Loaded book cover: " + coverPath);
                    } catch (NullPointerException e) {
                        LOGGER.warning("Book cover not found: " + coverPath);
                        bookCover.setImage(loadImage("/images/book_covers/hollow_rectangle.png"));
                    }
                } else {
                    bookCover.setImage(loadImage("/images/book_covers/hollow_rectangle.png"));
                    LOGGER.info("Loaded default book cover: hollow_rectangle.png");
                }
                VBox details = new VBox(5);
                Label titleLabel = new Label(title);
                titleLabel.setTextFill(javafx.scene.paint.Color.WHITE);
                titleLabel.setWrapText(true);
                titleLabel.setMaxWidth(122);
                titleLabel.setFont(Font.font("System", FontWeight.BOLD, 13));

                Label userLabel = new Label("From: " + username);
                userLabel.setTextFill(javafx.scene.paint.Color.WHITE);
                userLabel.setStyle("-fx-font-size: 10;");
                Button viewRequestButton = new Button("View Request");
                viewRequestButton.setStyle("-fx-border-radius: 5; -fx-border-color: #fff; -fx-background-color: " +
                        (status.equals("Pending") ? "#D9D9D9" : status.equals("Accepted") ? "#4CAF50" : "#F44336") + "; -fx-font-size: 10;");
                viewRequestButton.setPrefSize(83, 22);
                Button editStatusButton = new Button("Edit Status");
                editStatusButton.setStyle("-fx-background-color: #D9D9D9; -fx-border-radius: 5; -fx-font-size: 10;");
                editStatusButton.setPrefSize(83, 22);
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
                details.setPadding(new javafx.geometry.Insets(0, 10, 0, 10));
                requestHBox.getChildren().addAll(bookCover, details);
                colabReceivedContainer.getChildren().add(requestHBox);
            }
        } catch (SQLException e) {
            LOGGER.severe("Error loading received requests: " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load received requests: " + e.getMessage());
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
                vbox.setAlignment(javafx.geometry.Pos.CENTER);
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
                vbox.setPadding(new javafx.geometry.Insets(10));

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
        vbox.setAlignment(javafx.geometry.Pos.CENTER);
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
        vbox.setPadding(new javafx.geometry.Insets(10));

        Scene scene = new Scene(vbox, 350, 200);
        popupStage.setScene(scene);
        popupStage.show();
    }

    private void handleEditStatus(int inviteId, String currentStatus, int bookId, int inviterId) {
        Stage popupStage = new Stage();
        popupStage.initModality(Modality.APPLICATION_MODAL);
        popupStage.setTitle("Edit Collaboration Request Status");

        VBox vbox = new VBox(10);
        vbox.setAlignment(javafx.geometry.Pos.CENTER);
        Label statusLabel = new Label("Edit Status:");
        statusLabel.setStyle("-fx-font-size: 14; -fx-font-weight: bold;");
        javafx.scene.control.ChoiceBox<String> statusChoiceBox = new javafx.scene.control.ChoiceBox<>();
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
        vbox.setPadding(new javafx.geometry.Insets(10));

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
                return; // User is not a co-author, no action needed
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