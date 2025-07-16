package com.example.scribble;

import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.util.Duration;
import java.io.IOException;
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

public class contest_weekly_results__c {

    private static final Logger LOGGER = Logger.getLogger(contest_weekly_results__c.class.getName());
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("EEE d MMMM yyyy");
    private static final String DEFAULT_COVER = "/images/contest_book_cover/demo_cover_photo.png";

    @FXML private Label week_session;
    @FXML private ComboBox<String> genre_combox;
    @FXML private Button go_left;
    @FXML private Button go_right;
    @FXML private Button back_button;
    @FXML private Button first_open;
    @FXML private Button second_open;
    @FXML private Button third_open;
    @FXML private Label first_title, second_title, third_title;
    @FXML private Label first_author, second_author, third_author;
    @FXML private Label first_votes, second_votes, third_votes;
    @FXML private ImageView first_image, second_image, third_image;
    @FXML private VBox first_card, second_card, third_card;

    private List<WeekData> weekDataList;
    private int currentWeekIndex = 0;
    private String selectedGenre = "Fantasy"; // Default to Fantasy
    private nav_bar__c mainController;

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

    public void setMainController(nav_bar__c mainController) {
        this.mainController = mainController;
        LOGGER.info("Set mainController in contest__c: " + (mainController != null ? "not null" : "null"));
    }

    @FXML
    private void navigateToWeeklyResults(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/scribble/contest_weekly_results.fxml"));
            if (loader.getLocation() == null) {
                LOGGER.severe("Resource not found: /com/example/scribble/contest_weekly_results.fxml");
                showAlert("Error", "Weekly results page resource not found.");
                return;
            }
            Parent root = loader.load();
            contest_weekly_results__c controller = loader.getController();
            if (controller == null) {
                LOGGER.severe("Controller for contest_weekly_results.fxml is null");
                showAlert("Error", "Failed to initialize weekly results controller.");
                return;
            }
            if (mainController == null) {
                LOGGER.warning("Main controller is null, loading nav_bar.fxml");
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                FXMLLoader navLoader = new FXMLLoader(getClass().getResource("/com/example/scribble/nav_bar.fxml"));
                if (navLoader.getLocation() == null) {
                    LOGGER.severe("Resource not found: /com/example/scribble/nav_bar.fxml");
                    showAlert("Error", "Navigation bar resource not found.");
                    return;
                }
                Parent navRoot = navLoader.load();
                nav_bar__c navController = navLoader.getController();
                if (navController == null) {
                    LOGGER.severe("nav_bar__c controller is null");
                    showAlert("Error", "Failed to initialize navigation bar controller.");
                    return;
                }
                controller.setMainController(navController);
                navController.getCenterPane().getChildren().setAll(root);
                Scene newScene = new Scene(navRoot, 1400, 760);
                stage.setScene(newScene);
                LOGGER.info("Navigated to contest_weekly_results.fxml with new nav_bar.fxml, width: 1400, height: 760");
            } else {
                controller.setMainController(mainController);
                mainController.getCenterPane().getChildren().setAll(root);
                LOGGER.info("Navigated to contest_weekly_results.fxml via mainController");
            }
            AppState_c.getInstance().setPreviousFXML("/com/example/scribble/contest.fxml");
        } catch (IOException e) {
            LOGGER.severe("Failed to navigate to weekly results: " + e.getMessage());
            showAlert("Error", "Failed to load weekly results: " + e.getMessage());
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }


    @FXML
    private void initialize() {
        LOGGER.info("Initializing contest_weekly_results__c, mainController: " + (mainController != null ? "not null" : "null"));

        if (mainController == null && back_button != null) {
            Platform.runLater(() -> {
                try {
                    LOGGER.warning("mainController is null, attempting to load nav_bar.fxml as fallback");
                    Stage stage = (Stage) back_button.getScene().getWindow();
                    FXMLLoader navLoader = new FXMLLoader(getClass().getResource("/com/example/scribble/nav_bar.fxml"));
                    if (navLoader.getLocation() == null) {
                        LOGGER.severe("Resource not found: /com/example/scribble/nav_bar.fxml");
                        showAlert("Error", "Navigation bar resource not found.");
                        return;
                    }
                    Parent navRoot = navLoader.load();
                    nav_bar__c navController = navLoader.getController();
                    if (navController == null) {
                        LOGGER.severe("nav_bar__c controller is null");
                        showAlert("Error", "Failed to initialize navigation bar controller.");
                        return;
                    }
                    // Set the current page content into navController's centerPane
                    navController.getCenterPane().getChildren().setAll(back_button.getScene().getRoot());
                    // Use 1400x760 to match nav_bar__c's expected dimensions
                    Scene newScene = new Scene(navRoot, 1400, 760);
                    stage.setScene(newScene);
                    mainController = navController; // Update mainController to avoid further issues
                    LOGGER.info("Set up fallback scene with nav_bar.fxml, width: 1400, height: 760");
                } catch (IOException e) {
                    LOGGER.severe("Failed to set up fallback scene: " + e.getMessage());
                    showAlert("Error", "Failed to set up navigation: " + e.getMessage());
                }
            });
        }

        // Rest of the initialize method remains unchanged
        if (AppState_c.getInstance().getPreviousFXML() == null) {
            AppState_c.getInstance().setPreviousFXML("/com/example/scribble/contest.fxml");
            LOGGER.info("Set previousFXML to /com/example/scribble/contest.fxml in initialize");
        }
        genre_combox.getItems().addAll("Fantasy", "Thriller Mystery", "Youth Fiction", "Crime Horror");
        genre_combox.setValue("Fantasy");
        genre_combox.setOnAction(event -> {
            selectedGenre = genre_combox.getValue();
            currentWeekIndex = 0;
            loadWeekData();
            updateDisplay();
        });
        go_left.setOnAction(event -> {
            if (currentWeekIndex < weekDataList.size() - 1) {
                currentWeekIndex++;
                LOGGER.info("Navigating to older week, index: " + currentWeekIndex);
                updateDisplayWithSwap();
            }
        });
        go_right.setOnAction(event -> {
            if (currentWeekIndex > 0) {
                currentWeekIndex--;
                LOGGER.info("Navigating to newer week, index: " + currentWeekIndex);
                updateDisplayWithSwap();
            }
        });
        setupBookCardHandlers(first_image, first_title, first_open, 0);
        setupBookCardHandlers(second_image, second_title, second_open, 1);
        setupBookCardHandlers(third_image, third_title, third_open, 2);
        loadWeekData();
        updateDisplay();
    }

    private void setupBookCardHandlers(ImageView image, Label title, Button openButton, int entryIndex) {
        image.setOnMouseClicked(event -> openBook(entryIndex));
        title.setOnMouseClicked(event -> openBook(entryIndex));
        openButton.setOnAction(event -> openBook(entryIndex));
    }

    private void openBook(int entryIndex) {
        if (weekDataList == null || currentWeekIndex >= weekDataList.size() || weekDataList.get(currentWeekIndex).entries.size() <= entryIndex) {
            LOGGER.warning("No entry available for index: " + entryIndex);
            showErrorAlert("Error", "No book available to open.");
            return;
        }
        Entry entry = weekDataList.get(currentWeekIndex).entries.get(entryIndex);
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/scribble/contest_read_entry.fxml"));
            if (loader.getLocation() == null) {
                LOGGER.severe("Resource not found: /com/example/scribble/contest_read_entry.fxml");
                showErrorAlert("Resource Error", "Book reader page not found.");
                return;
            }
            Parent root = loader.load();
            contest_read_entry__c controller = loader.getController();
            controller.initData(entry.entryId);
            controller.setMainController(mainController);
            if (mainController != null) {
                mainController.getCenterPane().getChildren().setAll(root);
                LOGGER.info("Navigated to contest_read_entry.fxml for entryId: " + entry.entryId);
            } else {
                Scene scene = new Scene(root, 1400, 660);
                Stage stage = (Stage) back_button.getScene().getWindow();
                stage.setScene(scene);
                LOGGER.info("Opened contest_read_entry.fxml for entryId: " + entry.entryId + " in standalone mode");
            }
        } catch (IOException e) {
            LOGGER.severe("Failed to open book: " + e.getMessage());
            showErrorAlert("Navigation Error", "Failed to open book: " + e.getMessage());
        }
    }

    @FXML
    private void handle_back_button() {
        try {
            String previousFXML = AppState_c.getInstance().getPreviousFXML();
            LOGGER.info("Retrieved previousFXML: " + previousFXML);
            if (previousFXML == null || previousFXML.isEmpty() || previousFXML.equals("/com/example/scribble/contest_weekly_results.fxml")) {
                previousFXML = "/com/example/scribble/contest.fxml";
                LOGGER.warning("Invalid or self-referential previous FXML: " + AppState_c.getInstance().getPreviousFXML() + ", defaulting to: " + previousFXML);
                AppState_c.getInstance().setPreviousFXML(previousFXML); // Ensure state is updated
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource(previousFXML));
            if (loader.getLocation() == null) {
                LOGGER.severe("Resource not found: " + previousFXML);
                showErrorAlert("Resource Error", "Previous page resource not found: " + previousFXML);
                return;
            }

            Parent page = loader.load();
            Object controller = loader.getController();

            try {
                if (controller != null && mainController != null) {
                    java.lang.reflect.Method setMainControllerMethod = controller.getClass().getMethod("setMainController", nav_bar__c.class);
                    setMainControllerMethod.invoke(controller, mainController);
                    LOGGER.info("setMainController called on controller: " + controller.getClass().getName());
                }
            } catch (NoSuchMethodException | IllegalAccessException | java.lang.reflect.InvocationTargetException e) {
                LOGGER.info("Target controller does not support setMainController: " + (controller != null ? controller.getClass().getName() : "null"));
            }

            if (mainController != null) {
                mainController.getCenterPane().getChildren().setAll(page);
                LOGGER.info("Navigated back to " + previousFXML + " using mainController");
            } else {
                LOGGER.warning("mainController is null, loading nav_bar.fxml to preserve navigation bar");
                FXMLLoader navLoader = new FXMLLoader(getClass().getResource("/com/example/scribble/nav_bar.fxml"));
                if (navLoader.getLocation() == null) {
                    LOGGER.severe("nav_bar.fxml not found");
                    showErrorAlert("Resource Error", "Navigation bar resource not found");
                    return;
                }
                Parent navRoot = navLoader.load();
                nav_bar__c navController = navLoader.getController();
                navController.getCenterPane().getChildren().setAll(page);
                Scene scene = new Scene(navRoot, 1400, 660);
                Stage stage = (Stage) back_button.getScene().getWindow();
                stage.setScene(scene);
                LOGGER.info("Navigated back to " + previousFXML + " with nav_bar.fxml");
            }
        } catch (IOException e) {
            LOGGER.severe("Failed to navigate back to " + AppState_c.getInstance().getPreviousFXML() + ": " + e.getMessage());
            showErrorAlert("Error", "Failed to navigate back: " + e.getMessage());
        }
    }

    private void loadGenres() {
        // Hardcode the four genres
        genre_combox.getItems().clear();
        genre_combox.getItems().addAll("Fantasy", "Thriller Mystery", "Youth Fiction", "Crime Horror");
        LOGGER.info("Loaded genres: " + genre_combox.getItems());
    }

    private LocalDateTime getCurrentWeekStart() {
        // Explicitly set to July 12, 2025, as current week
        return LocalDateTime.of(2025, 7, 12, 0, 0, 0);
    }

    private LocalDateTime getPreviousWeekStart() {
        // Explicitly set to July 5, 2025, as previous week
        return LocalDateTime.of(2025, 7, 5, 0, 0, 0);
    }

    private void loadWeekData() {
        weekDataList = new ArrayList<>();
        try (Connection conn = db_connect.getConnection()) {
            if (conn == null) {
                LOGGER.severe("Database connection is null, cannot load week data");
                showErrorAlert("Database Error", "Failed to connect to the database.");
                return;
            }

            // Calculate the previous week's start (July 5, 2025)
            LocalDateTime previousWeekStart = getPreviousWeekStart();
            LocalDateTime currentWeekStart = getCurrentWeekStart();
            LOGGER.info("Previous week start: " + previousWeekStart.format(DATE_FORMATTER));
            LOGGER.info("Current week start (excluded): " + currentWeekStart.format(DATE_FORMATTER));

            // Fetch all unique week starts (Saturdays) for the selected genre, excluding current week
            String query = "SELECT DISTINCT DATE_SUB(ce.submission_date, INTERVAL (WEEKDAY(ce.submission_date) + 2) % 7 DAY) as week_start " +
                    "FROM contest_entries ce JOIN contests c ON ce.contest_id = c.contest_id " +
                    "WHERE c.genre = ? AND ce.submission_date < ? " +
                    "ORDER BY week_start DESC";

            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, selectedGenre);
                stmt.setTimestamp(2, Timestamp.valueOf(currentWeekStart));
                ResultSet rs = stmt.executeQuery();
                Set<LocalDateTime> uniqueWeekStarts = new HashSet<>();
                while (rs.next()) {
                    LocalDateTime weekStart = rs.getTimestamp("week_start").toLocalDateTime();
                    // Adjust to Saturday
                    int daysSinceSaturday = (weekStart.getDayOfWeek().getValue() + 1) % 7;
                    weekStart = weekStart.minusDays(daysSinceSaturday).withHour(0).withMinute(0).withSecond(0).withNano(0);
                    uniqueWeekStarts.add(weekStart);
                }

                // Load entries for each unique week
                for (LocalDateTime weekStart : uniqueWeekStarts) {
                    List<Entry> entries = loadEntriesForWeek(weekStart);
                    weekDataList.add(new WeekData(weekStart, entries));
                }
                LOGGER.info("Loaded " + weekDataList.size() + " unique weeks for genre: " + selectedGenre);

                // Sort weeks in descending order (newest first)
                weekDataList.sort((a, b) -> b.startDate.compareTo(a.startDate));

                // Set currentWeekIndex to previous week (July 5–11, 2025)
                currentWeekIndex = -1;
                for (int i = 0; i < weekDataList.size(); i++) {
                    if (weekDataList.get(i).startDate.truncatedTo(ChronoUnit.DAYS).equals(previousWeekStart.truncatedTo(ChronoUnit.DAYS))) {
                        currentWeekIndex = i;
                        break;
                    }
                }
                if (currentWeekIndex == -1) {
                    LOGGER.warning("Previous week " + previousWeekStart.format(DATE_FORMATTER) + " not found in weekDataList");
                    currentWeekIndex = weekDataList.isEmpty() ? 0 : 0; // Default to most recent week
                }
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
                    // Fix image path
                    if (coverPhoto == null || coverPhoto.trim().isEmpty()) {
                        coverPhoto = DEFAULT_COVER;
                    } else if (!coverPhoto.startsWith("/")) {
                        coverPhoto = "/images/contest_book_cover/" + coverPhoto;
                    }
                    // Verify resource exists
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

    private void updateDisplayWithSwap() {
        // Apply fade-out transition to book card VBoxes
        FadeTransition fadeOutFirst = new FadeTransition(Duration.millis(200), first_card);
        fadeOutFirst.setFromValue(1.0);
        fadeOutFirst.setToValue(0.0);

        FadeTransition fadeOutSecond = new FadeTransition(Duration.millis(200), second_card);
        fadeOutSecond.setFromValue(1.0);
        fadeOutSecond.setToValue(0.0);

        FadeTransition fadeOutThird = new FadeTransition(Duration.millis(200), third_card);
        fadeOutThird.setFromValue(1.0);
        fadeOutThird.setToValue(0.0);

        // Apply fade-in transition after update
        FadeTransition fadeInFirst = new FadeTransition(Duration.millis(200), first_card);
        fadeInFirst.setFromValue(0.0);
        fadeInFirst.setToValue(1.0);

        FadeTransition fadeInSecond = new FadeTransition(Duration.millis(200), second_card);
        fadeInSecond.setFromValue(0.0);
        fadeInSecond.setToValue(1.0);

        FadeTransition fadeInThird = new FadeTransition(Duration.millis(200), third_card);
        fadeInThird.setFromValue(0.0);
        fadeInThird.setToValue(1.0);

        // Play fade-out, update, then fade-in
        fadeOutFirst.setOnFinished(event -> {
            updateDisplay();
            fadeInFirst.play();
            fadeInSecond.play();
            fadeInThird.play();
        });

        fadeOutFirst.play();
        fadeOutSecond.play();
        fadeOutThird.play();
    }

    private void updateDisplay() {
        Platform.runLater(() -> {
            if (weekDataList == null || weekDataList.isEmpty() || currentWeekIndex >= weekDataList.size()) {
                LOGGER.warning("No week data available, currentWeekIndex: " + currentWeekIndex + ", weekDataList size: " + (weekDataList != null ? weekDataList.size() : 0));
                clearDisplay();
                return;
            }

            WeekData weekData = weekDataList.get(currentWeekIndex);
            LocalDateTime startDate = weekData.startDate;
            LocalDateTime endDate = startDate.plusDays(6);
            String weekText = startDate.format(DATE_FORMATTER) + " - " + endDate.format(DATE_FORMATTER);
            week_session.setText(weekData.entries.isEmpty() ? "No entries for this week" : weekText);
            LOGGER.info("Updating week_session to: " + week_session.getText());

            // Update first place
            if (weekData.entries.size() > 0) {
                Entry entry = weekData.entries.get(0);
                first_title.setText(entry.title);
                first_author.setText("by " + entry.author);
                first_votes.setText("Total Votes: " + entry.voteCount);
                first_open.setDisable(false);
                try {
                    String resourcePath = getClass().getResource(entry.coverPhoto).toExternalForm();
                    LOGGER.info("Attempting to load first place image: " + entry.coverPhoto + ", resourcePath: " + resourcePath);
                    first_image.setImage(new Image(resourcePath));
                } catch (Exception e) {
                    LOGGER.warning("Failed to load image for first place: " + entry.coverPhoto + ", error: " + e.getMessage());
                    first_image.setImage(new Image(getClass().getResource(DEFAULT_COVER).toExternalForm()));
                }
            } else {
                clearFirstPlace();
            }

            // Update second place
            if (weekData.entries.size() > 1) {
                Entry entry = weekData.entries.get(1);
                second_title.setText(entry.title);
                second_author.setText("by " + entry.author);
                second_votes.setText("Total Votes: " + entry.voteCount);
                second_open.setDisable(false);
                try {
                    String resourcePath = getClass().getResource(entry.coverPhoto).toExternalForm();
                    LOGGER.info("Attempting to load second place image: " + entry.coverPhoto + ", resourcePath: " + resourcePath);
                    second_image.setImage(new Image(resourcePath));
                } catch (Exception e) {
                    LOGGER.warning("Failed to load image for second place: " + entry.coverPhoto + ", error: " + e.getMessage());
                    second_image.setImage(new Image(getClass().getResource(DEFAULT_COVER).toExternalForm()));
                }
            } else {
                clearSecondPlace();
            }

            // Update third place
            if (weekData.entries.size() > 2) {
                Entry entry = weekData.entries.get(2);
                third_title.setText(entry.title);
                third_author.setText("by " + entry.author);
                third_votes.setText("Total Votes: " + entry.voteCount);
                third_open.setDisable(false);
                try {
                    String resourcePath = getClass().getResource(entry.coverPhoto).toExternalForm();
                    LOGGER.info("Attempting to load third place image: " + entry.coverPhoto + ", resourcePath: " + resourcePath);
                    third_image.setImage(new Image(resourcePath));
                } catch (Exception e) {
                    LOGGER.warning("Failed to load image for third place: " + entry.coverPhoto + ", error: " + e.getMessage());
                    third_image.setImage(new Image(getClass().getResource(DEFAULT_COVER).toExternalForm()));
                }
            } else {
                clearThirdPlace();
            }

            // Update button states
            go_left.setDisable(currentWeekIndex >= weekDataList.size() - 1);
            go_right.setDisable(currentWeekIndex <= 0);
            LOGGER.info("Navigation buttons updated: go_left=" + go_left.isDisable() + ", go_right=" + go_right.isDisable());
        });
    }

    private void clearDisplay() {
        week_session.setText("No entries for this week");
        clearFirstPlace();
        clearSecondPlace();
        clearThirdPlace();
        go_left.setDisable(true);
        go_right.setDisable(true);
        LOGGER.info("Cleared display due to no results");
    }

    private void clearFirstPlace() {
        first_title.setText("No entry");
        first_author.setText("");
        first_votes.setText("");
        first_image.setImage(null);
        first_open.setDisable(true);
    }

    private void clearSecondPlace() {
        second_title.setText("No entry");
        second_author.setText("");
        second_votes.setText("");
        second_image.setImage(null);
        second_open.setDisable(true);
    }

    private void clearThirdPlace() {
        third_title.setText("No entry");
        third_author.setText("");
        third_votes.setText("");
        third_image.setImage(null);
        third_open.setDisable(true);
    }

    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        Label contentLabel = new Label(message);
        contentLabel.setWrapText(true);
        alert.getDialogPane().setContent(contentLabel);
        alert.getDialogPane().setMinWidth(450);
        alert.getDialogPane().setPrefWidth(500);
        alert.showAndWait();
    }
}