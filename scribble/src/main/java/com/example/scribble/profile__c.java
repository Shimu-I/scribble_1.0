package com.example.scribble;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class profile__c {

    @FXML
    public ImageView cover_photo;
    @FXML
    public Label user_name;
    @FXML
    public Button edit_profile;
    @FXML
    public Label user_email;
    @FXML
    public Label joined_at;
    @FXML
    public TextField earning_amount;
    @FXML
    public HBox historyContainer;
    @FXML
    public HBox savedContainer;
    @FXML
    public HBox workContainer;
    @FXML
    public VBox draftContrainer;
    @FXML
    public VBox colabContainer;
    @FXML
    public VBox join_groupContainer;
    @FXML
    private Button back_button;

    @FXML
    private nav_bar__c mainController;

    private Pane editOverlay;
    private List<Node> originalChildren;
    private double mouseX, mouseY;
    private int userId; // Logged-in user's ID from UserSession

    @FXML
    public void initialize() {
        userId = UserSession.getInstance().getUserId();
        if (userId == 0) {
            showAlert("Error", "No user logged in");
            return;
        }
        earning_amount.setEditable(false); // Ensure read-only
        loadUserProfile();
        populateHistory();
        populateSavedBooks();
        populateUserWorks();
        populateDrafts();
        populateColabRequests();
        populateJoinedGroups();
        System.out.println("profile__c initialized for userId: " + userId);
    }

    private void loadUserProfile() {
        try (Connection conn = db_connect.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT username, email, profile_picture, created_at, " +
                             "(SELECT SUM(amount) FROM support WHERE author_id = ?) AS total_earnings " +
                             "FROM users WHERE user_id = ?")) {
            stmt.setInt(1, userId);
            stmt.setInt(2, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                user_name.setText(rs.getString("username"));
                user_email.setText(rs.getString("email"));
                String profilePic = rs.getString("profile_picture");
                if (profilePic != null && !profilePic.isEmpty()) {
                    cover_photo.setImage(new Image("file:src/main/resources/images/profiles/" + profilePic));
                }
                Timestamp createdAt = rs.getTimestamp("created_at");
                if (createdAt != null) {
                    joined_at.setText("Member since " + new SimpleDateFormat("dd MMMM yyyy").format(createdAt));
                }
                double earnings = rs.getDouble("total_earnings");
                if (rs.wasNull()) earnings = 0.0;
                earning_amount.setText(String.format("%.2f", earnings));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load user profile: " + e.getMessage());
        }
    }

    private void populateHistory() {
        historyContainer.getChildren().clear(); // Clear placeholder
        try (Connection conn = db_connect.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT b.book_id, b.title, b.cover_photo, bv.visited_at " +
                             "FROM book_visits bv JOIN books b ON bv.book_id = b.book_id " +
                             "WHERE bv.user_id = ? ORDER BY bv.visited_at DESC LIMIT 5")) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                int bookId = rs.getInt("book_id");
                String title = rs.getString("title");
                String coverPath = rs.getString("cover_photo");
                VBox bookCard = createBookCard(bookId, title, coverPath);
                historyContainer.getChildren().add(bookCard);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load history: " + e.getMessage());
        }
    }

    private void populateSavedBooks() {
        savedContainer.getChildren().clear(); // Clear placeholder
        try (Connection conn = db_connect.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT b.book_id, b.title, b.cover_photo " +
                             "FROM reading_list rl JOIN books b ON rl.listed_book_id = b.book_id " +
                             "WHERE rl.reader_id = ? AND rl.reading_status = 'SavedForLater'")) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                int bookId = rs.getInt("book_id");
                String title = rs.getString("title");
                String coverPath = rs.getString("cover_photo");
                VBox bookCard = createBookCard(bookId, title, coverPath);
                savedContainer.getChildren().add(bookCard);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load saved books: " + e.getMessage());
        }
    }

    private void populateUserWorks() {
        workContainer.getChildren().clear(); // Clear placeholder
        try (Connection conn = db_connect.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT b.book_id, b.title, b.cover_photo " +
                             "FROM book_authors ba JOIN books b ON ba.book_id = b.book_id " +
                             "WHERE ba.user_id = ?")) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                int bookId = rs.getInt("book_id");
                String title = rs.getString("title");
                String coverPath = rs.getString("cover_photo");
                VBox bookCard = createBookCard(bookId, title, coverPath);
                workContainer.getChildren().add(bookCard);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load user works: " + e.getMessage());
        }
    }

    private VBox createBookCard(int bookId, String title, String coverPath) {
        VBox bookCard = new VBox(5);
        bookCard.setAlignment(javafx.geometry.Pos.CENTER);
        bookCard.setStyle("-fx-background-color: #005D4D; -fx-background-radius: 5; -fx-padding: 5;");
        bookCard.setPrefHeight(144.0);
        bookCard.setPrefWidth(96.0);

        ImageView coverImage = new ImageView();
        coverImage.setFitWidth(65);
        coverImage.setFitHeight(96);
        coverImage.setPreserveRatio(true);
        if (coverPath != null && !coverPath.isEmpty()) {
            try {
                coverImage.setImage(new Image("file:src/main/resources/images/book_covers/" + coverPath));
            } catch (Exception e) {
                coverImage.setImage(new Image("file:src/main/resources/images/book_covers/demo_cover.png"));
            }
        } else {
            coverImage.setImage(new Image("file:src/main/resources/images/book_covers/demo_cover.png"));
        }

        coverImage.setOnMouseClicked(event -> openBook(bookId));

        Label titleLabel = new Label(title);
        titleLabel.setTextFill(javafx.scene.paint.Color.WHITE);
        titleLabel.setStyle("-fx-font-size: 10;");
        titleLabel.setWrapText(true);
        titleLabel.setMaxWidth(80);

        bookCard.getChildren().addAll(coverImage, titleLabel);
        return bookCard;
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
            mainController.loadFXML(String.valueOf(readBookPage)); // Pass Parent directly
            Stage stage = (Stage) historyContainer.getScene().getWindow();
            stage.setWidth(1400);
            stage.setHeight(660);
            stage.setResizable(false);
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to open book: " + e.getMessage());
        }
    }

    private void populateDrafts() {
        draftContrainer.getChildren().clear();
        try (Connection conn = db_connect.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT b.title, dc.chapter_number, dc.updated_at " +
                             "FROM draft_chapters dc JOIN books b ON dc.book_id = b.book_id " +
                             "WHERE dc.author_id = ? ORDER BY dc.updated_at DESC")) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Label label = new Label(rs.getString("title") + " - Chapter " + rs.getInt("chapter_number") +
                        " (Last updated: " + new SimpleDateFormat("dd MMM yyyy").format(rs.getTimestamp("updated_at")) + ")");
                label.setStyle("-fx-padding: 5; -fx-font-size: 14;");
                draftContrainer.getChildren().add(label);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load drafts: " + e.getMessage());
        }
    }

    private void populateColabRequests() {
        colabContainer.getChildren().clear();
        try (Connection conn = db_connect.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT b.title, ci.invitee_email, ci.status, ci.created_at " +
                             "FROM collaboration_invites ci JOIN books b ON ci.book_id = b.book_id " +
                             "WHERE ci.inviter_id = ? OR ci.invitee_email = (SELECT email FROM users WHERE user_id = ?)")) {
            stmt.setInt(1, userId);
            stmt.setInt(2, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Label label = new Label(rs.getString("title") + " - Invite to: " + rs.getString("invitee_email") +
                        " (" + rs.getString("status") + ", Sent: " +
                        new SimpleDateFormat("dd MMM yyyy").format(rs.getTimestamp("created_at")) + ")");
                label.setStyle("-fx-padding: 5; -fx-font-size: 14;");
                colabContainer.getChildren().add(label);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load collaboration requests: " + e.getMessage());
        }
    }

    private void populateJoinedGroups() {
        join_groupContainer.getChildren().clear();
        try (Connection conn = db_connect.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT cg.group_id, gm.joined_at " +
                             "FROM group_members gm JOIN community_groups cg ON gm.group_id = cg.group_id " +
                             "WHERE gm.user_id = ?")) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Label label = new Label("Group " + rs.getInt("group_id") +
                        " (Joined: " + new SimpleDateFormat("dd MMM yyyy").format(rs.getTimestamp("joined_at")) + ")");
                label.setStyle("-fx-padding: 5; -fx-font-size: 14;");
                join_groupContainer.getChildren().add(label);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load joined groups: " + e.getMessage());
        }
    }

    @FXML
    private void handle_edit_profile(ActionEvent actionEvent) {
        AnchorPane parentPane = getParentPane(actionEvent);
        if (parentPane == null) {
            showAlert("Error", "Cannot show edit overlay: parentPane is null");
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/scribble/profile_info_form.fxml"));
            Parent editPage = loader.load();
            profile_info_edit__c editController = loader.getController();
            editController.setParentController(this);
            editController.setUserId(userId);

            editOverlay = new Pane();
            editOverlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.2);");
            editOverlay.getChildren().add(editPage);

            originalChildren = new ArrayList<>(parentPane.getChildren());
            for (Node child : originalChildren) {
                child.setEffect(new GaussianBlur(10.0));
            }

            AnchorPane.setTopAnchor(editPage, 10.0);
            AnchorPane.setLeftAnchor(editPage, 10.0);

            editOverlay.setOnMousePressed(event -> {
                mouseX = event.getSceneX();
                mouseY = event.getSceneY();
            });
            editOverlay.setOnMouseDragged(event -> {
                double deltaX = event.getSceneX() - mouseX;
                double deltaY = event.getSceneY() - mouseY;
                Double currentX = AnchorPane.getLeftAnchor(editPage);
                Double currentY = AnchorPane.getTopAnchor(editPage);
                AnchorPane.setLeftAnchor(editPage, currentX == null ? deltaX : currentX + deltaX);
                AnchorPane.setTopAnchor(editPage, currentY == null ? deltaY : currentY + deltaY);
                mouseX = event.getSceneX();
                mouseY = event.getSceneY();
            });

            parentPane.getChildren().add(editOverlay);
            AnchorPane.setTopAnchor(editOverlay, 0.0);
            AnchorPane.setBottomAnchor(editOverlay, 0.0);
            AnchorPane.setLeftAnchor(editOverlay, 0.0);
            AnchorPane.setRightAnchor(editOverlay, 0.0);
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load profile edit form: " + e.getMessage());
        }
    }

    @FXML
    private void handle_edit_photo(ActionEvent actionEvent) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));
        File file = fileChooser.showOpenDialog(edit_profile.getScene().getWindow());
        if (file != null) {
            try {
                String profileDir = "src/main/resources/images/profiles/";
                Files.createDirectories(Paths.get(profileDir));
                String newFileName = "profile_" + userId + "_" + System.currentTimeMillis() + ".jpg";
                Path targetPath = Paths.get(profileDir, newFileName);
                Files.copy(file.toPath(), targetPath);
                try (Connection conn = db_connect.getConnection();
                     PreparedStatement stmt = conn.prepareStatement(
                             "UPDATE users SET profile_picture = ? WHERE user_id = ?")) {
                    stmt.setString(1, newFileName);
                    stmt.setInt(2, userId);
                    stmt.executeUpdate();
                }
                cover_photo.setImage(new Image("file:" + targetPath.toString()));
            } catch (IOException | SQLException e) {
                e.printStackTrace();
                showAlert("Error", "Failed to update profile photo: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handle_earning_amount(ActionEvent actionEvent) {
        showAlert("Info", "Earnings display the total support received. This field is read-only.");
    }

    @FXML
    private void handle_back_button(ActionEvent event) {
        if (mainController != null) {
            mainController.loadFXML("home.fxml");
            Stage stage = (Stage) back_button.getScene().getWindow();
            stage.setWidth(1400);
            stage.setHeight(660);
            stage.setResizable(false);
        } else {
            showAlert("Error", "Navigation failed: mainController is null");
        }
    }

    public void setMainController(nav_bar__c mainController) {
        this.mainController = mainController;
        System.out.println("setMainController called with: " + (mainController != null ? "set" : "null"));
    }

    public void closeEditOverlay() {
        if (editOverlay == null) {
            System.err.println("editOverlay is null, nothing to close");
            return;
        }
        AnchorPane parentPane = null;
        if (mainController != null && mainController.getCenterPane() != null) {
            parentPane = mainController.getCenterPane();
        } else if (editOverlay.getParent() instanceof AnchorPane) {
            parentPane = (AnchorPane) editOverlay.getParent();
        }
        if (parentPane != null) {
            parentPane.getChildren().remove(editOverlay);
            if (originalChildren != null) {
                for (Node child : originalChildren) {
                    child.setEffect(null);
                }
                originalChildren.clear();
            }
            editOverlay = null;
            System.out.println("Edit overlay closed");
            loadUserProfile(); // Refresh profile data
        } else {
            showAlert("Error", "Cannot close edit overlay: parentPane is null");
        }
    }

    public void setAuthorId(int authorId) {
        this.userId = authorId;
        System.out.println("setAuthorId called with userId: " + userId);
    }

    private AnchorPane getParentPane(ActionEvent actionEvent) {
        if (mainController != null && mainController.getCenterPane() != null) {
            return mainController.getCenterPane();
        }
        Node source = (Node) actionEvent.getSource();
        Parent root = source.getScene().getRoot();
        if (root instanceof AnchorPane) {
            return (AnchorPane) root;
        } else if (root instanceof BorderPane) {
            BorderPane borderPane = (BorderPane) root;
            Node center = borderPane.getCenter();
            if (center instanceof AnchorPane) {
                return (AnchorPane) center;
            }
        }
        return null;
    }

    private void showAlert(String title, String content) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}