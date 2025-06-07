package com.example.scribble;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import java.io.IOException;
import javafx.animation.*;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.util.Duration;

import java.util.List;

public class home__con {

    @FXML
    private Button read_more_button;

    @FXML
    private Label label_text;

    @FXML
    private nav_bar__c mainController;

    @FXML
    void handle_read_more(ActionEvent event) {
        System.out.println("Opening the about us!");
        if (mainController != null) {
            mainController.loadFXML("about_us.fxml");
        } else {
            System.out.println("mainController is null in home__con, attempting to reload nav_bar.fxml");
            try {
                // Load nav_bar.fxml to restore the navigation bar
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/scribble/nav_bar.fxml"));
                Parent root = loader.load();
                Stage stage = (Stage) read_more_button.getScene().getWindow();
                Scene scene = new Scene(root);
                stage.setScene(scene);
                stage.show();

                // After loading nav_bar.fxml, load about_us.fxml
                nav_bar__c navController = loader.getController();
                navController.loadFXML("about_us.fxml");
            } catch (IOException e) {
                System.err.println("Error loading nav_bar.fxml: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }




    private final List<String> messages = List.of(
            "Love your faves? They're all here—dive into the contest and vibe with the community!",
            "Ready to turn up the fun? Join the contest, explore what you love, and meet awesome people!",
            "Don’t just scroll—get involved! Your favorites, cool prizes, and a community that gets you!"
    );

    private int messageIndex = 0;
    private Timeline blinkTimeline;

    @FXML
    public void initialize() {
        playTypingEffect();
    }

    private void playTypingEffect() {
        if (blinkTimeline != null) {
            blinkTimeline.stop();
        }

        String currentMessage = messages.get(messageIndex);
        StringBuilder displayedText = new StringBuilder();

        Timeline typingTimeline = new Timeline();

        for (int i = 0; i < currentMessage.length(); i++) {
            final int index = i;
            KeyFrame keyFrame = new KeyFrame(Duration.millis(40 * i), e -> {
                displayedText.append(currentMessage.charAt(index));
                label_text.setText(displayedText.toString() + " ⬤");
            });
            typingTimeline.getKeyFrames().add(keyFrame);
        }

        typingTimeline.setOnFinished(e -> {
            startBlinkingDot(displayedText.toString());

            PauseTransition pause = new PauseTransition(Duration.seconds(3));
            pause.setOnFinished(ev -> {
                messageIndex = (messageIndex + 1) % messages.size();
                playTypingEffect();
            });
            pause.play();
        });

        typingTimeline.play();
    }

    private void startBlinkingDot(String baseText) {
        blinkTimeline = new Timeline(
                new KeyFrame(Duration.seconds(0), e -> label_text.setText(baseText + " ⬤")),
                new KeyFrame(Duration.seconds(0.5), e -> label_text.setText(baseText + " "))
        );
        blinkTimeline.setCycleCount(Animation.INDEFINITE);
        blinkTimeline.play();
    }

    public void setMainController(nav_bar__c mainController) {
        this.mainController = mainController;
        System.out.println("setMainController called in home__con with mainController: " + (mainController != null ? "not null" : "null"));
    }


}