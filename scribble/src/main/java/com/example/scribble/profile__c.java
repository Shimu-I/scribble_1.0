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
                user_name.setText(userId + " : " + rs.getString("username"));
                user_email.setText(rs.getString("email"));
                String profilePic = rs.getString("profile_picture");
                if (profilePic != null && !profilePic.isEmpty()) {
                    String resourcePath = "/images/profiles/" + profilePic;
                    try {
                        cover_photo.setImage(new Image(getClass().getResource(resourcePath).toExternalForm()));
                    } catch (NullPointerException e) {
                        LOGGER.warning("Profile picture not found: " + resourcePath);
                        cover_photo.setImage(new Image(getClass().getResource("/images/profiles/green_circle.png").toExternalForm()));
                    }
                } else {
                    cover_photo.setImage(new Image(getClass().getResource("/images/profiles/green_circle.png").toExternalForm()));
                }
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
        AnchorPane parentPane = getParentPane(event);
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

            for (Node child : parentPane.getChildren()) {
                child.setEffect(new GaussianBlur(10.0));
            }

            AnchorPane.setTopAnchor(editPage, 10.0);
            AnchorPane.setLeftAnchor(editPage, 10.0);

            parentPane.getChildren().add(editOverlay);
            AnchorPane.setTopAnchor(editOverlay, 0.0);
            AnchorPane.setBottomAnchor(editOverlay, 0.0);
            AnchorPane.setLeftAnchor(editOverlay, 0.0);
            AnchorPane.setRightAnchor(editOverlay, 0.0);
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

    private void showSupportersList() {
        try (Connection conn = db_connect.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT u.username, s.message, s.amount " +
                             "FROM support s " +
                             "JOIN users u ON s.user_id = u.user_id " +
                             "WHERE s.author_id = ?")) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            // Create a list to hold supporter data
            List<Supporter> supporters = new ArrayList<>();
            while (rs.next()) {
                supporters.add(new Supporter(
                        rs.getString("username"),
                        rs.getString("message") != null ? rs.getString("message") : "No message",
                        rs.getDouble("amount")
                ));
            }

            // Check if no supporters exist
            if (supporters.isEmpty()) {
                showAlert("Info", "You haven't received any support");
                return;
            }

            // Create TableView
            TableView<Supporter> tableView = new TableView<>();
            tableView.setPlaceholder(new Label("No supporters yet."));

            // Serial number column
            TableColumn<Supporter, Integer> serialColumn = new TableColumn<>("Serial");
            serialColumn.setCellFactory(col -> new TableCell<>() {
                @Override
                protected void updateItem(Integer item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setText(null);
                    } else {
                        setText(String.valueOf(getIndex() + 1)); // Serial number starts from 1
                    }
                }
            });
            serialColumn.setPrefWidth(50);

            // Username column
            TableColumn<Supporter, String> usernameColumn = new TableColumn<>("Username");
            usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
            usernameColumn.setPrefWidth(150);

            // Message column with "Open" button
            TableColumn<Supporter, Void> messageColumn = new TableColumn<>("Message");
            messageColumn.setCellFactory(col -> new TableCell<>() {
                private final Button openButton = new Button("Open");

                {
                    openButton.setOnAction(event -> {
                        Supporter supporter = getTableView().getItems().get(getIndex());
                        showMessagePopup(supporter.getMessage());
                    });
                }

                @Override
                protected void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setGraphic(null);
                    } else {
                        setGraphic(openButton);
                    }
                }
            });
            messageColumn.setPrefWidth(100);

            // Amount column
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
            amountColumn.setPrefWidth(100);

            // Add columns to table
            tableView.getColumns().addAll(serialColumn, usernameColumn, messageColumn, amountColumn);
            tableView.getItems().addAll(supporters);

            // Create popup window
            Stage popupStage = new Stage();
            popupStage.initModality(Modality.APPLICATION_MODAL);
            popupStage.setTitle("Supporters List");
            VBox root = new VBox(10, tableView);
            root.setPadding(new javafx.geometry.Insets(10));
            Scene scene = new Scene(root, 450, 300); // Adjusted width to accommodate serial column
            popupStage.setScene(scene);
            popupStage.showAndWait();

        } catch (SQLException e) {
            LOGGER.severe("Failed to load supporters: " + e.getMessage());
            showAlert("Error", "Failed to load supporters: " + e.getMessage());
        }
    }

    private void showMessagePopup(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Supporter Message");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Model class for supporter data
    public static class Supporter {
        private final String username;
        private final String message;
        private final double amount;

        public Supporter(String username, String message, double amount) {
            this.username = username;
            this.message = message;
            this.amount = amount;
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