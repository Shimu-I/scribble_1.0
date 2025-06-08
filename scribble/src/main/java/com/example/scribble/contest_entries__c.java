package com.example.scribble;


import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;

public class contest_entries__c {

    @FXML
    private Button back_button;

    @FXML
    private Button add_entry;

    @FXML
    private VBox entryContainer;

    @FXML
    private Label genre_name;

    private int contestId;
    private String genre;
    private int userId;
    private String username;

    public void initData(int contestId, String genre, int userId, String username) {
        this.contestId = contestId;
        this.genre = genre;
        this.userId = userId;
        this.username = username;
        updateGenreLabel();
        loadEntries();
    }

    public void setContestId(int contestId, String genre) {
        this.contestId = contestId;
        this.genre = genre;
        this.userId = UserSession.getInstance().getUserId();
        this.username = UserSession.getInstance().getUsername();
        updateGenreLabel();
        loadEntries();
    }

    private void updateGenreLabel() {
        if (genre_name != null && genre != null) {
            genre_name.setText(genre);
        } else if (genre_name == null) {
            System.err.println("genre_name label is not injected from FXML.");
        } else {
            System.out.println("Skipped genre label update: genre_name=" + genre_name + ", genre=" + genre);
        }
    }

    @FXML
    private void handle_back_button(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/scribble/contest.fxml"));
            if (loader.getLocation() == null) {
                showErrorAlert("Resource Error", "Contest page resource not found.");
                return;
            }
            Parent root = loader.load();
            Stage stage = (Stage) back_button.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Pen Wars Contests");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Navigation Error", "Failed to return to the contest page: " + e.getMessage());
        }
    }

    @FXML
    private void handle_add_entry(ActionEvent event) {
        if (!UserSession.getInstance().isLoggedIn()) {
            showErrorAlert("Session Error", "You must be logged in to add an entry.");
            return;
        }
        if (userId != UserSession.getInstance().getUserId()) {
            showErrorAlert("Session Error", "User ID mismatch. Please log in with the correct account.");
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/scribble/contest_write.fxml"));
            if (loader.getLocation() == null) {
                showErrorAlert("Resource Error", "Contest writing page resource not found.");
                return;
            }
            Parent root = loader.load();
            contest_write__c controller = loader.getController();
            controller.initData(contestId, genre, userId, username, UserSession.getInstance().getUserPhotoPath());
            Stage stage = (Stage) add_entry.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Submit Contest Entry");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Navigation Error", "Failed to open the entry submission page: " + e.getMessage());
        }
    }

    @FXML
    private void handle_not_voted_button(ActionEvent event) {
        if (!UserSession.getInstance().isLoggedIn()) {
            showErrorAlert("Session Error", "You must be logged in to vote.");
            return;
        }
        Button source = (Button) event.getSource();
        HBox entryHBox = (HBox) source.getParent().getParent().getParent().getParent();
        int entryId = getEntryIdFromHBox(entryHBox);
        addVote(entryId, entryHBox);
    }

    @FXML
    private void handle_voted_button(ActionEvent event) {
        if (!UserSession.getInstance().isLoggedIn()) {
            showErrorAlert("Session Error", "You must be logged in to remove a vote.");
            return;
        }
        Button source = (Button) event.getSource();
        HBox entryHBox = (HBox) source.getParent().getParent().getParent().getParent();
        int entryId = getEntryIdFromHBox(entryHBox);
        removeVote(entryId, entryHBox);
    }

    @FXML
    private void handle_open_entry(ActionEvent event) {
        Button source = (Button) event.getSource();
        HBox entryHBox = (HBox) source.getParent();
        int entryId = getEntryIdFromHBox(entryHBox);
        openEntryPage(entryId);
    }

    private void loadEntries() {
        if (entryContainer == null) {
            System.err.println("entryContainer is null; cannot load entries.");
            return;
        }
        entryContainer.getChildren().clear();
        if (contestId <= 0) {
            showErrorAlert("Invalid Contest", "Invalid contest ID: " + contestId);
            return;
        }

        try (Connection conn = db_connect.getConnection()) {
            if (conn == null) {
                showErrorAlert("Database Error", "Failed to connect to the database.");
                return;
            }

            String query = "SELECT ce.entry_id, ce.entry_title, ce.submission_date, ce.vote_count, ce.cover_photo, u.username " +
                    "FROM contest_entries ce JOIN users u ON ce.user_id = u.user_id " +
                    "WHERE ce.contest_id = ? ORDER BY ce.vote_count DESC, ce.submission_date ASC";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setInt(1, contestId);
                System.out.println("Executing query for contestId: " + contestId);
                ResultSet rs = stmt.executeQuery();
                int entryNumber = 1;
                while (rs.next()) {
                    HBox entryHBox = createEntryHBox(
                            rs.getInt("entry_id"),
                            entryNumber++,
                            rs.getString("entry_title"),
                            rs.getString("username"),
                            rs.getTimestamp("submission_date"),
                            rs.getInt("vote_count"),
                            rs.getString("cover_photo")
                    );
                    entryContainer.getChildren().add(entryHBox);
                }
                if (entryNumber == 1) {
                    System.out.println("No entries found for contestId: " + contestId);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showErrorAlert("Database Error", "Failed to load contest entries: " + e.getMessage());
        }
    }

    private HBox createEntryHBox(int entryId, int entryNumber, String title, String author, java.sql.Timestamp submissionDate,
                                 int voteCount, String coverPhoto) {
        HBox hbox = new HBox();
        hbox.setAlignment(javafx.geometry.Pos.CENTER);
        hbox.setPrefHeight(95.0); // Set height to 95
        hbox.setPrefWidth(1030.0); // Set width to 1030
        hbox.setMaxHeight(95.0); // Enforce maximum height
        hbox.setMaxWidth(1030.0); // Enforce maximum width
        hbox.setMinHeight(95.0); // Enforce minimum height
        hbox.setMinWidth(1030.0); // Enforce minimum width
        hbox.setSpacing(25.0); // Match FXML
        hbox.setStyle("-fx-background-color: #F4908A; -fx-background-radius: 10;");
        hbox.setId("entry_hbox");

        HBox numberBox = new HBox();
        numberBox.setAlignment(javafx.geometry.Pos.CENTER);
        numberBox.setPrefHeight(58.0); // Set height to 58
        numberBox.setPrefWidth(58.0); // Set width to 58
        numberBox.setMaxHeight(58.0); // Enforce maximum height
        numberBox.setMaxWidth(58.0); // Enforce maximum width
        numberBox.setMinHeight(58.0); // Enforce minimum height
        numberBox.setMinWidth(58.0); // Enforce minimum width
        numberBox.setStyle("-fx-background-color: #014237; -fx-background-radius: 20;");
        numberBox.setId("entry_no_hbox");
        Label numberLabel = new Label(String.valueOf(entryNumber));
        numberLabel.setTextFill(javafx.scene.paint.Color.WHITE);
        numberLabel.setFont(new Font("System Bold", 20.0));
        numberBox.getChildren().add(numberLabel);

        VBox titleAuthorBox = new VBox();
        titleAuthorBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        titleAuthorBox.setPrefHeight(95.0); // Match FXML
        titleAuthorBox.setPrefWidth(266.0); // Match FXML
        Label titleLabel = new Label(title);
        titleLabel.setFont(new Font("System Bold", 24.0));
        Label authorLabel = new Label("by " + author);
        authorLabel.setFont(new Font("System Bold", 14.0));
        titleAuthorBox.getChildren().addAll(titleLabel, authorLabel);

        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
        Label dateLabel = new Label("Uploaded on " + dateFormat.format(submissionDate));
        dateLabel.setFont(new Font(14.0));

        HBox voteBox = new HBox();
        voteBox.setAlignment(javafx.geometry.Pos.CENTER);
        voteBox.setPrefHeight(95.0); // Match FXML
        voteBox.setPrefWidth(232.0); // Match FXML
        voteBox.setSpacing(10.0); // Match FXML

        StackPane voteButtonsPane = new StackPane();
        voteButtonsPane.setAlignment(javafx.geometry.Pos.CENTER);
        voteButtonsPane.setPrefHeight(30.0); // Height of one button
        voteButtonsPane.setPrefWidth(30.0); // Width of one button
        voteButtonsPane.setStyle("-fx-pref-height: 30.0; -fx-pref-width: 60.0; -fx-max-height: -Infinity; -fx-max-width: -Infinity; -fx-min-height: -Infinity; -fx-min-width: -Infinity;");

        Button notVotedButton = new Button();
        ImageView notVotedIcon = new ImageView(new Image(getClass().getResource("/images/icons/star5.png").toExternalForm()));
        notVotedIcon.setFitHeight(30.0); // Match FXML
        notVotedIcon.setFitWidth(30.0); // Match FXML
        notVotedButton.setGraphic(notVotedIcon);
        notVotedButton.setPrefHeight(30.0); // Match FXML
        notVotedButton.setPrefWidth(30.0); // Match FXML
        notVotedButton.setStyle("-fx-background-color: transparent;"); // Fixed
        notVotedButton.setOnAction(e -> addVote(entryId, hbox));

        Button votedButton = new Button();
        ImageView votedIcon = new ImageView(new Image(getClass().getResource("/images/icons/star6.png").toExternalForm()));
        votedIcon.setFitHeight(30.0); // Match FXML
        votedIcon.setFitWidth(30.0); // Match FXML
        votedButton.setGraphic(votedIcon);
        votedButton.setPrefHeight(30.0); // Match FXML
        votedButton.setPrefWidth(30.0); // Match FXML
        votedButton.setStyle("-fx-background-color: transparent;"); // Fixed
        votedButton.setOnAction(e -> removeVote(entryId, hbox));

        boolean hasVoted = UserSession.getInstance().isLoggedIn() && hasUserVoted(entryId);
        notVotedButton.setVisible(!hasVoted);
        votedButton.setVisible(hasVoted);
        voteButtonsPane.getChildren().addAll(notVotedButton, votedButton); // Overlay at 0,0,0

        Label voteCountLabel = new Label("Voted by " + voteCount + " people");
        voteCountLabel.setFont(new Font(14.0));
        voteBox.getChildren().addAll(voteButtonsPane, voteCountLabel);

        Button openButton = new Button("open");
        openButton.setPrefHeight(30.0); // Match FXML
        openButton.setPrefWidth(120.0); // Match FXML
        openButton.setStyle("-fx-background-color: #F5E0CD; -fx-background-radius: 5;"); // Fixed
        openButton.setTextFill(javafx.scene.paint.Color.valueOf("#014237"));
        openButton.setFont(new Font("System Bold", 14.0));
        openButton.setOnAction(e -> openEntryPage(entryId));

        hbox.getChildren().addAll(numberBox, titleAuthorBox, dateLabel, voteBox, openButton);
        hbox.getProperties().put("entryId", entryId);
        return hbox;
    }

    private boolean hasUserVoted(int entryId) {
        try (Connection conn = db_connect.getConnection()) {
            String query = "SELECT vote_id FROM contest_votes WHERE contest_entry_id = ? AND user_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setInt(1, entryId);
                stmt.setInt(2, UserSession.getInstance().getUserId());
                ResultSet rs = stmt.executeQuery();
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void addVote(int entryId, HBox entryHBox) {
        try (Connection conn = db_connect.getConnection()) {
            conn.setAutoCommit(false);
            String insertVote = "INSERT INTO contest_votes (contest_entry_id, user_id, vote_value, created_at) VALUES (?, ?, TRUE, NOW())";
            try (PreparedStatement stmt = conn.prepareStatement(insertVote)) {
                stmt.setInt(1, entryId);
                stmt.setInt(2, UserSession.getInstance().getUserId());
                stmt.executeUpdate();
            }
            String updateVotes = "UPDATE contest_entries SET vote_count = vote_count + 1 WHERE entry_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(updateVotes)) {
                stmt.setInt(1, entryId);
                stmt.executeUpdate();
            }
            conn.commit();
            updateVoteDisplay(entryHBox, true);
            loadEntries();
        } catch (SQLException e) {
            e.printStackTrace();
            showErrorAlert("Database Error", "Failed to add vote: " + e.getMessage());
        }
    }

    private void removeVote(int entryId, HBox entryHBox) {
        try (Connection conn = db_connect.getConnection()) {
            conn.setAutoCommit(false);
            String deleteVote = "DELETE FROM contest_votes WHERE contest_entry_id = ? AND user_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(deleteVote)) {
                stmt.setInt(1, entryId);
                stmt.setInt(2, UserSession.getInstance().getUserId());
                stmt.executeUpdate();
            }
            String updateVotes = "UPDATE contest_entries SET vote_count = vote_count - 1 WHERE entry_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(updateVotes)) {
                stmt.setInt(1, entryId);
                stmt.executeUpdate();
            }
            conn.commit();
            updateVoteDisplay(entryHBox, false);
            loadEntries();
        } catch (SQLException e) {
            e.printStackTrace();
            showErrorAlert("Database Error", "Failed to remove vote: " + e.getMessage());
        }
    }

    private void updateVoteDisplay(HBox entryHBox, boolean hasVoted) {
        HBox voteBox = (HBox) entryHBox.getChildren().get(3);
        StackPane voteButtonsPane = (StackPane) voteBox.getChildren().get(0);
        Button notVotedButton = (Button) voteButtonsPane.getChildren().get(0);
        Button votedButton = (Button) voteButtonsPane.getChildren().get(1);
        notVotedButton.setVisible(!hasVoted);
        votedButton.setVisible(hasVoted);

        Label voteCountLabel = (Label) voteBox.getChildren().get(1);
        int currentVotes = Integer.parseInt(voteCountLabel.getText().replaceAll("Voted by (\\d+) people", "$1"));
        voteCountLabel.setText("Voted by " + (hasVoted ? currentVotes + 1 : Math.max(0, currentVotes - 1)) + " people");
    }

    private void openEntryPage(int entryId) {
        try {
            URL resource = getClass().getResource("/com/example/scribble/contest_read_entry.fxml");
            if (resource == null) {
                System.err.println("Resource not found: /com/example/scribble/contest_read_entry.fxml");
                showErrorAlert("Resource Error", "Contest entry read page resource not found.");
                return;
            }
            FXMLLoader loader = new FXMLLoader(resource);
            Parent root = loader.load();
            contest_read_entry__c controller = loader.getController();
            controller.initData(entryId);
            Stage stage = (Stage) entryContainer.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Read Contest Entry");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Navigation Error", "Failed to open the entry page: " + e.getMessage());
        }
    }

    private int getEntryIdFromHBox(HBox hbox) {
        return (int) hbox.getProperties().get("entryId");
    }

    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}