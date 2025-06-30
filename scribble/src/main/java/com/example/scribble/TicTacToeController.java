package com.example.scribble;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.animation.PauseTransition;
import javafx.util.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TicTacToeController {
    @FXML
    private Label statusLabel;
    @FXML
    private RadioButton radioX, radioO;
    @FXML
    private Button btn00, btn01, btn02, btn10, btn11, btn12, btn20, btn21, btn22;

    private Button[][] board = new Button[3][3];
    private boolean isPlayerX;
    private boolean gameStarted;
    private int moveCount;
    private Random random = new Random();
    private boolean isPlayerTurn;

    @FXML
    public void initialize() {
        board[0][0] = btn00;
        board[0][1] = btn01;
        board[0][2] = btn02;
        board[1][0] = btn10;
        board[1][1] = btn11;
        board[1][2] = btn12;
        board[2][0] = btn20;
        board[2][1] = btn21;
        board[2][2] = btn22;

        radioX.setToggleGroup(new javafx.scene.control.ToggleGroup());
        radioO.setToggleGroup(radioX.getToggleGroup());
        radioX.setSelected(true);

        disableBoard(true);
        isPlayerTurn = true;
    }

    @FXML
    private void startGame() {
        if (!gameStarted) {
            isPlayerX = radioX.isSelected();
            gameStarted = true;
            moveCount = 0;
            isPlayerTurn = isPlayerX;
            statusLabel.setText(isPlayerX ? "Your turn (X)" : "Computer's turn (O)");
            disableBoard(false);
            radioX.setDisable(true);
            radioO.setDisable(true);

            if (!isPlayerX) {
                isPlayerTurn = false;
                makeComputerMove();
            }
        }
    }

    @FXML
    private void handleButtonClick(javafx.event.ActionEvent event) {
        if (!gameStarted || isGameOver() || !isPlayerTurn) return;

        Button clickedButton = (Button) event.getSource();
        String playerMark = isPlayerX ? "X" : "O";

        if (clickedButton.getText().isEmpty()) {
            clickedButton.setText(playerMark);
            moveCount++;
            scheduleMarkDeletion(clickedButton, playerMark, true);

            disableBoard(true);
            isPlayerTurn = false;

            if (checkWin()) {
                statusLabel.setText("You Win!");
                disableBoard(true);
            } else if (moveCount == 9) {
                statusLabel.setText("It's a Draw!");
                disableBoard(true);
            } else {
                statusLabel.setText("Computer's turn");
                makeComputerMove();
            }
        }
    }

    private void makeComputerMove() {
        if (isGameOver()) return;

        PauseTransition delay = new PauseTransition(Duration.seconds(3));
        delay.setOnFinished(e -> {
            String computerMark = isPlayerX ? "O" : "X";
            String playerMark = isPlayerX ? "X" : "O";
            Button computerButton = null;

            // Step 1: Check for winning move
            computerButton = findWinningMove(computerMark);
            if (computerButton == null) {
                // Step 2: Block player's winning move
                computerButton = findWinningMove(playerMark);
            }
            if (computerButton == null) {
                // Step 3: Try to take center
                if (board[1][1].getText().isEmpty()) {
                    computerButton = board[1][1];
                }
            }
            if (computerButton == null) {
                // Step 4: Try to take a corner
                List<Button> corners = new ArrayList<>();
                if (board[0][0].getText().isEmpty()) corners.add(board[0][0]);
                if (board[0][2].getText().isEmpty()) corners.add(board[0][2]);
                if (board[2][0].getText().isEmpty()) corners.add(board[2][0]);
                if (board[2][2].getText().isEmpty()) corners.add(board[2][2]);
                if (!corners.isEmpty()) {
                    computerButton = corners.get(random.nextInt(corners.size()));
                }
            }
            if (computerButton == null) {
                // Step 5: Take any remaining edge
                List<Button> edges = new ArrayList<>();
                if (board[0][1].getText().isEmpty()) edges.add(board[0][1]);
                if (board[1][0].getText().isEmpty()) edges.add(board[1][0]);
                if (board[1][2].getText().isEmpty()) edges.add(board[1][2]);
                if (board[2][1].getText().isEmpty()) edges.add(board[2][1]);
                if (!edges.isEmpty()) {
                    computerButton = edges.get(random.nextInt(edges.size()));
                }
            }

            if (computerButton != null) {
                computerButton.setText(computerMark);
                moveCount++;
                scheduleMarkDeletion(computerButton, computerMark, false);

                if (checkWin()) {
                    statusLabel.setText("Computer Wins!");
                    disableBoard(true);
                } else if (moveCount == 9) {
                    statusLabel.setText("It's a Draw!");
                    disableBoard(true);
                } else {
                    statusLabel.setText(isPlayerX ? "Your turn (X)" : "Your turn (O)");
                    disableBoard(false);
                    isPlayerTurn = true;
                }
            }
        });
        delay.play();
    }

    private Button findWinningMove(String mark) {
        // Check rows
        for (int i = 0; i < 3; i++) {
            if (board[i][0].getText().equals(mark) && board[i][1].getText().equals(mark) && board[i][2].getText().isEmpty())
                return board[i][2];
            if (board[i][1].getText().equals(mark) && board[i][2].getText().equals(mark) && board[i][0].getText().isEmpty())
                return board[i][0];
            if (board[i][0].getText().equals(mark) && board[i][2].getText().equals(mark) && board[i][1].getText().isEmpty())
                return board[i][1];
        }
        // Check columns
        for (int i = 0; i < 3; i++) {
            if (board[0][i].getText().equals(mark) && board[1][i].getText().equals(mark) && board[2][i].getText().isEmpty())
                return board[2][i];
            if (board[1][i].getText().equals(mark) && board[2][i].getText().equals(mark) && board[0][i].getText().isEmpty())
                return board[0][i];
            if (board[0][i].getText().equals(mark) && board[2][i].getText().equals(mark) && board[1][i].getText().isEmpty())
                return board[1][i];
        }
        // Check diagonals
        if (board[0][0].getText().equals(mark) && board[1][1].getText().equals(mark) && board[2][2].getText().isEmpty())
            return board[2][2];
        if (board[1][1].getText().equals(mark) && board[2][2].getText().equals(mark) && board[0][0].getText().isEmpty())
            return board[0][0];
        if (board[0][0].getText().equals(mark) && board[2][2].getText().equals(mark) && board[1][1].getText().isEmpty())
            return board[1][1];
        if (board[0][2].getText().equals(mark) && board[1][1].getText().equals(mark) && board[2][0].getText().isEmpty())
            return board[2][0];
        if (board[1][1].getText().equals(mark) && board[2][0].getText().equals(mark) && board[0][2].getText().isEmpty())
            return board[0][2];
        if (board[0][2].getText().equals(mark) && board[2][0].getText().equals(mark) && board[1][1].getText().isEmpty())
            return board[1][1];
        return null;
    }

    private void scheduleMarkDeletion(Button button, String mark, boolean isPlayer) {
        double duration = isPlayer ? 20.0 : 20.0;
        PauseTransition pause = new PauseTransition(Duration.seconds(duration));
        pause.setOnFinished(e -> {
            if (button.getText().equals(mark)) {
                button.setText("");
                moveCount--;
            }
        });
        pause.play();
    }

    private boolean checkWin() {
        for (int i = 0; i < 3; i++) {
            if (!board[i][0].getText().isEmpty() &&
                    board[i][0].getText().equals(board[i][1].getText()) &&
                    board[i][0].getText().equals(board[i][2].getText())) {
                return true;
            }
            if (!board[0][i].getText().isEmpty() &&
                    board[0][i].getText().equals(board[1][i].getText()) &&
                    board[0][i].getText().equals(board[2][i].getText())) {
                return true;
            }
        }
        if (!board[0][0].getText().isEmpty() &&
                board[0][0].getText().equals(board[1][1].getText()) &&
                board[0][0].getText().equals(board[2][2].getText())) {
            return true;
        }
        if (!board[0][2].getText().isEmpty() &&
                board[0][2].getText().equals(board[1][1].getText()) &&
                board[0][2].getText().equals(board[2][0].getText())) {
            return true;
        }
        return false;
    }

    private boolean isGameOver() {
        return checkWin() || moveCount == 9;
    }

    private void disableBoard(boolean disable) {
        for (Button[] row : board) {
            for (Button btn : row) {
                btn.setDisable(disable);
            }
        }
    }

    @FXML
    private void resetGame() {
        for (Button[] row : board) {
            for (Button btn : row) {
                btn.setText("");
                btn.setDisable(true);
            }
        }
        radioX.setDisable(false);
        radioO.setDisable(false);
        gameStarted = false;
        moveCount = 0;
        isPlayerTurn = true;
        statusLabel.setText("Choose your side");
    }
}