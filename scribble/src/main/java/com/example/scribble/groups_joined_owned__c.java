package com.example.scribble;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.text.Font;
import javafx.geometry.Insets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.io.IOException;
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

        //count of joined groups
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

        // Count Owned groups
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
            LOGGER.warning("total_joined_label is null. Check FXML fx:id.");
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
        

        groupBox.setPrefSize(270, 105);
        groupBox.setMaxSize(270, 105);
        groupBox.setMinSize(270, 105);
        groupBox.setStyle("-fx-background-color: #F28888; -fx-background-radius: 5; -fx-border-color: #fff; -fx-border-radius: 5;");

        ImageView imageView = new ImageView();
        imageView.setFitHeight(76.0);
        imageView.setFitWidth(50.0);
        imageView.setPickOnBounds(true);
        imageView.setPreserveRatio(true);
        // Load book cover similar to colab_sent_received__c
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

        VBox textBox = new VBox();
        textBox.setPrefHeight(76.0);
        textBox.setPrefWidth(133.0);
        textBox.setSpacing(5.0);
        textBox.setPadding(new Insets(0, 10, 0, 10));

        Label bookNameLabel = new Label(bookName);
        bookNameLabel.setTextFill(javafx.scene.paint.Color.WHITE);
        bookNameLabel.setPrefHeight(37.0);
        bookNameLabel.setPrefWidth(122.0);
        bookNameLabel.setWrapText(true);

        Label subtitleLabel = new Label(subtitle);
        subtitleLabel.setTextFill(javafx.scene.paint.Color.WHITE);
        subtitleLabel.setFont(new Font(10.0));

        Button button = new Button(buttonText);
        button.setPrefHeight(22.0);
        button.setPrefWidth(83.0);
        button.setStyle("-fx-border-radius: 5; -fx-border-color: 1; -fx-background-color: #fff");
        button.setFont(new Font(10.0));
        VBox.setMargin(button, new Insets(5.0, 0, 0, 0));
        button.setUserData(groupId);
        button.setId(isJoined ? "joined_status" : "owned_status");

        textBox.getChildren().addAll(bookNameLabel, subtitleLabel, button);
        groupBox.getChildren().addAll(imageView, textBox);

        return groupBox;
    }

    @FXML
    private void handle_joined_group(javafx.event.ActionEvent event) {
        Button button = (Button) event.getSource();
        Integer groupId = (Integer) button.getUserData();
        if (groupId != null) {
            navigateToGroupDetails(groupId);
        } else {
            System.err.println("No group ID found for joined group button.");
        }
    }

    @FXML
    private void handle_owned_group(javafx.event.ActionEvent event) {

    }

    private void navigateToGroupDetails(int groupId) {

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