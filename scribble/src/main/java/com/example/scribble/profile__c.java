package com.example.scribble;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.Node;
import javafx.stage.Stage;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.Scene;
import javafx.stage.Modality;
import java.util.ArrayList;
import java.util.List;

import java.io.IOException;
import java.sql.*;
import java.util.logging.Logger;

public class profile__c {

    private static final Logger LOGGER = Logger.getLogger(profile__c.class.getName());

    @FXML
    private ImageView cover_photo;
    @FXML
    private Label user_name;
    @FXML
    private Button edit_profile;
    @FXML
    private Label user_email;
    @FXML
    private Label joined_at;
    @FXML
    private TextField supported_amount;
    @FXML
    private Button show_supporters;
    @FXML
    private Button history_library_button;
    @FXML
    private Button my_work_my_draft_button;
    @FXML
    private Button colab_sent_received_button;
    @FXML
    private Button groups_joined_owned_button;
    @FXML
    private Button back_button;
    @FXML
    private VBox all_button_work;
    @FXML
    private VBox vbox_container;

    @FXML
    private nav_bar__c mainController;
    private int userId;
    private Pane editOverlay;

    @FXML
    public void initialize() {
        userId = UserSession.getInstance().getUserId();
        if (userId == 0) {
            showAlert("Error", "No user logged in");
            return;
        }
        supported_amount.setEditable(false);
        loadUserProfile();
        vbox_container.setPrefHeight(332.0);
        // Simulate click on history_library_button by default
        handle_history_library(new ActionEvent(history_library_button, null));
    }

    private void loadProfileImage(String profilePicName) {
        if (profilePicName != null && !profilePicName.isEmpty()) {
            try {
                // Try loading from filesystem first
                java.io.File uploadFile = new java.io.File("Uploads/profiles/" + profilePicName);
                if (uploadFile.exists()) {
                    Image image = new Image("file:" + uploadFile.getAbsolutePath(), true);
                    if (!image.isError()) {
                        cover_photo.setImage(image);
                        LOGGER.info("Loaded profile image from filesystem: file:" + uploadFile.getAbsolutePath());
                        return;
                    } else {
                        LOGGER.warning("Failed to load profile image from filesystem (image error): " + profilePicName);
                    }
                } else {
                    // Fall back to classpath
                    String imagePath = "/images/profiles/" + profilePicName;
                    java.net.URL resource = getClass().getResource(imagePath);
                    if (resource != null) {
                        Image image = new Image(resource.toExternalForm(), true);
                        if (!image.isError()) {
                            cover_photo.setImage(image);
                            LOGGER.info("Loaded profile image from classpath: " + imagePath);
                            return;
                        } else {
                            LOGGER.warning("Failed to load profile image from classpath (image error): " + profilePicName);
                        }
                    } else {
                        LOGGER.warning("Profile picture not found: " + imagePath);
                    }
                }
            } catch (Exception e) {
                LOGGER.severe("Failed to load profile image: " + profilePicName + " - " + e.getMessage());
            }
        } else {
            LOGGER.info("No profile picture provided, using default");
        }
        // Default to hollow_circle.png
        Image defaultImage = new Image(getClass().getResource("/images/profiles/hollow_circle.png").toExternalForm());
        cover_photo.setImage(defaultImage);
        LOGGER.info("Loaded default profile image: hollow_circle.png");
    }

    public void loadUserProfile() {
        try (Connection conn = db_connect.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT username, email, profile_picture, created_at, " +
                             "(SELECT SUM(amount) FROM support WHERE author_id = ?) AS total_earnings " +
                             "FROM users WHERE user_id = ?")) {
            stmt.setInt(1, userId);
            stmt.setInt(2, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                user_name.setText(userId + " : " + rs.getString("username"));
                user_email.setText(rs.getString("email"));
                String profilePic = rs.getString("profile_picture");
                loadProfileImage(profilePic);
                Timestamp createdAt = rs.getTimestamp("created_at");
                if (createdAt != null) {
                    joined_at.setText("Member since " + new java.text.SimpleDateFormat("MMMM dd, yyyy").format(createdAt));
                }
                double earnings = rs.getDouble("total_earnings");
                if (rs.wasNull()) earnings = 0.0;
                supported_amount.setText(String.format("$%.2f", earnings));
            } else {
                showAlert("Error", "User profile not found for ID: " + userId);
            }
        } catch (SQLException e) {
            LOGGER.severe("Failed to load user profile: " + e.getMessage());
            showAlert("Error", "Failed to load user profile: " + e.getMessage());
        }
    }

    @FXML
    private void handle_history_library(ActionEvent event) {
        updateButtonStyles(history_library_button);
        loadContent("history_library.fxml");
    }

    @FXML
    private void handle_my_work_my_draft(ActionEvent event) {
        updateButtonStyles(my_work_my_draft_button);
        loadContent("my_works_drafts.fxml");
    }

    @FXML
    private void handle_colab_sent_received(ActionEvent event) {
        updateButtonStyles(colab_sent_received_button);
        loadContent("colab_sent_received.fxml");
    }

    @FXML
    private void handle_groups_joined_owned(ActionEvent event) {
        updateButtonStyles(groups_joined_owned_button);
        loadContent("groups_joined_owned.fxml");
    }

    private void updateButtonStyles(Button activeButton) {
        Button[] buttons = {history_library_button, my_work_my_draft_button, colab_sent_received_button, groups_joined_owned_button};
        for (Button button : buttons) {
            if (button != null) {
                // Not-clicked style
                button.setStyle("-fx-background-color: #F8E9D4; -fx-text-fill: #000; -fx-background-radius: 5;");
            }
        }
        if (activeButton != null) {
            // Clicked style
            activeButton.setStyle("-fx-border-color: #fff; -fx-border-radius: 5; -fx-background-color: transparent; -fx-background-radius: 5;");
            activeButton.setTextFill(javafx.scene.paint.Color.WHITE);
        }
    }

    private void loadContent(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/scribble/" + fxmlPath));
            Parent content = loader.load();
            if (content instanceof VBox) {
                ((VBox) content).setPrefHeight(332.0);
            }
            Object controller = loader.getController();
            if (controller instanceof nav_bar__cAware) {
                ((nav_bar__cAware) controller).setMainController(mainController);
            }
            vbox_container.getChildren().clear();
            vbox_container.getChildren().add(content);
        } catch (IOException e) {
            LOGGER.severe("Failed to load content " + fxmlPath + ": " + e.getMessage());
            showAlert("Error", "Failed to load content: " + e.getMessage());
        }
    }

    @FXML
    private void handle_edit_profile(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/scribble/profile_info_form.fxml"));
            Parent editPage = loader.load();
            profile_info_edit__c editController = loader.getController();
            editController.setParentController(this);
            editController.setMainController(mainController); // Pass nav_bar__c to profile_info_edit__c
            editController.setUserId(userId);

            // Create a new modal stage
            Stage modalStage = new Stage();
            modalStage.initModality(Modality.APPLICATION_MODAL);
            modalStage.setTitle("Edit Profile");
            Scene scene = new Scene(editPage);
            modalStage.setScene(scene);
            modalStage.setResizable(false);

            // Remove blur effect from parent window
            AnchorPane parentPane = getParentPane(event);
            if (parentPane != null) {
                for (Node child : parentPane.getChildren()) {
                    child.setEffect(null); // Remove any existing blur
                }
            }

            // Show the modal window
            modalStage.showAndWait();
            loadUserProfile(); // Refresh profile data after modal closes
        } catch (IOException e) {
            LOGGER.severe("Failed to load profile edit form: " + e.getMessage());
            showAlert("Error", "Failed to load profile edit form: " + e.getMessage());
        }
    }

    @FXML
    private void handle_supported_amount(ActionEvent event) {
        showAlert("Info", "Earnings display the total support received. This field is read-only.");
    }

    @FXML
    private void handle_show_supporters(ActionEvent event) {
        showSupportersList();
    }


    private void showMessagePopup(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Supporter Message");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showSupportersList() {
        try (Connection conn = db_connect.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT s.support_id, u.username, s.message, s.amount, s.book_id, b.title " +
                             "FROM support s " +
                             "JOIN users u ON s.user_id = u.user_id " +
                             "LEFT JOIN books b ON s.book_id = b.book_id " +
                             "WHERE s.author_id = ?")) {
            LOGGER.info("Executing query for showSupportersList with author_id: " + userId);
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            List<Supporter> supporters = new ArrayList<>();
            int recordCount = 0;
            while (rs.next()) {
                recordCount++;
                String message = rs.getString("message");
                String bookTitle = rs.getString("title");
                int bookId = rs.getInt("book_id");
                String username = rs.getString("username");
                double amount = rs.getDouble("amount");
                LOGGER.info(String.format("Support Record %d: support_id=%d, username=%s, book_id=%d, book_title=%s, amount=%.2f, message=%s",
                        recordCount, rs.getInt("support_id"), username, bookId,
                        (bookTitle != null ? bookTitle : "NULL"), amount,
                        (message != null ? message : "No message")));
                supporters.add(new Supporter(
                        username,
                        message != null ? message : "No message",
                        amount,
                        bookTitle != null ? bookTitle : "No Book Associated"
                ));
            }
            LOGGER.info("Total support records retrieved: " + recordCount);

            if (supporters.isEmpty()) {
                LOGGER.info("No supporters found for author_id: " + userId);
                showAlert("Info", "You haven't received any support");
                return;
            }

            LOGGER.info("Populating TableView with " + supporters.size() + " supporters");
            TableView<Supporter> tableView = new TableView<>();
            tableView.setPrefWidth(480); // Fixed width to match column totals
            tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY); // Distribute space among columns
            tableView.setPlaceholder(new Label("No supporters yet."));

            TableColumn<Supporter, Integer> serialColumn = new TableColumn<>("Serial");
            serialColumn.setCellFactory(col -> new TableCell<>() {
                @Override
                protected void updateItem(Integer item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setText(null);
                    } else {
                        setText(String.valueOf(getIndex() + 1));
                    }
                }
            });
            serialColumn.setPrefWidth(30);

            TableColumn<Supporter, String> usernameColumn = new TableColumn<>("Username");
            usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
            usernameColumn.setPrefWidth(120);

            TableColumn<Supporter, String> bookTitleColumn = new TableColumn<>("Book Title");
            bookTitleColumn.setCellValueFactory(new PropertyValueFactory<>("bookTitle"));
            bookTitleColumn.setPrefWidth(150);

            TableColumn<Supporter, Void> messageColumn = new TableColumn<>("Message");
            messageColumn.setCellFactory(col -> new TableCell<>() {
                private final Button openButton = new Button("Open");

                {
                    openButton.setOnAction(event -> {
                        Supporter supporter = getTableView().getItems().get(getIndex());
                        if (supporter != null) {
                            String message = supporter.getMessage();
                            LOGGER.info("Displaying message for supporter: " + supporter.getUsername() + ", message: " + message);
                            showMessagePopup(message != null ? message : "No message");
                        } else {
                            LOGGER.warning("Supporter is null at index: " + getIndex());
                            showAlert("Error", "Unable to display message: No supporter data available.");
                        }
                    });
                }

                @Override
                protected void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    setGraphic(empty ? null : openButton);
                }
            });
            messageColumn.setPrefWidth(80);

            TableColumn<Supporter, Double> amountColumn = new TableColumn<>("Amount");
            amountColumn.setCellValueFactory(new PropertyValueFactory<>("amount"));
            amountColumn.setCellFactory(col -> new TableCell<>() {
                @Override
                protected void updateItem(Double amount, boolean empty) {
                    super.updateItem(amount, empty);
                    if (empty || amount == null) {
                        setText(null);
                    } else {
                        setText(String.format("$%.2f", amount));
                    }
                }
            });
            amountColumn.setPrefWidth(80);

            tableView.getColumns().addAll(serialColumn, usernameColumn, bookTitleColumn, messageColumn, amountColumn);
            tableView.getItems().addAll(supporters);
            LOGGER.info("TableView populated with columns: Serial, Username, Book Title, Message, Amount");

            Stage popupStage = new Stage();
            popupStage.initModality(Modality.APPLICATION_MODAL);
            popupStage.setTitle("Supporters List");
            VBox root = new VBox(10, tableView);
            root.setPadding(new javafx.geometry.Insets(10));
            Scene scene = new Scene(root, 550, 300); // Keeping original scene size for flexibility
            popupStage.setScene(scene);
            LOGGER.info("Displaying Supporters List popup");
            popupStage.showAndWait();

        } catch (SQLException e) {
            LOGGER.severe("Failed to load supporters: " + e.getMessage());
            showAlert("Error", "Failed to load supporters: " + e.getMessage());
        }
    }

    public static class Supporter {
        private final String username;
        private final String message;
        private final double amount;
        private final String bookTitle;

        public Supporter(String username, String message, double amount, String bookTitle) {
            this.username = username;
            this.message = message;
            this.amount = amount;
            this.bookTitle = bookTitle;
        }

        public String getUsername() {
            return username;
        }

        public String getMessage() {
            return message;
        }

        public double getAmount() {
            return amount;
        }

        public String getBookTitle() {
            return bookTitle;
        }
    }

    @FXML
    private void handle_back_button(ActionEvent event) {
        if (mainController != null) {
            mainController.loadFXML("home.fxml");
            Stage stage = (Stage) back_button.getScene().getWindow();
            stage.setResizable(true); // Allow resizing instead of fixed size
        } else {
            showAlert("Error", "Navigation failed: mainController is null");
        }
    }

    public void setMainController(nav_bar__c mainController) {
        this.mainController = mainController;
    }

    public void closeEditOverlay() {
        AnchorPane parentPane = (mainController != null && mainController.getCenterPane() != null) ?
                mainController.getCenterPane() : (AnchorPane) back_button.getScene().getRoot();
        if (parentPane != null && editOverlay != null) {
            parentPane.getChildren().remove(editOverlay);
            for (Node child : parentPane.getChildren()) {
                child.setEffect(null);
            }
            editOverlay = null;
            loadUserProfile();
        } else {
            showAlert("Error", "Cannot close edit overlay: parentPane or editOverlay is null");
        }
    }

    public void setAuthorId(int authorId) {
        this.userId = authorId;
    }

    private AnchorPane getParentPane(ActionEvent event) {
        if (mainController != null && mainController.getCenterPane() != null) {
            return mainController.getCenterPane();
        }
        Node source = (Node) event.getSource();
        Parent root = source.getScene().getRoot();
        if (root instanceof AnchorPane) {
            return (AnchorPane) root;
        } else if (root instanceof BorderPane) {
            Node center = ((BorderPane) root).getCenter();
            if (center instanceof AnchorPane) {
                return (AnchorPane) center;
            }
        }
        return null;
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}

// Interface for controller injection
interface nav_bar__cAware {
    void setMainController(nav_bar__c controller);
}