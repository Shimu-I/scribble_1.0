package com.example.scribble;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.geometry.Insets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Logger;

public class groups_joined_owned__c {
    private static final Logger LOGGER = Logger.getLogger(groups_joined_owned__c.class.getName());

    @FXML
    private VBox groupJoinedContainer;

    @FXML
    private VBox groupOwnedContainer;

    @FXML
    private Label joinedLabel; // Not used due to missing fx:id in FXML

    @FXML
    private Label ownedLabel; // Not used due to missing fx:id in FXML

    @FXML
    private Label total_joined_record; // Renamed to match FXML

    @FXML
    private Label total_owned_record; // Renamed to match FXML

    private int joinedCount;
    private int ownedCount;

    @FXML
    private void initialize() {
        if (groupJoinedContainer == null || groupOwnedContainer == null) {
            System.err.println("Error: groupJoinedContainer or groupOwnedContainer is null. Check FXML fx:id.");
        } else {
            System.out.println("groupJoinedContainer and groupOwnedContainer initialized successfully.");
        }

        LOGGER.info("groupJoinedContainer and groupOwnedContainer initialized successfully.");
        fetchGroupCounts();
        updateLabels();
        loadJoinedGroups();
        loadOwnedGroups();

        LOGGER.info("Record counts: Joined (" + joinedCount + "), Owned (" + ownedCount + ")");

        System.out.println("Joined Groups: " + joinedCount);
        System.out.println("Owned Groups: " + ownedCount);
    }

    private void fetchGroupCounts() {
        int userId = UserSession.getInstance().getUserId();
        LOGGER.info("Fetching counts for user ID: " + userId);

        // Count of joined groups
        String joinedQuery = "SELECT COUNT(DISTINCT gm.group_id) AS joined_count " +
                "FROM group_members gm " +
                "JOIN community_groups cg ON gm.group_id = cg.group_id " +
                "WHERE gm.user_id = ? " +
                "UNION " +
                "SELECT COUNT(*) FROM community_groups cg " +
                "WHERE cg.group_id = 21 AND cg.admin_id = ?";
        try (Connection conn = db_connect.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(joinedQuery)) {
            pstmt.setInt(1, userId);
            pstmt.setInt(2, userId);
            ResultSet rs = pstmt.executeQuery();
            joinedCount = 0;
            while (rs.next()) {
                joinedCount += rs.getInt("joined_count");
            }
            LOGGER.info("Joined groups count: " + joinedCount);
        } catch (SQLException e) {
            LOGGER.severe("SQL error fetching joined groups count: " + e.getMessage() + ", SQLState: " + e.getSQLState());
            e.printStackTrace();
        }

        // Count owned groups
        String ownedQuery = "SELECT COUNT(*) AS owned_count " +
                "FROM community_groups cg " +
                "WHERE cg.admin_id = ?";
        try (Connection conn = db_connect.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(ownedQuery)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                ownedCount = rs.getInt("owned_count");
                LOGGER.info("Owned groups count: " + ownedCount);
            } else {
                LOGGER.info("Owned groups count query returned no results.");
            }
        } catch (SQLException e) {
            LOGGER.severe("SQL error fetching owned groups count: " + e.getMessage());
        }
    }

    private void updateLabels() {
        if (total_joined_record != null) {
            total_joined_record.setText("(" + joinedCount + ")");
        } else {
            LOGGER.warning("total_joined_record is null. Check FXML fx:id.");
        }
        if (total_owned_record != null) {
            total_owned_record.setText("(" + ownedCount + ")");
        } else {
            LOGGER.warning("total_owned_record is null. Check FXML fx:id.");
        }
        LOGGER.info("Updated labels: Joined (" + joinedCount + "), Owned (" + ownedCount + ")");
    }

    private void loadJoinedGroups() {
        groupJoinedContainer.getChildren().clear();
        int userId = UserSession.getInstance().getUserId();
        System.out.println("Loading joined groups for user ID: " + userId);

        // Ensure the user is a member of the default group (ID 21)
        ensureDefaultGroupMembership(userId);

        String query = "SELECT DISTINCT cg.group_id, cg.group_name, COALESCE(b.title, cg.group_name) COLLATE utf8mb4_0900_ai_ci AS book_name, " +
                "COUNT(gm2.user_id) AS member_count, b.cover_photo " +
                "FROM group_members gm " +
                "JOIN community_groups cg ON gm.group_id = cg.group_id " +
                "LEFT JOIN books b ON cg.book_id = b.book_id " +
                "LEFT JOIN group_members gm2 ON cg.group_id = gm2.group_id " +
                "WHERE gm.user_id = ? " +
                "GROUP BY cg.group_id, cg.group_name, b.title, b.cover_photo " +
                "UNION " +
                "SELECT cg.group_id, cg.group_name, COALESCE(b.title, cg.group_name) COLLATE utf8mb4_0900_ai_ci AS book_name, " +
                "COUNT(gm2.user_id) AS member_count, b.cover_photo " +
                "FROM community_groups cg " +
                "LEFT JOIN books b ON cg.book_id = b.book_id " +
                "LEFT JOIN group_members gm2 ON cg.group_id = gm2.group_id " +
                "WHERE cg.group_id = 21 AND cg.admin_id = ? " +
                "GROUP BY cg.group_id, cg.group_name, b.title, b.cover_photo " +
                "ORDER BY group_id DESC";
        try (Connection conn = db_connect.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, userId);
            pstmt.setInt(2, userId);
            ResultSet rs = pstmt.executeQuery();
            int rowCount = 0;
            while (rs.next()) {
                rowCount++;
                int groupId = rs.getInt("group_id");
                String groupName = rs.getString("group_name");
                String bookName = rs.getString("book_name");
                int memberCount = rs.getInt("member_count");
                String coverPhoto = rs.getString("cover_photo");
                // Format subtitle to show member count
                String subtitle = memberCount + (memberCount == 1 ? " person joined" : " people joined");
                // Log all fields to debug
                System.out.println("Found joined group: ID=" + groupId +
                        ", Group Name=" + groupName +
                        ", Book=" + (bookName != null ? bookName : "null") +
                        ", Members=" + subtitle +
                        ", Cover=" + (coverPhoto != null ? coverPhoto : "null"));
                HBox groupBox = createGroupBox(bookName, subtitle, "View Group", groupId, true, coverPhoto);
                groupJoinedContainer.getChildren().add(groupBox);
            }
            System.out.println("Total joined groups loaded: " + rowCount);
            if (rowCount == 0) {
                Label noGroupsLabel = new Label("No joined groups found.");
                noGroupsLabel.setTextFill(javafx.scene.paint.Color.GRAY);
                noGroupsLabel.setFont(new Font(14.0));
                groupJoinedContainer.getChildren().add(noGroupsLabel);
                System.out.println("No joined groups found for user ID: " + userId);
            }
        } catch (SQLException e) {
            System.err.println("SQL error loading joined groups: " + e.getMessage() + ", SQLState: " + e.getSQLState());
            e.printStackTrace();
        }
    }

    private void ensureDefaultGroupMembership(int userId) {
        String checkQuery = "SELECT COUNT(*) AS count FROM group_members WHERE group_id = 21 AND user_id = ?";
        String insertQuery = "INSERT INTO group_members (group_id, user_id) VALUES (21, ?)";
        try (Connection conn = db_connect.getConnection();
             PreparedStatement checkStmt = conn.prepareStatement(checkQuery)) {
            checkStmt.setInt(1, userId);
            ResultSet rs = checkStmt.executeQuery();
            if (rs.next() && rs.getInt("count") == 0) {
                try (PreparedStatement insertStmt = conn.prepareStatement(insertQuery)) {
                    insertStmt.setInt(1, userId);
                    int rowsAffected = insertStmt.executeUpdate();
                    LOGGER.info("Added user ID " + userId + " to default group ID 21, rows affected: " + rowsAffected);
                }
            } else {
                LOGGER.info("User ID " + userId + " is already a member of default group ID 21");
            }
        } catch (SQLException e) {
            LOGGER.severe("Error ensuring default group membership: " + e.getMessage() + ", SQLState: " + e.getSQLState());
            e.printStackTrace();
        }
    }

    private void loadOwnedGroups() {
        groupOwnedContainer.getChildren().clear();
        int userId = UserSession.getInstance().getUserId();
        System.out.println("Loading owned groups for user ID: " + userId);

        String query = "SELECT cg.group_id, b.title AS book_name, COUNT(gm.user_id) AS member_count, b.cover_photo " +
                "FROM community_groups cg " +
                "JOIN books b ON cg.book_id = b.book_id " +
                "LEFT JOIN group_members gm ON cg.group_id = gm.group_id " +
                "WHERE cg.admin_id = ? " +
                "GROUP BY cg.group_id, b.title, b.cover_photo ORDER BY cg.created_at DESC";

        try (Connection conn = db_connect.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            int rowCount = 0;
            while (rs.next()) {
                rowCount++;
                String bookName = rs.getString("book_name");
                int memberCount = rs.getInt("member_count");
                String subtitle = memberCount + (memberCount == 1 ? " person joined" : " people joined");
                String coverPhoto = rs.getString("cover_photo");
                int groupId = rs.getInt("group_id");
                System.out.println("Found owned group: ID=" + groupId + ", Book=" + bookName + ", Members=" + subtitle + ", Cover=" + (coverPhoto != null ? coverPhoto : "null"));
                HBox groupBox = createGroupBox(bookName, subtitle, "Owned", groupId, false, coverPhoto);
                groupOwnedContainer.getChildren().add(groupBox);
            }
            System.out.println("Total owned groups loaded: " + rowCount);
            if (rowCount == 0) {
                System.out.println("No owned groups found for user ID: " + userId);
            }
        } catch (SQLException e) {
            System.err.println("SQL error loading owned groups: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private HBox createGroupBox(String bookName, String subtitle, String buttonText, int groupId, boolean isJoined, String coverPhoto) {
        HBox groupBox = new HBox();
        groupBox.setAlignment(javafx.geometry.Pos.CENTER);
        groupBox.setStyle("-fx-background-color: #F28888; -fx-background-radius: 5; -fx-border-color: #fff; -fx-border-radius: 5; -fx-padding: 5;");
        groupBox.setPrefHeight(105.0);
        groupBox.setPrefWidth(270.0);
        groupBox.setMaxHeight(Double.NEGATIVE_INFINITY);
        groupBox.setMaxWidth(Double.NEGATIVE_INFINITY);
        groupBox.setMinHeight(Double.NEGATIVE_INFINITY);
        groupBox.setMinWidth(Double.NEGATIVE_INFINITY);

        Region spacer = new Region();
        spacer.setPrefWidth(26.0);
        spacer.setPrefHeight(104.0);

        ImageView imageView = new ImageView();
        imageView.setFitHeight(76.0);
        imageView.setFitWidth(50.0);
        imageView.setPickOnBounds(true);
        imageView.setPreserveRatio(true);
        if (coverPhoto != null && !coverPhoto.isEmpty()) {
            try {
                imageView.setImage(new Image(getClass().getResource("/images/book_covers/" + coverPhoto).toExternalForm()));
                LOGGER.info("Loaded book cover: " + coverPhoto);
            } catch (NullPointerException e) {
                LOGGER.warning("Book cover not found: " + coverPhoto);
                imageView.setImage(loadImage("/images/book_covers/hollow_rectangle.png"));
            }
        } else {
            imageView.setImage(loadImage("/images/book_covers/hollow_rectangle.png"));
            LOGGER.info("Loaded default book cover: hollow_rectangle.png");
        }
        HBox.setMargin(imageView, new Insets(5, 5, 5, 5));

        VBox textBox = new VBox();
        textBox.setPrefHeight(76.0);
        textBox.setPrefWidth(141.0);
        textBox.setSpacing(5.0);
        textBox.setPadding(new Insets(0, 10, 0, 10));
        HBox.setMargin(textBox, new Insets(5, 5, 5, 5));
        HBox.setHgrow(textBox, javafx.scene.layout.Priority.ALWAYS);

        Label bookNameLabel = new Label(bookName);
        bookNameLabel.setTextFill(javafx.scene.paint.Color.WHITE);
        bookNameLabel.setPrefHeight(37.0);
        bookNameLabel.setPrefWidth(122.0);
        bookNameLabel.setWrapText(true);
        bookNameLabel.setFont(Font.font("System", FontWeight.BOLD, 12.0));

        Label subtitleLabel = new Label(subtitle);
        subtitleLabel.setTextFill(javafx.scene.paint.Color.WHITE);
        subtitleLabel.setFont(new Font(9.0));
        subtitleLabel.setPrefHeight(24.0);
        subtitleLabel.setPrefWidth(123.0);
        subtitleLabel.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
        subtitleLabel.setWrapText(true);

        Button actionButton = new Button(buttonText);
        actionButton.setPrefHeight(18.0);
        actionButton.setPrefWidth(80.0);
        actionButton.setStyle("-fx-border-radius: 5; -fx-border-color: #fff; -fx-background-color: #fff;");
        actionButton.setFont(new Font(9.0));
        actionButton.setUserData(groupId);
        actionButton.setId(isJoined ? "joined_status" : "owned_status");
        actionButton.setOnAction(isJoined ? this::handle_joined_group : this::handle_owned_group);

        textBox.getChildren().addAll(bookNameLabel, subtitleLabel, actionButton);

        VBox deleteButtonBox = new VBox();
        deleteButtonBox.setAlignment(javafx.geometry.Pos.TOP_RIGHT);
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
        deleteButton.setOnAction(e -> deleteGroupRecord(groupId, isJoined));

        deleteButtonBox.getChildren().add(deleteButton);

        groupBox.getChildren().addAll(spacer, imageView, textBox, deleteButtonBox);

        return groupBox;
    }

    private void deleteGroupRecord(int groupId, boolean isJoined) {
        int userId = UserSession.getInstance().getUserId();
        try (Connection conn = db_connect.getConnection()) {
            if (isJoined) {
                // Delete from group_members for joined groups
                String deleteQuery = "DELETE FROM group_members WHERE group_id = ? AND user_id = ?";
                try (PreparedStatement stmt = conn.prepareStatement(deleteQuery)) {
                    stmt.setInt(1, groupId);
                    stmt.setInt(2, userId);
                    int rowsAffected = stmt.executeUpdate();
                    if (rowsAffected > 0) {
                        LOGGER.info("Deleted membership for group_id " + groupId + " and user_id " + userId);
                        loadJoinedGroups();
                        fetchGroupCounts();
                        updateLabels();
                        showAlert("Success", "Left the group successfully.");
                    } else {
                        LOGGER.warning("No membership found for group_id " + groupId + " and user_id " + userId);
                        showAlert("Error", "No membership found to delete.");
                    }
                }
            } else {
                // Delete from community_groups for owned groups
                String deleteQuery = "DELETE FROM community_groups WHERE group_id = ? AND admin_id = ?";
                try (PreparedStatement stmt = conn.prepareStatement(deleteQuery)) {
                    stmt.setInt(1, groupId);
                    stmt.setInt(2, userId);
                    int rowsAffected = stmt.executeUpdate();
                    if (rowsAffected > 0) {
                        LOGGER.info("Deleted owned group with group_id " + groupId + " for admin_id " + userId);
                        loadOwnedGroups();
                        fetchGroupCounts();
                        updateLabels();
                        showAlert("Success", "Group deleted successfully.");
                    } else {
                        LOGGER.warning("No owned group found for group_id " + groupId + " and admin_id " + userId);
                        showAlert("Error", "No group found to delete or you are not the owner.");
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.severe("Failed to delete group record: " + e.getMessage());
            showAlert("Error", "Failed to delete group record: " + e.getMessage());
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    private void handle_joined_group(ActionEvent event) {
        Button button = (Button) event.getSource();
        Integer groupId = (Integer) button.getUserData();
        if (groupId != null) {
            navigateToGroupDetails(groupId);
        } else {
            System.err.println("No group ID found for joined group button.");
        }
    }

    @FXML
    private void handle_owned_group(ActionEvent event) {
        // Implement navigation or other logic for owned groups if needed
    }

    private void navigateToGroupDetails(int groupId) {
        // Implement navigation to group details page if needed
    }

    private Image loadImage(String path) {
        try {
            Image image = new Image(getClass().getResource(path).toExternalForm());
            if (image.isError()) {
                LOGGER.warning("Image load error for path: " + path);
                return new Image(getClass().getResource("/images/book_covers/hollow_rectangle.png").toExternalForm());
            }
            LOGGER.info("Successfully loaded image: " + path);
            return image;
        } catch (NullPointerException | IllegalArgumentException e) {
            LOGGER.warning("Failed to load image: " + path + ", using fallback: " + e.getMessage());
            return new Image(getClass().getResource("/images/book_covers/hollow_rectangle.png").toExternalForm());
        }
    }
}