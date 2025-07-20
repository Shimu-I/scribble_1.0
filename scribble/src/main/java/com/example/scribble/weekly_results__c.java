package com.example.scribble;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;

public class weekly_results__c {

    @FXML
    private Button go_left;

    @FXML
    private Button go_right;

    @FXML
    private VBox first_card;

    @FXML
    private Label first_position;

    @FXML
    private ImageView first_book_cover;

    @FXML
    private Label first_book_title;

    @FXML
    private Label first_book_author;

    @FXML
    private Label first_total_votes;

    @FXML
    private Button first_open;

    @FXML
    private VBox second_card;

    @FXML
    private Label second_position;

    @FXML
    private ImageView second_book_cover;

    @FXML
    private Label second_book_title;

    @FXML
    private Label second_book_author;

    @FXML
    private Label second_total_votes;

    @FXML
    private Button second_open;

    @FXML
    private VBox third_card;

    @FXML
    private Label third_position;

    @FXML
    private ImageView third_book_cover;

    @FXML
    private Label third_book_title;

    @FXML
    private Label third_book_author;

    @FXML
    private Label third_total_votes;

    @FXML
    private Button third_open;

    private List<String> fullResults = new ArrayList<>();
    private int currentPage = 0;
    private static final int ENTRIES_PER_PAGE = 3;
    private String firstEntryId;
    private String secondEntryId;
    private String thirdEntryId;

    @FXML
    private void initialize() {
        go_left.setOnAction(event -> handleGoLeft());
        go_right.setOnAction(event -> handleGoRight());
        first_open.setOnAction(event -> handleOpenFirst());
        second_open.setOnAction(event -> handleOpenSecond());
        third_open.setOnAction(event -> handleOpenThird());
    }

    public void setResults(List<String> results) {
        this.fullResults = results != null ? new ArrayList<>(results) : new ArrayList<>();
        this.currentPage = 0;
        updateResultsDisplay();
    }

    private void updateResultsDisplay() {
        firstEntryId = null;
        secondEntryId = null;
        thirdEntryId = null;

        int startIndex = currentPage * ENTRIES_PER_PAGE;
        int endIndex = Math.min(startIndex + ENTRIES_PER_PAGE, fullResults.size());

        // Handle empty results
        if (fullResults.isEmpty()) {
            first_card.setVisible(true);
            first_position.setText("1st");
            first_book_title.setText("This week has no entries");
            first_book_author.setText("");
            first_total_votes.setText("");
            first_book_cover.setImage(new Image("file://path/to/default_cover.png"));
            first_open.setDisable(true);

            second_card.setVisible(true);
            second_position.setText("2nd");
            second_book_title.setText("This week has no entries");
            second_book_author.setText("");
            second_total_votes.setText("");
            second_book_cover.setImage(new Image("file://path/to/default_cover.png"));
            second_open.setDisable(true);

            third_card.setVisible(true);
            third_position.setText("3rd");
            third_book_title.setText("This week has no entries");
            third_book_author.setText("");
            third_total_votes.setText("");
            third_book_cover.setImage(new Image("file://path/to/default_cover.png"));
            third_open.setDisable(true);

            go_left.setDisable(true);
            go_right.setDisable(true);
            return;
        }

        // Update first card
        if (startIndex < endIndex) {
            String[] data = fullResults.get(startIndex).split("\\|");
            if (data.length >= 5) {
                first_card.setVisible(true);
                first_position.setText("1st");
                first_book_title.setText(data[1]);
                first_book_author.setText("by " + data[2]);
                first_total_votes.setText(data[3] + " votes");
                firstEntryId = data[0];
                first_open.setDisable(false);
                try {
                    first_book_cover.setImage(new Image(data[4]));
                } catch (Exception e) {
                    first_book_cover.setImage(new Image("file://path/to/default_cover.png"));
                }
            } else {
                first_card.setVisible(false);
            }
        } else {
            first_card.setVisible(true);
            first_position.setText("1st");
            first_book_title.setText("This week has no entries");
            first_book_author.setText("");
            first_total_votes.setText("");
            first_book_cover.setImage(new Image("file://path/to/default_cover.png"));
            first_open.setDisable(true);
        }

        // Update second card
        if (startIndex + 1 < endIndex) {
            String[] data = fullResults.get(startIndex + 1).split("\\|");
            if (data.length >= 5) {
                second_card.setVisible(true);
                second_position.setText("2nd");
                second_book_title.setText(data[1]);
                second_book_author.setText("by " + data[2]);
                second_total_votes.setText(data[3] + " votes");
                secondEntryId = data[0];
                second_open.setDisable(false);
                try {
                    second_book_cover.setImage(new Image(data[4]));
                } catch (Exception e) {
                    second_book_cover.setImage(new Image("file://path/to/default_cover.png"));
                }
            } else {
                second_card.setVisible(false);
            }
        } else {
            second_card.setVisible(true);
            second_position.setText("2nd");
            second_book_title.setText("This week has no entries");
            second_book_author.setText("");
            second_total_votes.setText("");
            second_book_cover.setImage(new Image("file://path/to/default_cover.png"));
            second_open.setDisable(true);
        }

        // Update third card
        if (startIndex + 2 < endIndex) {
            String[] data = fullResults.get(startIndex + 2).split("\\|");
            if (data.length >= 5) {
                third_card.setVisible(true);
                third_position.setText("3rd");
                third_book_title.setText(data[1]);
                third_book_author.setText("by " + data[2]);
                third_total_votes.setText(data[3] + " votes");
                thirdEntryId = data[0];
                third_open.setDisable(false);
                try {
                    third_book_cover.setImage(new Image(data[4]));
                } catch (Exception e) {
                    third_book_cover.setImage(new Image("file://path/to/default_cover.png"));
                }
            } else {
                third_card.setVisible(false);
            }
        } else {
            third_card.setVisible(true);
            third_position.setText("3rd");
            third_book_title.setText("This week has no entries");
            third_book_author.setText("");
            third_total_votes.setText("");
            third_book_cover.setImage(new Image("file://path/to/default_cover.png"));
            third_open.setDisable(true);
        }

        go_left.setDisable(currentPage == 0);
        go_right.setDisable(endIndex >= fullResults.size());
    }

    private void handleGoLeft() {
        if (currentPage > 0) {
            currentPage--;
            updateResultsDisplay();
            System.out.println("Navigating to previous results (Page " + (currentPage + 1) + ")");
        }
    }

    private void handleGoRight() {
        if ((currentPage + 1) * ENTRIES_PER_PAGE < fullResults.size()) {
            currentPage++;
            updateResultsDisplay();
            System.out.println("Navigating to next results (Page " + (currentPage + 1) + ")");
        }
    }

    private void handleOpenFirst() {
        if (firstEntryId != null) {
            System.out.println("Opening contest entry: " + first_book_title.getText() + " (ID: " + firstEntryId + ")");
            // Add logic to open details page for entry_id
        }
    }

    private void handleOpenSecond() {
        if (secondEntryId != null) {
            System.out.println("Opening contest entry: " + second_book_title.getText() + " (ID: " + secondEntryId + ")");
            // Add logic to open details page for entry_id
        }
    }

    private void handleOpenThird() {
        if (thirdEntryId != null) {
            System.out.println("Opening contest entry: " + third_book_title.getText() + " (ID: " + thirdEntryId + ")");
            // Add logic to open details page for entry_id
        }
    }
}