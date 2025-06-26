package com.example.scribble;

import com.example.scribble.PuzzlePiece;
import javafx.animation.AnimationTimer;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class PuzzleController {
    @FXML
    private Canvas canvas;
    @FXML
    private ComboBox<String> difficultyCombo;
    @FXML
    private Label timerLabel;

    private List<PuzzlePiece> pieces;
    private PuzzlePiece selectedPiece;
    private double offsetX, offsetY;
    private int gridSize;
    private long startTime;
    private AnimationTimer timer;
    private boolean isGameActive;
    private Image image;
    private static final String[] IMAGE_FILES = {"1.png", "2.png", "3.png", "4.png", "5.png"};

    @FXML
    public void initialize() {
        if (canvas == null || difficultyCombo == null || timerLabel == null) {
            System.err.println("FXML components not properly initialized.");
            return;
        }
        difficultyCombo.getItems().addAll("Easy (3x3)", "Medium (4x4)", "Hard (6x6)");
        difficultyCombo.setValue("Easy (3x3)");
        difficultyCombo.setOnAction(e -> startNewGame());
        canvas.setOnMousePressed(this::handleMousePressed);
        canvas.setOnMouseDragged(this::handleMouseDragged);
        canvas.setOnMouseReleased(this::handleMouseReleased);
        startNewGame();
    }

    private void startNewGame() {
        try {
            gridSize = switch (difficultyCombo.getValue()) {
                case "Medium (4x4)" -> 4;
                case "Hard (6x6)" -> 6;
                default -> 3;
            };
            pieces = new ArrayList<>();
            String randomImage = IMAGE_FILES[new Random().nextInt(IMAGE_FILES.length)];
            image = new Image(getClass().getResource("/game_photos/" + randomImage).toExternalForm());
            createPuzzlePieces();
            randomizePieces();
            startTime = System.nanoTime();
            isGameActive = true;

            if (timer != null) timer.stop();
            timer = new AnimationTimer() {
                @Override
                public void handle(long now) {
                    updateTimer();
                    drawPuzzle();
                }
            };
            timer.start();
        } catch (Exception e) {
            System.err.println("Error loading image or initializing game: " + e.getMessage());
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("Failed to start the game. Check image resources.");
            alert.showAndWait();
        }
    }

    private void createPuzzlePieces() {
        double pieceWidth = 600.0 / gridSize;
        double pieceHeight = 347.0 / gridSize;
        pieces.clear();
        for (int row = 0; row < gridSize; row++) {
            for (int col = 0; col < gridSize; col++) {
                Rectangle shape = new Rectangle(0, 0, pieceWidth, pieceHeight);
                PuzzlePiece piece = new PuzzlePiece(
                        (col * pieceWidth) + 5.0, (row * pieceHeight) + 5.0,
                        pieceWidth, pieceHeight,
                        image, shape, col, row
                );
                double maxX = canvas.getWidth() - pieceWidth;
                double maxY = canvas.getHeight() - pieceHeight;
                piece.setCurrentX(600.0 + new Random().nextDouble() * (maxX - 600.0));
                piece.setCurrentY(new Random().nextDouble() * maxY);
                pieces.add(piece);
            }
        }
    }

    private void randomizePieces() {
        Collections.shuffle(pieces);
        for (PuzzlePiece piece : pieces) {
            piece.setSnapped(false);
        }
    }

    private void handleMousePressed(MouseEvent event) {
        if (!isGameActive) return;
        selectedPiece = null;
        double mouseX = event.getX();
        double mouseY = event.getY();
        for (int i = pieces.size() - 1; i >= 0; i--) {
            PuzzlePiece piece = pieces.get(i);
            if (!piece.isSnapped() && piece.contains(mouseX, mouseY)) {
                selectedPiece = piece;
                offsetX = mouseX - piece.getCurrentX();
                offsetY = mouseY - piece.getCurrentY();
                pieces.remove(piece);
                pieces.add(piece);
                break;
            }
        }
    }

    private void handleMouseDragged(MouseEvent event) {
        if (selectedPiece != null && isGameActive) {
            double newX = Math.max(0, Math.min(event.getX() - offsetX, canvas.getWidth() - selectedPiece.getWidth()));
            double newY = Math.max(0, Math.min(event.getY() - offsetY, canvas.getHeight() - selectedPiece.getHeight()));
            selectedPiece.setCurrentX(newX);
            selectedPiece.setCurrentY(newY);
        }
    }

    private void handleMouseReleased(MouseEvent event) {
        if (selectedPiece != null && isGameActive) {
            snapPiece(selectedPiece);
            selectedPiece = null;
            checkCompletion();
        }
    }

    private void snapPiece(PuzzlePiece piece) {
        double targetX = piece.getTargetX();
        double targetY = piece.getTargetY();
        double currentX = piece.getCurrentX();
        double currentY = piece.getCurrentY();
        double snapThreshold = 20;

        if (Math.abs(currentX - targetX) < snapThreshold && Math.abs(currentY - targetY) < snapThreshold) {
            piece.setCurrentX(targetX);
            piece.setCurrentY(targetY);
            piece.setSnapped(true);
        }
    }

    private void checkCompletion() {
        boolean complete = pieces.stream().allMatch(PuzzlePiece::isSnapped);
        if (complete) {
            isGameActive = false;
            timer.stop();
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setContentText("Congratulations! Puzzle completed!");
            alert.showAndWait();
        }
    }

    private void updateTimer() {
        if (!isGameActive) return;
        long elapsed = (System.nanoTime() - startTime) / 1_000_000_000;
        timerLabel.setText(String.format("Time: %d:%02d", elapsed / 60, elapsed % 60));
    }

    private void drawPuzzle() {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setFill(Color.LIGHTGRAY);
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(2); // Ensure line width is set for the rectangle
        gc.strokeRect(5, 5, 600, 347); // Adjusted to match puzzle area

        for (PuzzlePiece piece : pieces) {
            piece.draw(gc);
        }
    }
}