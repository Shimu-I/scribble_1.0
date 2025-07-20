package com.example.scribble;

import javafx.event.ActionEvent;

import java.time.temporal.ChronoUnit;
import java.util.concurrent.atomic.AtomicReference;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Timer;
import java.util.TimerTask;
import javafx.fxml.Initializable;
import java.util.ResourceBundle;
import java.util.logging.Logger;
import java.sql.Timestamp;

public class contest_entries__c implements Initializable {

    private static final Logger LOGGER = Logger.getLogger(contest_entries__c.class.getName());

    @FXML private Button back_button;
    @FXML private Button add_entry;
    @FXML private VBox entryContainer;
    @FXML private Label genre_name;
    @FXML private Button previous_week_button;
    @FXML private Button current_week_button;
    @FXML private Label weekly_session;
    @FXML private Label countdown;
    @FXML private ComboBox<String> filter_combo_box;
    @FXML private HBox entry_hbox; // Added for FXML alignment
    @FXML private HBox entry_no_hbox; // Added for FXML alignment
    @FXML private Label entry_no; // Added for FXML alignment
    @FXML private Label title_of_the_content; // Added for FXML alignment
    @FXML private Label author_name; // Added for FXML alignment
    @FXML private Label submited_date; // Added for FXML alignment
    @FXML private Label voted_by_no_of_people; // Added for FXML alignment
    @FXML private Button open_entry; // Added for FXML alignment
    @FXML private Button not_voted_button; // Added for FXML alignment
    @FXML private Button voted_button; // Added for FXML alignment

    private int contestId;
    private String genre;
    private int userId;
    private String username;
    private boolean isCurrentWeekView = true;
    private String currentSortField = "submission_date";
    private boolean isAscending = false;

    @FXML private nav_bar__c mainController;

    public void setMainController(nav_bar__c mainController) {
        this.mainController = mainController;
        LOGGER.info("Set mainController in contest_entries__c: " + mainController);
    }

    public void initData(int contestId, String genre, int userId, String username, boolean isCurrentWeekView) {
        this.contestId = contestId;
        this.genre = genre;
        this.userId = userId;
        this.username = username;
        this.isCurrentWeekView = isCurrentWeekView; // Set the view state
        updateWeeklySessionLabel(isCurrentWeekView);
        updateButtonStyles(); // Update button styles based on view state
        startCountdownTimer();
        initializeComboBox();
        loadEntries();
    }

    private void updateButtonStyles() {
        if (current_week_button != null) {
            current_week_button.setStyle(isCurrentWeekView
                    ? "-fx-background-color: #C9B8A9; -fx-background-radius: 0 10 10 0; -fx-text-fill: #014237;"
                    : "-fx-background-color: #F5E0CD; -fx-background-radius: 0 10 10 0; -fx-text-fill: #014237;");
        }
        if (previous_week_button != null) {
            previous_week_button.setStyle(isCurrentWeekView
                    ? "-fx-background-color: #F5E0CD; -fx-background-radius: 10 0 0 10; -fx-text-fill: #014237;"
                    : "-fx-background-color: #C9B8A9; -fx-background-radius: 10 0 0 10; -fx-text-fill: #014237;");
        }
        if (add_entry != null) {
            add_entry.setDisable(!isCurrentWeekView); // Disable add_entry button for previous week
        }
    }

    public void setContestId(int contestId, String genre) {
        this.contestId = contestId;
        this.genre = genre;
        this.userId = UserSession.getInstance().getUserId();
        this.username = UserSession.getInstance().getUsername();
        updateWeeklySessionLabel(true);
        updateGenreLabel();
        startCountdownTimer();
        initializeComboBox();
        loadEntries();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (previous_week_button != null) {
            previous_week_button.setOnMouseEntered(e -> previous_week_button.setStyle("-fx-background-color: #C9B8A9; -fx-background-radius: 10 0 0 10; -fx-text-fill: #014237; -fx-translate-y: -2px;"));
            previous_week_button.setOnMouseExited(e -> previous_week_button.setStyle(isCurrentWeekView ? "-fx-background-color: #F5E0CD; -fx-background-radius: 10 0 0 10; -fx-text-fill: #014237;" : "-fx-background-color: #C9B8A9; -fx-background-radius: 10 0 0 10; -fx-text-fill: #014237;"));
        }
        if (current_week_button != null) {
            current_week_button.setOnMouseEntered(e -> current_week_button.setStyle("-fx-background-color: #C9B8A9; -fx-background-radius: 0 10 10 0; -fx-text-fill: #014237; -fx-translateji-y: -2px;"));
            current_week_button.setOnMouseExited(e -> current_week_button.setStyle(isCurrentWeekView ? "-fx-background-color: #C9B8A9; -fx-background-radius: 0 10 10 0; -fx-text-fill: #014237;" : "-fx-background-color: #F5E0CD; -fx-background-radius: 0 10 10 0; -fx-text-fill: #014237;"));
        }
        initializeComboBox();
        // Initialize sample entry if present
        if (entry_hbox != null) {
            setupSampleEntry();
        }
    }

    private void setupSampleEntry() {
        // Configure the sample entry_hbox from FXML as a template
        if (entry_no != null) {
            entry_no.setText("1");
        }
        if (title_of_the_content != null) {
            title_of_the_content.setText("Sample Title");
        }
        if (author_name != null) {
            author_name.setText("by Sample Author");
        }
        if (submited_date != null) {
            submited_date.setText("Uploaded on " + new SimpleDateFormat("MM/dd/yyyy").format(new java.util.Date()));
        }
        if (voted_by_no_of_people != null) {
            voted_by_no_of_people.setText("Received 0 votes");
        }
        if (not_voted_button != null && voted_button != null) {
            not_voted_button.setVisible(true);
            voted_button.setVisible(false);
        }
        // Ensure entry_hbox is not added to entryContainer unless needed
        if (entryContainer != null && !entryContainer.getChildren().contains(entry_hbox)) {
            entryContainer.getChildren().clear();
            entryContainer.getChildren().add(entry_hbox);
        }
    }

    private void updateGenreLabel() {
        if (genre_name != null && genre != null) {
            genre_name.setText(genre);
        } else if (genre_name == null) {
            LOGGER.severe("genre_name label is not injected from FXML.");
        } else {
            LOGGER.info("Skipped genre label update: genre_name=" + genre_name + ", genre=" + genre);
        }
    }

    @FXML
    private void handle_back_button(ActionEvent event) {
        if (mainController == null) {
            LOGGER.severe("Main controller is null, cannot navigate back from contest entries page");
            showErrorAlert("Error", "Navigation failed: main controller is not initialized.");
            return;
        }

        try {
            String previousFXML = AppState_c.getInstance().getPreviousFXML();
            if (previousFXML == null || previousFXML.isEmpty() || previousFXML.equals("/com/example/scribble/contest_entries.fxml")) {
                previousFXML = "/com/example/scribble/contest.fxml";
                LOGGER.warning("Invalid or self-referential previous FXML: " + AppState_c.getInstance().getPreviousFXML() + ", defaulting to: " + previousFXML);
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

            if (controller != null) {
                try {
                    java.lang.reflect.Method setMainControllerMethod = controller.getClass().getMethod("setMainController", nav_bar__c.class);
                    setMainControllerMethod.invoke(controller, mainController);
                    LOGGER.info("setMainController called on controller: " + controller.getClass().getName());

                    // Initialize contest_entries__c with preserved state
                    if (controller instanceof contest_entries__c entriesController && previousFXML.equals("/com/example/scribble/contest_entries.fxml")) {
                        entriesController.initData(contestId, genre, userId, username, isCurrentWeekView);
                        LOGGER.info("Initialized contest_entries__c with contestId=" + contestId + ", genre=" + genre + ", isCurrentWeekView=" + isCurrentWeekView);
                    }
                } catch (NoSuchMethodException | IllegalAccessException | java.lang.reflect.InvocationTargetException e) {
                    LOGGER.info("Target controller does not support setMainController: " + (controller != null ? controller.getClass().getName() : "null") + ", error: " + e.getMessage());
                }
            } else {
                LOGGER.warning("Controller is null for FXML: " + previousFXML);
            }

            mainController.getCenterPane().getChildren().setAll(root);
            LOGGER.info("Navigated back to " + previousFXML);
        } catch (IOException e) {
            LOGGER.severe("Failed to navigate back to previous FXML: " + e.getMessage());
            showErrorAlert("Navigation Error", "Failed to return to the previous page: " + e.getMessage());
        }
    }

    @FXML
    private void handle_add_entry(ActionEvent event) {
        if (!UserSession.getInstance().isLoggedIn()) {
            showErrorAlert("Session Error", "You must be logged in to add an entry.");
            LOGGER.severe("User not logged in, cannot navigate to contest_write.fxml");
            return;
        }
        if (userId != UserSession.getInstance().getUserId()) {
            showErrorAlert("Session Error", "User ID mismatch. Please log in with the correct account.");
            LOGGER.severe("User ID mismatch: userId=" + userId + ", session userId=" + UserSession.getInstance().getUserId());
            return;
        }

        LocalDateTime weekStart = getCurrentWeekStart();
        LocalDateTime weekEnd = weekStart.plusDays(6).withHour(23).withMinute(59).withSecond(59);
        try (Connection conn = db_connect.getConnection()) {
            if (conn == null) {
                LOGGER.severe("Database connection is null, cannot check existing entry.");
                showErrorAlert("Database Error", "Failed to connect to the database.");
                return;
            }
            String query = "SELECT COUNT(*) FROM contest_entries WHERE contest_id = ? AND user_id = ? AND submission_date BETWEEN ? AND ?";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setInt(1, contestId);
                stmt.setInt(2, userId);
                stmt.setTimestamp(3, Timestamp.valueOf(weekStart));
                stmt.setTimestamp(4, Timestamp.valueOf(weekEnd));
                ResultSet rs = stmt.executeQuery();
                if (rs.next() && rs.getInt(1) > 0) {
                    showErrorAlert("Submission Error", "You have already added your contest entry for this week in this genre.");
                    LOGGER.warning("User " + userId + " attempted to submit multiple entries for contestId=" + contestId);
                    return;
                }
            }
        } catch (SQLException e) {
            LOGGER.severe("Failed to check existing entry: " + e.getMessage());
            showErrorAlert("Database Error", "Failed to verify submission status: " + e.getMessage());
            return;
        }

        try (Connection conn = db_connect.getConnection()) {
            if (conn == null) {
                LOGGER.severe("Database connection is null, cannot check winner cooldown.");
                showErrorAlert("Database Error", "Failed to connect to the database.");
                return;
            }
            LocalDateTime currentWeekStart = getCurrentWeekStart();
            LocalDateTime previousWeekStart = currentWeekStart.minusWeeks(1);
            LocalDateTime previousWeekEnd = previousWeekStart.plusDays(6);
            String query = "SELECT ce.submission_date FROM contest_entries ce " +
                    "JOIN (SELECT contest_id, submission_date, vote_count, " +
                    "RANK() OVER (PARTITION BY contest_id ORDER BY vote_count DESC, (SELECT MIN(created_at) FROM contest_votes cv WHERE cv.contest_entry_id = ce.entry_id) ASC) as rnk " +
                    "FROM contest_entries ce WHERE ce.user_id = ? AND ce.contest_id = ? AND ce.submission_date BETWEEN ? AND ?) as ranked " +
                    "WHERE ranked.rnk <= 3 AND ce.contest_id = ranked.contest_id AND ce.submission_date = ranked.submission_date";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setInt(1, userId);
                stmt.setInt(2, contestId);
                stmt.setTimestamp(3, Timestamp.valueOf(previousWeekStart));
                stmt.setTimestamp(4, Timestamp.valueOf(previousWeekEnd));
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    LocalDateTime winDate = rs.getTimestamp("submission_date").toLocalDateTime();
                    LocalDateTime cooldownStart = currentWeekStart;
                    LocalDateTime eligibleDate = cooldownStart.plusDays(14);
                    LocalDateTime now = LocalDateTime.now(ZoneOffset.ofHours(6));
                    if (now.isBefore(eligibleDate)) {
                        long daysRemaining = ChronoUnit.DAYS.between(now, eligibleDate);
                        String fullMessage = "You achieved a top position last week. Please wait 2 weeks from " + cooldownStart.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) + " before submitting a new entry. Days remaining: " + daysRemaining;
                        LOGGER.info("Full cooldown message: " + fullMessage);
                        showErrorAlert("Submission Error", fullMessage);
                        LOGGER.warning("User " + userId + " is in cooldown until " + eligibleDate + " for contestId=" + contestId);
                        return;
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.severe("Failed to check winner cooldown: " + e.getMessage());
            showErrorAlert("Database Error", "Failed to verify cooldown status: " + e.getMessage());
            return;
        }

        if (mainController == null) {
            LOGGER.severe("Main controller is null, cannot navigate to contest_write.fxml");
            showErrorAlert("Error", "Navigation failed: main controller is not initialized.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/scribble/contest_write.fxml"));
            if (loader.getLocation() == null) {
                LOGGER.severe("Resource not found: /com/example/scribble/contest_write.fxml");
                showErrorAlert("Resource Error", "Contest writing page resource not found.");
                return;
            }
            Parent root = loader.load();
            contest_write__c controller = loader.getController();
            controller.initData(contestId, genre, userId, username, UserSession.getInstance().getUserPhotoPath());
            controller.setMainController(mainController);
            AppState_c.getInstance().setPreviousFXML("/com/example/scribble/contest_entries.fxml");
            mainController.getCenterPane().getChildren().setAll(root);
            LOGGER.info("Successfully navigated to contest_write.fxml");
        } catch (IOException e) {
            LOGGER.severe("Failed to navigate to contest_write.fxml: " + e.getMessage());
            showErrorAlert("Navigation Error", "Failed to open the entry submission page: " + e.getMessage());
        }
    }

    @FXML
    private void handle_not_voted_button(ActionEvent event) {
        if (!UserSession.getInstance().isLoggedIn()) {
            showErrorAlert("Session Error", "You must be logged in to vote.");
            LOGGER.severe("User not logged in, cannot vote.");
            return;
        }
        Button source = (Button) event.getSource();
        HBox entryHBox = findEntryHBox(source);
        if (entryHBox == null) {
            LOGGER.severe("Failed to find entry HBox for voting button.");
            showErrorAlert("UI Error", "Unable to process vote due to UI structure issue.");
            return;
        }
        int entryId = getEntryIdFromHBox(entryHBox);
        if (isOwnEntry(entryId)) {
            showErrorAlert("Vote Error", "You cannot vote for your own entry.");
            LOGGER.warning("User " + UserSession.getInstance().getUserId() + " attempted to vote for their own entryId=" + entryId);
            return;
        }
        if (hasUserVoted(entryId)) {
            showErrorAlert("Vote Error", "You have already voted for this entry.");
            LOGGER.warning("User " + UserSession.getInstance().getUserId() + " attempted to vote again for entryId=" + entryId);
            return;
        }
        addVote(entryId, entryHBox);
    }

    @FXML
    private void handle_voted_button(ActionEvent event) {
        if (!UserSession.getInstance().isLoggedIn()) {
            showErrorAlert("Session Error", "You must be logged in to remove a vote.");
            LOGGER.severe("User not logged in, cannot remove vote.");
            return;
        }
        Button source = (Button) event.getSource();
        HBox entryHBox = findEntryHBox(source);
        if (entryHBox == null) {
            LOGGER.severe("Failed to find entry HBox for voting button.");
            showErrorAlert("UI Error", "Unable to process vote removal due to UI structure issue.");
            return;
        }
        int entryId = getEntryIdFromHBox(entryHBox);
        if (!hasUserVoted(entryId)) {
            showErrorAlert("Vote Error", "You have not voted for this entry.");
            LOGGER.warning("User " + UserSession.getInstance().getUserId() + " attempted to remove non-existent vote for entryId=" + entryId);
            return;
        }
        removeVote(entryId, entryHBox);
    }

    @FXML
    private void handle_open_entry(ActionEvent event) {
        if (!UserSession.getInstance().isLoggedIn()) {
            showErrorAlert("Session Error", "You must be logged in to view entries.");
            LOGGER.severe("User not logged in, cannot navigate to contest_read_entry.fxml");
            return;
        }
        if (mainController == null) {
            LOGGER.severe("Main controller is null in contest_entries__c, cannot navigate to contest_read_entry.fxml");
            showErrorAlert("Error", "Navigation failed: main controller is not initialized in contest_entries__c.");
            return;
        }

        Button source = (Button) event.getSource();
        HBox entryHBox = findEntryHBox(source);
        if (entryHBox == null) {
            LOGGER.severe("Failed to find entry HBox for open button.");
            showErrorAlert("UI Error", "Unable to open entry due to UI structure issue.");
            return;
        }
        int entryId = getEntryIdFromHBox(entryHBox);

        LOGGER.info("Initiating navigation to contest_read_entry.fxml for entryId: " + entryId + " with mainController: " + mainController);
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/scribble/contest_read_entry.fxml"));
            if (loader.getLocation() == null) {
                LOGGER.severe("Resource not found: /com/example/scribble/contest_read_entry.fxml");
                showErrorAlert("Resource Error", "Contest read entry page resource not found.");
                return;
            }
            Parent root = loader.load();
            contest_read_entry__c controller = loader.getController();
            controller.initData(entryId, isCurrentWeekView); // Pass isCurrentWeekView
            controller.setMainController(mainController);
            LOGGER.info("mainController set in contest_read_entry__c: " + mainController + ", isCurrentWeekView=" + isCurrentWeekView);
            AppState_c.getInstance().setPreviousFXML("/com/example/scribble/contest_entries.fxml");
            mainController.getCenterPane().getChildren().setAll(root);
            LOGGER.info("Successfully navigated to contest_read_entry.fxml for entryId: " + entryId);
        } catch (IOException e) {
            LOGGER.severe("Failed to navigate to contest_read_entry.fxml: " + e.getMessage());
            showErrorAlert("Navigation Error", "Failed to open the entry page: " + e.getMessage());
        }
    }

    private void loadEntries() {
        if (entryContainer == null) {
            LOGGER.severe("entryContainer is null; cannot load entries.");
            return;
        }
        entryContainer.getChildren().clear();
        if (contestId <= 0) {
            showErrorAlert("Invalid Contest", "Invalid contest ID: " + contestId);
            return;
        }

        LocalDateTime weekStart = isCurrentWeekView ? getCurrentWeekStart() : getPreviousWeekStart();
        LocalDateTime weekEnd = weekStart.plusDays(6).withHour(23).withMinute(59).withSecond(59);
        Timestamp weekStartTs = Timestamp.valueOf(weekStart);
        Timestamp weekEndTs = Timestamp.valueOf(weekEnd);

        try (Connection conn = db_connect.getConnection()) {
            if (conn == null) {
                showErrorAlert("Database Error", "Failed to connect to the database.");
                return;
            }

            String query = isCurrentWeekView ?
                    "SELECT ce.entry_id, ce.entry_title, ce.submission_date, ce.vote_count, ce.cover_photo, u.username " +
                            "FROM contest_entries ce JOIN users u ON ce.user_id = u.user_id " +
                            "WHERE ce.contest_id = ? AND ce.submission_date BETWEEN ? AND ? " +
                            "ORDER BY ce.submission_date ASC" :
                    "SELECT ce.entry_id, ce.entry_title, ce.submission_date, ce.vote_count, ce.cover_photo, u.username, " +
                            "(SELECT MIN(created_at) FROM contest_votes cv WHERE cv.contest_entry_id = ce.entry_id) as first_vote " +
                            "FROM contest_entries ce JOIN users u ON ce.user_id = u.user_id " +
                            "WHERE ce.contest_id = ? AND ce.submission_date BETWEEN ? AND ? " +
                            "ORDER BY ce.vote_count DESC, first_vote ASC";

            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setInt(1, contestId);
                stmt.setTimestamp(2, weekStartTs);
                stmt.setTimestamp(3, weekEndTs);
                LOGGER.info("Executing query for contestId: " + contestId + ", week: " + weekStartTs + " to " + weekEndTs);
                ResultSet rs = stmt.executeQuery();
                int entryNumber = 1;
                while (rs.next()) {
                    HBox entryHBox = createEntryHBox(
                            rs.getInt("entry_id"),
                            entryNumber,
                            rs.getString("entry_title"),
                            rs.getString("username"),
                            rs.getTimestamp("submission_date"),
                            rs.getInt("vote_count"),
                            rs.getString("cover_photo")
                    );
                    if (!isCurrentWeekView && entryNumber <= 3 && rs.getInt("vote_count") > 0) {
                        HBox numberBox = (HBox) entryHBox.getChildren().get(0);
                        String backgroundColor;
                        if (entryNumber == 1) {
                            backgroundColor = "#721415";
                        } else if (entryNumber == 2) {
                            backgroundColor = "#AC2324";
                        } else {
                            backgroundColor = "#BA3D3E";
                        }
                        numberBox.setStyle("-fx-background-color: " + backgroundColor + "; -fx-background-radius: 20; -fx-border-width: 2;");
                    }
                    entryContainer.getChildren().add(entryHBox);
                    entryNumber++;
                }
                if (entryNumber == 1) {
                    LOGGER.info("No entries found for contestId: " + contestId + ", week: " + weekStartTs + " to " + weekEndTs);
                }
            }
        } catch (SQLException e) {
            LOGGER.severe("Failed to load contest entries: " + e.getMessage());
            showErrorAlert("Database Error", "Failed to load contest entries: " + e.getMessage());
        }
    }

    private void initializeComboBox() {
        if (filter_combo_box == null) {
            LOGGER.severe("filter_combo_box is null; cannot initialize ComboBox.");
            return;
        }
        filter_combo_box.getItems().clear();
        filter_combo_box.getItems().addAll(
                "Submission Date (Desc)",
                "Submission Date (Asc)",
                "Book Name (Asc)",
                "Book Name (Desc)",
                "Votes (Asc)",
                "Votes (Desc)"
        );
        filter_combo_box.setValue("Submission Date (Desc)"); // Corrected typo from FXML prompt
        currentSortField = "submission_date";
        isAscending = false;
        filter_combo_box.setOnAction(event -> {
            String selected = filter_combo_box.getValue();
            if (selected != null) {
                switch (selected) {
                    case "Submission Date (Asc)":
                        currentSortField = "submission_date";
                        isAscending = true;
                        break;
                    case "Submission Date (Desc)":
                        currentSortField = "submission_date";
                        isAscending = false;
                        break;
                    case "Book Name (Asc)":
                        currentSortField = "entry_title";
                        isAscending = true;
                        break;
                    case "Book Name (Desc)":
                        currentSortField = "entry_title";
                        isAscending = false;
                        break;
                    case "Votes (Asc)":
                        currentSortField = "vote_count";
                        isAscending = true;
                        break;
                    case "Votes (Desc)":
                        currentSortField = "vote_count";
                        isAscending = false;
                        break;
                    default:
                        LOGGER.warning("Unknown sort option selected: " + selected);
                        return;
                }
                loadSortedEntries();
            }
        });
        LOGGER.info("ComboBox initialized with options: " + filter_combo_box.getItems());
    }

    private void loadSortedEntries() {
        if (entryContainer == null) {
            LOGGER.severe("entryContainer is null; cannot load entries.");
            return;
        }
        entryContainer.getChildren().clear();
        if (contestId <= 0) {
            showErrorAlert("Invalid Contest", "Invalid contest ID: " + contestId);
            return;
        }

        LocalDateTime weekStart = isCurrentWeekView ? getCurrentWeekStart() : getPreviousWeekStart();
        LocalDateTime weekEnd = weekStart.plusDays(6).withHour(23).withMinute(59).withSecond(59);
        Timestamp weekStartTs = Timestamp.valueOf(weekStart);
        Timestamp weekEndTs = Timestamp.valueOf(weekEnd);

        try (Connection conn = db_connect.getConnection()) {
            if (conn == null) {
                showErrorAlert("Database Error", "Failed to connect to the database.");
                return;
            }

            String baseQuery = isCurrentWeekView ?
                    "SELECT ce.entry_id, ce.entry_title, ce.submission_date, ce.vote_count, ce.cover_photo, u.username " +
                            "FROM contest_entries ce JOIN users u ON ce.user_id = u.user_id " +
                            "WHERE ce.contest_id = ? AND ce.submission_date BETWEEN ? AND ? " :
                    "SELECT ce.entry_id, ce.entry_title, ce.submission_date, ce.vote_count, ce.cover_photo, u.username, " +
                            "(SELECT MIN(created_at) FROM contest_votes cv WHERE cv.contest_entry_id = ce.entry_id) as first_vote " +
                            "FROM contest_entries ce JOIN users u ON ce.user_id = u.user_id " +
                            "WHERE ce.contest_id = ? AND ce.submission_date BETWEEN ? AND ? ";

            String orderByClause = isCurrentWeekView ?
                    String.format("ORDER BY ce.%s %s", currentSortField, isAscending ? "ASC" : "DESC") :
                    String.format("ORDER BY ce.%s %s, ce.vote_count DESC, first_vote ASC", currentSortField, isAscending ? "ASC" : "DESC");

            String query = baseQuery + orderByClause;

            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setInt(1, contestId);
                stmt.setTimestamp(2, weekStartTs);
                stmt.setTimestamp(3, weekEndTs);
                LOGGER.info("Executing query for contestId: " + contestId + ", week: " + weekStartTs + " to " + weekEndTs + ", sorted by: " + currentSortField + " " + (isAscending ? "ASC" : "DESC"));
                ResultSet rs = stmt.executeQuery();
                int entryNumber = 1;
                while (rs.next()) {
                    HBox entryHBox = createEntryHBox(
                            rs.getInt("entry_id"),
                            entryNumber,
                            rs.getString("entry_title"),
                            rs.getString("username"),
                            rs.getTimestamp("submission_date"),
                            rs.getInt("vote_count"),
                            rs.getString("cover_photo")
                    );
                    if (!isCurrentWeekView && entryNumber <= 3 && rs.getInt("vote_count") > 0) {
                        HBox numberBox = (HBox) entryHBox.getChildren().get(0);
                        String backgroundColor;
                        if (entryNumber == 1) {
                            backgroundColor = "#721415";
                        } else if (entryNumber == 2) {
                            backgroundColor = "#AC2324";
                        } else {
                            backgroundColor = "#BA3D3E";
                        }
                        numberBox.setStyle("-fx-background-color: " + backgroundColor + "; -fx-background-radius: 20; -fx-border-width: 2;");
                    }
                    entryContainer.getChildren().add(entryHBox);
                    entryNumber++;
                }
                if (entryNumber == 1) {
                    LOGGER.info("No entries found for contestId: " + contestId + ", week: " + weekStartTs + " to " + weekEndTs);
                }
            }
        } catch (SQLException e) {
            LOGGER.severe("Failed to load contest entries: " + e.getMessage());
            showErrorAlert("Database Error", "Failed to load contest entries: " + e.getMessage());
        }
    }

    private HBox createEntryHBox(int entryId, int entryNumber, String title, String author, java.sql.Timestamp submissionDate,
                                 int voteCount, String coverPhoto) {
        HBox hbox = new HBox();
        hbox.setAlignment(javafx.geometry.Pos.CENTER);
        hbox.setPrefHeight(95.0);
        hbox.setPrefWidth(1030.0);
        hbox.setMaxHeight(95.0);
        hbox.setMaxWidth(1030.0);
        hbox.setMinHeight(95.0);
        hbox.setMinWidth(1030.0);
        hbox.setSpacing(25.0);
        hbox.setStyle("-fx-background-color: #F4908A; -fx-background-radius: 10;");
        hbox.setId("entry_hbox");

        HBox numberBox = new HBox();
        numberBox.setAlignment(javafx.geometry.Pos.CENTER);
        numberBox.setPrefHeight(58.0);
        numberBox.setPrefWidth(58.0);
        numberBox.setMaxHeight(58.0);
        numberBox.setMaxWidth(58.0);
        numberBox.setMinHeight(58.0);
        numberBox.setMinWidth(58.0);
        numberBox.setStyle("-fx-background-color: #014237; -fx-background-radius: 20;");
        numberBox.setId("entry_no_hbox");
        Label numberLabel = new Label(String.valueOf(entryNumber));
        numberLabel.setTextFill(javafx.scene.paint.Color.WHITE);
        numberLabel.setFont(new Font("System Bold", 20.0));
        numberLabel.setId("entry_no");
        numberBox.getChildren().add(numberLabel);

        VBox titleAuthorBox = new VBox();
        titleAuthorBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        titleAuthorBox.setPrefHeight(95.0);
        titleAuthorBox.setPrefWidth(378.0);
        Label titleLabel = new Label(title);
        titleLabel.setFont(new Font("System Bold", 20.0));
        titleLabel.setId("title_of_the_content");
        Label authorLabel = new Label("by " + author);
        authorLabel.setFont(new Font("System Bold", 14.0));
        authorLabel.setId("author_name");
        titleAuthorBox.getChildren().addAll(titleLabel, authorLabel);

        HBox infoBox = new HBox();
        infoBox.setAlignment(javafx.geometry.Pos.CENTER);
        infoBox.setPrefHeight(95.0);
        infoBox.setPrefWidth(424.0);
        infoBox.setSpacing(15.0);

        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
        Label dateLabel = new Label("Uploaded on " + dateFormat.format(submissionDate));
        dateLabel.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
        dateLabel.setWrapText(true);
        dateLabel.setId("submited_date");

        HBox voteBox = new HBox();
        voteBox.setAlignment(javafx.geometry.Pos.CENTER);
        voteBox.setPrefHeight(95.0);
        voteBox.setPrefWidth(169.0);
        voteBox.setSpacing(10.0);

        StackPane voteButtonsPane = new StackPane(); // Changed to StackPane for stacking buttons
        voteButtonsPane.setAlignment(javafx.geometry.Pos.CENTER);
        voteButtonsPane.setPrefHeight(30.0);
        voteButtonsPane.setPrefWidth(60.0);
        voteButtonsPane.setMaxHeight(30.0);
        voteButtonsPane.setMaxWidth(60.0);

        Button notVotedButton = new Button();
        ImageView notVotedIcon = new ImageView(new Image(getClass().getResource("/images/icons/star5.png").toExternalForm()));
        notVotedIcon.setFitHeight(30.0);
        notVotedIcon.setFitWidth(30.0);
        notVotedButton.setGraphic(notVotedIcon);
        notVotedButton.setPrefHeight(30.0);
        notVotedButton.setPrefWidth(30.0);
        notVotedButton.setStyle("-fx-background-color: transparent;");
        notVotedButton.setId("not_voted_button");
        notVotedButton.setOnAction(this::handle_not_voted_button);
        notVotedButton.setDisable(!isCurrentWeekView);

        Button votedButton = new Button();
        ImageView votedIcon = new ImageView(new Image(getClass().getResource("/images/icons/star6.png").toExternalForm()));
        votedIcon.setFitHeight(30.0);
        votedIcon.setFitWidth(30.0);
        votedButton.setGraphic(votedIcon);
        votedButton.setPrefHeight(30.0);
        votedButton.setPrefWidth(30.0);
        votedButton.setStyle("-fx-background-color: transparent;");
        votedButton.setId("voted_button");
        votedButton.setOnAction(this::handle_voted_button);
        votedButton.setDisable(!isCurrentWeekView);

        boolean hasVoted = UserSession.getInstance().isLoggedIn() && hasUserVoted(entryId);
        notVotedButton.setVisible(!hasVoted);
        votedButton.setVisible(hasVoted);
        voteButtonsPane.getChildren().addAll(notVotedButton, votedButton); // Stacked in StackPane

        Label voteCountLabel = new Label("Received " + voteCount + " votes");
        voteCountLabel.setWrapText(true);
        voteCountLabel.setId("voted_by_no_of_people");

        voteBox.getChildren().addAll(voteButtonsPane, voteCountLabel);

        Button openButton = new Button("open");
        openButton.setPrefHeight(30.0);
        openButton.setPrefWidth(120.0);
        openButton.setStyle("-fx-background-color: #F5E0CD; -fx-background-radius: 5;");
        openButton.setTextFill(javafx.scene.paint.Color.valueOf("#014237"));
        openButton.setFont(new Font("System Bold", 14.0));
        openButton.setId("open_entry");
        openButton.setOnAction(this::handle_open_entry);

        infoBox.getChildren().addAll(dateLabel, voteBox, openButton);
        hbox.getChildren().addAll(numberBox, titleAuthorBox, infoBox);
        hbox.getProperties().put("entryId", entryId);
        return hbox;
    }

    private boolean isOwnEntry(int entryId) {
        try (Connection conn = db_connect.getConnection()) {
            if (conn == null) {
                LOGGER.severe("Database connection is null, cannot check entry ownership for entryId=" + entryId);
                return false;
            }
            String query = "SELECT user_id FROM contest_entries WHERE entry_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setInt(1, entryId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt("user_id") == UserSession.getInstance().getUserId();
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.severe("Failed to check entry ownership for entryId=" + entryId + ": " + e.getMessage());
            showErrorAlert("Database Error", "Unable to verify entry ownership: " + e.getMessage());
        }
        return false;
    }

    private boolean hasUserVoted(int entryId) {
        if (!UserSession.getInstance().isLoggedIn()) {
            return false;
        }
        try (Connection conn = db_connect.getConnection()) {
            if (conn == null) {
                LOGGER.severe("Database connection is null, cannot check vote status for entryId=" + entryId);
                return false;
            }
            String query = "SELECT vote_id FROM contest_votes WHERE contest_entry_id = ? AND user_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setInt(1, entryId);
                stmt.setInt(2, UserSession.getInstance().getUserId());
                try (ResultSet rs = stmt.executeQuery()) {
                    return rs.next();
                }
            }
        } catch (SQLException e) {
            LOGGER.severe("Failed to verify vote status for entryId=" + entryId + ": " + e.getMessage());
            showErrorAlert("Database Error", "Unable to verify vote status: " + e.getMessage());
            return false;
        }
    }

    private void addVote(int entryId, HBox entryHBox) {
        Connection conn = null;
        try {
            conn = db_connect.getConnection();
            if (conn == null) {
                throw new SQLException("Database connection is null");
            }
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
                int rowsAffected = stmt.executeUpdate();
                if (rowsAffected == 0) {
                    throw new SQLException("No rows updated for entry_id=" + entryId);
                }
            }

            conn.commit();
            updateVoteDisplay(entryHBox, true);
            loadEntries();
            LOGGER.info("Vote added for entryId=" + entryId + " by userId=" + UserSession.getInstance().getUserId());
        } catch (SQLException e) {
            LOGGER.severe("Failed to add vote for entryId=" + entryId + ": " + e.getMessage());
            if (conn != null) {
                try {
                    conn.rollback();
                    LOGGER.info("Transaction rolled back for addVote, entryId=" + entryId);
                } catch (SQLException rollbackEx) {
                    LOGGER.severe("Rollback failed: " + rollbackEx.getMessage());
                }
            }
            showErrorAlert("Database Error", "Failed to add vote: " + (e.getMessage().contains("Duplicate entry") ? "You have already voted." : e.getMessage()));
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    LOGGER.severe("Failed to close connection: " + e.getMessage());
                }
            }
        }
    }

    private void removeVote(int entryId, HBox entryHBox) {
        Connection conn = null;
        try {
            conn = db_connect.getConnection();
            if (conn == null) {
                throw new SQLException("Database connection is null");
            }
            conn.setAutoCommit(false);

            String deleteVote = "DELETE FROM contest_votes WHERE contest_entry_id = ? AND user_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(deleteVote)) {
                stmt.setInt(1, entryId);
                stmt.setInt(2, UserSession.getInstance().getUserId());
                int rowsAffected = stmt.executeUpdate();
                if (rowsAffected == 0) {
                    throw new SQLException("No vote found to delete for entry_id=" + entryId);
                }
            }

            String updateVotes = "UPDATE contest_entries SET vote_count = GREATEST(vote_count - 1, 0) WHERE entry_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(updateVotes)) {
                stmt.setInt(1, entryId);
                int rowsAffected = stmt.executeUpdate();
                if (rowsAffected == 0) {
                    throw new SQLException("No rows updated for entry_id=" + entryId);
                }
            }

            conn.commit();
            updateVoteDisplay(entryHBox, false);
            loadEntries();
            LOGGER.info("Vote removed for entryId=" + entryId + " by userId=" + UserSession.getInstance().getUserId());
        } catch (SQLException e) {
            LOGGER.severe("Failed to remove vote for entryId=" + entryId + ": " + e.getMessage());
            if (conn != null) {
                try {
                    conn.rollback();
                    LOGGER.info("Transaction rolled back for removeVote, entryId=" + entryId);
                } catch (SQLException rollbackEx) {
                    LOGGER.severe("Rollback failed: " + rollbackEx.getMessage());
                }
            }
            showErrorAlert("Database Error", "Failed to remove vote: " + e.getMessage());
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    LOGGER.severe("Failed to close connection: " + e.getMessage());
                }
            }
        }
    }

    private void updateVoteDisplay(HBox entryHBox, boolean hasVoted) {
        try {
            HBox infoBox = (HBox) entryHBox.getChildren().get(2);
            HBox voteBox = (HBox) infoBox.getChildren().get(1);
            StackPane voteButtonsPane = (StackPane) voteBox.getChildren().get(0); // Updated to StackPane
            Button notVotedButton = (Button) voteButtonsPane.getChildren().get(0);
            Button votedButton = (Button) voteButtonsPane.getChildren().get(1);
            notVotedButton.setVisible(!hasVoted);
            votedButton.setVisible(hasVoted);

            Label voteCountLabel = (Label) voteBox.getChildren().get(1);
            int entryId = getEntryIdFromHBox(entryHBox);
            int currentVoteCount = getCurrentVoteCount(entryId);
            voteCountLabel.setText("Received " + currentVoteCount + " votes");
        } catch (Exception e) {
            LOGGER.severe("Failed to update vote display for entryId=" + getEntryIdFromHBox(entryHBox) + ": " + e.getMessage());
            showErrorAlert("UI Error", "Failed to update vote display: " + e.getMessage());
        }
    }

    private int getCurrentVoteCount(int entryId) {
        try (Connection conn = db_connect.getConnection()) {
            if (conn == null) {
                LOGGER.severe("Database connection is null, cannot fetch vote count for entryId=" + entryId);
                return 0;
            }
            String query = "SELECT vote_count FROM contest_entries WHERE entry_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setInt(1, entryId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt("vote_count");
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.severe("Failed to fetch vote count for entryId=" + entryId + ": " + e.getMessage());
            return 0;
        }
        return 0;
    }

    private int getEntryIdFromHBox(HBox hbox) {
        return (int) hbox.getProperties().get("entryId");
    }

    private HBox findEntryHBox(Node node) {
        while (node != null) {
            if (node instanceof HBox && "entry_hbox".equals(node.getId())) {
                return (HBox) node;
            }
            node = node.getParent();
        }
        return null;
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

    private LocalDateTime getCurrentWeekStart() {
        LocalDateTime now = LocalDateTime.now(ZoneOffset.ofHours(6));
        LocalDateTime saturday = now.with(DayOfWeek.SATURDAY).withHour(0).withMinute(0).withSecond(0).withNano(0);
        if (now.isAfter(saturday)) {
            return saturday;
        }
        return saturday.minusWeeks(1);
    }

    private LocalDateTime getPreviousWeekStart() {
        return getCurrentWeekStart().minusWeeks(1);
    }

    private void updateWeeklySessionLabel(boolean isCurrentWeek) {
        LocalDateTime weekStart = isCurrentWeek ? getCurrentWeekStart() : getPreviousWeekStart();
        LocalDateTime weekEnd = weekStart.plusDays(6).withHour(23).withMinute(59).withSecond(59);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy");
        if (weekly_session != null) {
            weekly_session.setText(weekStart.format(formatter) + " - " + weekEnd.format(formatter));
        }
    }

    private void startCountdownTimer() {
        AtomicReference<LocalDateTime> nextSaturdayRef = new AtomicReference<>(getCurrentWeekStart().plusWeeks(1));
        Timer timer = new Timer(true);
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                javafx.application.Platform.runLater(() -> {
                    LocalDateTime now = LocalDateTime.now(ZoneOffset.ofHours(6));
                    LocalDateTime nextSaturday = nextSaturdayRef.get();
                    Duration duration = Duration.between(now, nextSaturday);
                    if (duration.isNegative() || duration.isZero()) {
                        updateWeeklySessionLabel(true);
                        nextSaturdayRef.set(nextSaturday.plusWeeks(1));
                    }
                    long days = duration.toDays();
                    long hours = duration.toHoursPart();
                    long minutes = duration.toMinutesPart();
                    long seconds = duration.toSecondsPart();
                    if (countdown != null) {
                        countdown.setText(String.format("Next reset in: %dd: %02dh: %02dm: %02ds", days, hours, minutes, seconds));
                    }
                });
            }
        }, 0, 1000);
    }

    public void handle_previous_week_button(ActionEvent actionEvent) {
        isCurrentWeekView = false;
        updateWeeklySessionLabel(false);
        updateButtonStyles();
        loadEntries();
    }

    public void handle_current_week_button(ActionEvent actionEvent) {
        isCurrentWeekView = true;
        updateWeeklySessionLabel(true);
        updateButtonStyles();
        loadEntries();
    }
}