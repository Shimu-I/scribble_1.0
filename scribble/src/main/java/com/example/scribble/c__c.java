package com.example.scribble;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class c__c {
    private static final Logger LOGGER = Logger.getLogger(c__c.class.getName());
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("EEE d MMMM yyyy");

    @FXML
    private nav_bar__c mainController;

    @FXML
    private Button back_button;

    @FXML
    private Label week_session;

    @FXML
    private Label week_session1;

    @FXML
    private Button genre_fantasy;

    @FXML
    private Button genre_mystery;

    @FXML
    private Button genre_fiction;

    @FXML
    private Button genre_horror;

    @FXML
    private HBox view_result;

    private Button activeButton;
    private String selectedGenre = "Fantasy";
    private int weekIndex = 2; // 0: lastpreviousweek, 1: previousweek, 2: currentweek
    private final String[] weeks = {"lastpreviousweek", "previousweek", "currentweek"};

    @FXML
    public void initialize() {
        // Initialize default previous FXML
        if (AppState_c.getInstance().getPreviousFXML() == null) {
            AppState_c.getInstance().setPreviousFXML("/com/example/scribble/contest.fxml");
            LOGGER.info("Set default previous FXML to: /com/example/scribble/contest.fxml");
        }

        // Initialize labels and view_result
        week_session1.setText("Select Genre");
        updateWeekSessionLabel();
        view_result.getChildren().clear();
        view_result.getChildren().add(new Text("Select a genre to view results"));

        // Set default genre to Fantasy
        setActiveButton(genre_fantasy);
        loadWeeklyResults("Fantasy");

        // Set up button actions
        genre_fantasy.setOnAction(this::handle_genre_fantasy);
        genre_mystery.setOnAction(this::handle_genre_mystery);
        genre_fiction.setOnAction(this::handle_genre_fiction);
        genre_horror.setOnAction(this::handle_genre_horror);
    }

    private void updateWeekSessionLabel() {
        LocalDate today = LocalDate.of(2025, 7, 20);
        LocalDate startDate;
        LocalDate endDate;

        switch (weeks[weekIndex]) {
            case "lastpreviousweek":
                startDate = today.minusDays(15); // Sat 5 July
                endDate = today.minusDays(9);   // Fri 11 July
                break;
            case "previousweek":
                startDate = today.minusDays(8); // Sat 12 July
                endDate = today.minusDays(2);   // Fri 18 July
                break;
            case "currentweek":
            default:
                startDate = today.minusDays(1); // Sat 19 July
                endDate = today.plusDays(5);    // Fri 25 July
                break;
        }

        week_session.setText(startDate.format(DATE_FORMATTER) + " - " + endDate.format(DATE_FORMATTER));
    }

    private void setActiveButton(Button selectedButton) {
        for (Button button : new Button[]{genre_fantasy, genre_mystery, genre_fiction, genre_horror}) {
            button.setStyle("-fx-background-radius: 5; -fx-background-color: #F5E0CD;");
            button.setOpacity(1.0);
        }
        activeButton = selectedButton;
        if (activeButton != null) {
            activeButton.setStyle("-fx-background-radius: 5; -fx-background-color: #D3BFA3;");
            activeButton.setOpacity(0.6);
        }
    }

    @FXML
    private void handle_back_button(ActionEvent event) {
        if (mainController == null) {
            LOGGER.severe("Main controller is null, cannot navigate back");
            showErrorAlert("Error", "Navigation failed: main controller is not initialized.");
            return;
        }

        try {
            String previousFXML = AppState_c.getInstance().getPreviousFXML();
            if (previousFXML == null || previousFXML.isEmpty() || previousFXML.equals("/com/example/scribble/c.fxml")) {
                previousFXML = "/com/example/scribble/contest.fxml";
                LOGGER.warning("Invalid previous FXML, defaulting to: " + previousFXML);
            }

            URL fxmlResource = getClass().getResource(previousFXML);
            if (fxmlResource == null) {
                LOGGER.severe("Resource not found: " + previousFXML);
                showErrorAlert("Resource Error", "Previous page resource not found: " + previousFXML);
                return;
            }

            FXMLLoader loader = new FXMLLoader(fxmlResource);
            Parent root = loader.load();
            Object controller = loader.getController();

            if (controller instanceof c__c) {
                ((c__c) controller).setMainController(mainController);
                LOGGER.info("setMainController called on c__c controller");
            }

            mainController.getCenterPane().getChildren().setAll(root);
            LOGGER.info("Navigated back to " + previousFXML);
        } catch (IOException e) {
            LOGGER.severe("Failed to load FXML: " + e.getMessage());
            showErrorAlert("Navigation Error", "Failed to load previous page: " + e.getMessage());
        }
    }

    private void handleGenreClick(String genre) {
        if (selectedGenre.equals(genre)) {
            // Same genre clicked, cycle to next week
            weekIndex = (weekIndex + 1) % weeks.length;
        } else {
            // New genre, reset to current week
            selectedGenre = genre;
            weekIndex = 2; // Start with current week
        }
        setActiveButton(getButtonForGenre(genre));
        updateWeekSessionLabel();
        loadWeeklyResults(genre);
    }

    private Button getButtonForGenre(String genre) {
        switch (genre) {
            case "Fantasy": return genre_fantasy;
            case "Thriller Mystery": return genre_mystery;
            case "Youth Fiction": return genre_fiction;
            case "Crime Horror": return genre_horror;
            default: return genre_fantasy;
        }
    }

    @FXML
    private void handle_genre_fantasy(ActionEvent event) {
        handleGenreClick("Fantasy");
    }

    @FXML
    private void handle_genre_mystery(ActionEvent event) {
        handleGenreClick("Thriller Mystery");
    }

    @FXML
    private void handle_genre_fiction(ActionEvent event) {
        handleGenreClick("Youth Fiction");
    }

    @FXML
    private void handle_genre_horror(ActionEvent event) {
        handleGenreClick("Crime Horror");
    }

    private void loadWeeklyResults(String genre) {
        view_result.getChildren().clear();
        try {
            LocalDate today = LocalDate.of(2025, 7, 20);
            LocalDate startDate;
            LocalDate endDate;

            switch (weeks[weekIndex]) {
                case "lastpreviousweek":
                    startDate = today.minusDays(15); // Sat 5 July
                    endDate = today.minusDays(9);   // Fri 11 July
                    break;
                case "previousweek":
                    startDate = today.minusDays(8); // Sat 12 July
                    endDate = today.minusDays(2);   // Fri 18 July
                    break;
                case "currentweek":
                default:
                    startDate = today.minusDays(1); // Sat 19 July
                    endDate = today.plusDays(5);    // Fri 25 July
                    break;
            }

            // Fetch results using db_connect.getConnection()
            List<String> results = new ArrayList<>();
            String query = "SELECT ce.entry_id, ce.entry_title, u.username, ce.vote_count, ce.cover_photo " +
                    "FROM contest_entries ce " +
                    "JOIN contests c ON ce.contest_id = c.contest_id " +
                    "JOIN users u ON ce.user_id = u.user_id " +
                    "WHERE c.genre = ? " +
                    "AND ce.submission_date BETWEEN ? AND ? " +
                    "ORDER BY ce.vote_count DESC LIMIT 3";

            try (Connection conn = db_connect.getConnection(); // Use existing db_connect
                 PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, genre);
                LocalDateTime startDateTime = startDate.atStartOfDay();
                LocalDateTime endDateTime = endDate.atTime(23, 59, 59);
                stmt.setString(2, startDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                stmt.setString(3, endDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                ResultSet rs = stmt.executeQuery();

                while (rs.next()) {
                    String result = rs.getInt("entry_id") + "|" +
                            rs.getString("entry_title") + "|" +
                            rs.getString("username") + "|" +
                            rs.getInt("vote_count") + "|" +
                            (rs.getString("cover_photo") != null ? rs.getString("cover_photo") : "file://path/to/default_cover.png");
                    results.add(result);
                }
                LOGGER.info("Fetched " + results.size() + " results for genre: " + genre + ", week: " + startDate + " to " + endDate);
            } catch (SQLException e) {
                LOGGER.severe("Database error for genre " + genre + ": " + e.getMessage());
                showErrorAlert("Database Error", "Failed to fetch results for " + genre + ": " + e.getMessage());
                return;
            }

            if (results.isEmpty()) {
                view_result.getChildren().add(new Text("No entries found for " + genre + " in " + week_session.getText()));
                return;
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/scribble/weekly_results.fxml"));
            Parent root = loader.load();
            weekly_results__c controller = loader.getController();
            controller.setResults(results);
            view_result.getChildren().setAll(root);
            LOGGER.info("Loaded weekly_results.fxml for genre: " + genre + ", week: " + weeks[weekIndex]);
        } catch (IOException e) {
            LOGGER.severe("Failed to load weekly_results.fxml: " + e.getMessage());
            showErrorAlert("Load Error", "Failed to load weekly results: " + e.getMessage());
        }
    }

    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void setMainController(nav_bar__c mainController) {
        this.mainController = mainController;
        LOGGER.info("Set mainController in c__c: " + mainController);
    }
}