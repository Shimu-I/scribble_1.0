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
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

public class c__c {
    private static final Logger LOGGER = Logger.getLogger(c__c.class.getName());
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("EEE d MMMM yyyy");
    private static final String DEFAULT_COVER = "/images/contest_book_cover/demo_cover_photo.png";

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
    private List<WeekData> weekDataList = new ArrayList<>();
    private int currentWeekIndex = 0;

    private static class WeekData {
        LocalDateTime startDate;
        List<Entry> entries;

        WeekData(LocalDateTime startDate, List<Entry> entries) {
            this.startDate = startDate;
            this.entries = entries;
        }
    }

    private static class Entry {
        int entryId;
        String title;
        String author;
        int voteCount;
        String coverPhoto;

        Entry(int entryId, String title, String author, int voteCount, String coverPhoto) {
            this.entryId = entryId;
            this.title = title;
            this.author = author;
            this.voteCount = voteCount;
            this.coverPhoto = coverPhoto;
        }
    }

    @FXML
    public void initialize() {
        if (AppState_c.getInstance().getPreviousFXML() == null) {
            AppState_c.getInstance().setPreviousFXML("/com/example/scribble/contest.fxml");
            LOGGER.info("Set default previous FXML to: /com/example/scribble/contest.fxml");
        }

        week_session1.setText("Select Genre");
        week_session.setText("Select a genre to view results");
        view_result.getChildren().clear();
        view_result.getChildren().add(new Text("Select a genre to view results"));

        setActiveButton(genre_fantasy);
        loadWeekData("Fantasy");
        loadWeeklyResults();

        genre_fantasy.setOnAction(this::handle_genre_fantasy);
        genre_mystery.setOnAction(this::handle_genre_mystery);
        genre_fiction.setOnAction(this::handle_genre_fiction);
        genre_horror.setOnAction(this::handle_genre_horror);
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
        selectedGenre = genre;
        currentWeekIndex = 0;
        setActiveButton(getButtonForGenre(genre));
        loadWeekData(genre);
        loadWeeklyResults();
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

    private LocalDateTime getCurrentWeekStart() {
        return LocalDateTime.of(2025, 7, 12, 0, 0, 0); // As per contest_weekly_results__c
    }

    private void loadWeekData(String genre) {
        weekDataList.clear();
        try (Connection conn = db_connect.getConnection()) {
            if (conn == null) {
                LOGGER.severe("Database connection is null, cannot load week data");
                showErrorAlert("Database Error", "Failed to connect to the database.");
                return;
            }

            LocalDateTime currentWeekStart = getCurrentWeekStart();
            String query = "SELECT DISTINCT DATE_SUB(ce.submission_date, INTERVAL (WEEKDAY(ce.submission_date) + 2) % 7 DAY) as week_start " +
                    "FROM contest_entries ce JOIN contests c ON ce.contest_id = c.contest_id " +
                    "WHERE c.genre = ? AND ce.submission_date < ? " +
                    "ORDER BY week_start DESC";

            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, genre);
                stmt.setTimestamp(2, Timestamp.valueOf(currentWeekStart));
                ResultSet rs = stmt.executeQuery();
                Set<LocalDateTime> uniqueWeekStarts = new HashSet<>();
                while (rs.next()) {
                    LocalDateTime weekStart = rs.getTimestamp("week_start").toLocalDateTime()
                            .minusDays((rs.getTimestamp("week_start").toLocalDateTime().getDayOfWeek().getValue() + 1) % 7)
                            .withHour(0).withMinute(0).withSecond(0).withNano(0);
                    uniqueWeekStarts.add(weekStart);
                }

                for (LocalDateTime weekStart : uniqueWeekStarts) {
                    List<Entry> entries = loadEntriesForWeek(weekStart);
                    weekDataList.add(new WeekData(weekStart, entries));
                }
                LOGGER.info("Loaded " + weekDataList.size() + " unique weeks for genre: " + genre);

                weekDataList.sort((a, b) -> b.startDate.compareTo(a.startDate));
                currentWeekIndex = weekDataList.isEmpty() ? 0 : 0;
            }
        } catch (SQLException e) {
            LOGGER.severe("Failed to load week data: " + e.getMessage());
            showErrorAlert("Database Error", "Failed to load weekly results: " + e.getMessage());
        }
    }

    private List<Entry> loadEntriesForWeek(LocalDateTime weekStart) {
        List<Entry> entries = new ArrayList<>();
        LocalDateTime weekEnd = weekStart.plusDays(6).withHour(23).withMinute(59).withSecond(59);
        try (Connection conn = db_connect.getConnection()) {
            if (conn == null) {
                LOGGER.severe("Database connection is null, cannot load entries for week: " + weekStart);
                return entries;
            }

            String query = "SELECT ce.entry_id, ce.entry_title, ce.vote_count, ce.cover_photo, u.username " +
                    "FROM contest_entries ce JOIN users u ON ce.user_id = u.user_id " +
                    "JOIN contests c ON ce.contest_id = c.contest_id " +
                    "WHERE c.genre = ? AND ce.submission_date BETWEEN ? AND ? AND ce.vote_count > 0 " +
                    "ORDER BY ce.vote_count DESC, (SELECT MIN(created_at) FROM contest_votes cv WHERE cv.contest_entry_id = ce.entry_id) ASC " +
                    "LIMIT 3";

            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, selectedGenre);
                stmt.setTimestamp(2, Timestamp.valueOf(weekStart));
                stmt.setTimestamp(3, Timestamp.valueOf(weekEnd));
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    String coverPhoto = rs.getString("cover_photo");
                    if (coverPhoto == null || coverPhoto.trim().isEmpty()) {
                        coverPhoto = DEFAULT_COVER;
                    } else if (!coverPhoto.startsWith("/")) {
                        coverPhoto = "/images/contest_book_cover/" + coverPhoto;
                    }
                    if (getClass().getResource(coverPhoto) == null) {
                        LOGGER.warning("Image not found in resources: " + coverPhoto + ", using default cover");
                        coverPhoto = DEFAULT_COVER;
                    }
                    entries.add(new Entry(
                            rs.getInt("entry_id"),
                            rs.getString("entry_title"),
                            rs.getString("username"),
                            rs.getInt("vote_count"),
                            coverPhoto
                    ));
                }
                LOGGER.info("Loaded " + entries.size() + " entries for week " + weekStart.format(DATE_FORMATTER));
            }
        } catch (SQLException e) {
            LOGGER.severe("Failed to load entries for week " + weekStart + ": " + e.getMessage());
        }
        return entries;
    }

    private void loadWeeklyResults() {
        view_result.getChildren().clear();
        try {
            List<String> results = new ArrayList<>();
            if (!weekDataList.isEmpty() && currentWeekIndex < weekDataList.size()) {
                WeekData weekData = weekDataList.get(currentWeekIndex);
                LocalDateTime startDate = weekData.startDate;
                LocalDateTime endDate = startDate.plusDays(6);
                week_session.setText(weekData.entries.isEmpty() ? "No entries for this week" :
                        startDate.format(DATE_FORMATTER) + " - " + endDate.format(DATE_FORMATTER));

                for (Entry entry : weekData.entries) {
                    results.add(entry.entryId + "|" + entry.title + "|" + entry.author + "|" +
                            entry.voteCount + "|" + entry.coverPhoto);
                }
            } else {
                week_session.setText("No entries for this week");
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/scribble/weekly_results.fxml"));
            Parent root = loader.load();
            weekly_results__c controller = loader.getController();
            controller.setResults(results);
            view_result.getChildren().setAll(root);
            LOGGER.info("Loaded weekly_results.fxml for genre: " + selectedGenre + ", week index: " + currentWeekIndex);
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