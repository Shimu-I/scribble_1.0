package com.example.scribble;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.transform.Scale;
import javafx.stage.Stage;
import java.io.IOException;

public class games__c {

    private boolean stylesApplied = false; // Flag to prevent multiple styling

    @FXML
    public void initialize() {
        // Rely on event handlers for styling due to lack of node injection
    }

    private void applyStylesToButtons(Node startingNode) {
        if (stylesApplied) return;

        // Find the center HBox
        Node current = startingNode;
        VBox parentVBox = null;
        while (current != null) {
            if (current instanceof VBox) {
                parentVBox = (VBox) current;
                break;
            }
            current = current.getParent();
        }

        if (parentVBox == null) {
            System.err.println("Could not find parent VBox");
            return;
        }

        current = parentVBox.getParent(); // Should be HBox
        if (!(current instanceof HBox)) {
            System.err.println("Parent is not HBox");
            return;
        }

        HBox centerHBox = (HBox) current;
        for (Node node : centerHBox.getChildren()) {
            if (node instanceof HBox) { // Check nested HBox
                HBox innerHBox = (HBox) node;
                for (Node innerNode : innerHBox.getChildren()) {
                    if (innerNode instanceof VBox) {
                        VBox vbox = (VBox) innerNode;
                        Button button = null;
                        ImageView imageView = null;
                        for (Node child : vbox.getChildren()) {
                            if (child instanceof Button) {
                                button = (Button) child;
                            } else if (child instanceof ImageView) {
                                imageView = (ImageView) child;
                            }
                        }
                        if (button != null) {
                            applyButtonStyles(button, imageView);
                        }
                    }
                }
            }
        }
        stylesApplied = true;
    }

    private void applyButtonStyles(Button button, ImageView imageView) {
        // Apply default drop shadow effect (white)
        DropShadow defaultShadow = new DropShadow();
        defaultShadow.setRadius(10.0);
        defaultShadow.setOffsetX(2.0);
        defaultShadow.setOffsetY(2.0);
        defaultShadow.setColor(Color.color(1, 1, 1, 0.3)); // White shadow
        button.setEffect(defaultShadow);

        // Create hover drop shadow effect (white, larger)
        DropShadow hoverShadow = new DropShadow();
        hoverShadow.setRadius(15.0); // Larger radius for hover
        hoverShadow.setOffsetX(3.0);
        hoverShadow.setOffsetY(3.0);
        hoverShadow.setColor(Color.color(1, 1, 1, 0.5)); // White, more opaque

        // Store original background color
        String originalStyle = button.getStyle() != null ? button.getStyle() : "";
        String baseColor = "#F4908A";
        String hoverColor = "#FF9999";

        // Create scale transform for ImageView
        Scale scale = new Scale(1.0, 1.0);
        if (imageView != null) {
            imageView.getTransforms().add(scale);
        }

        // Apply hover effects
        button.setOnMouseEntered(event -> {
            button.setStyle(originalStyle + "-fx-background-color: " + hoverColor + ";");
            button.setEffect(hoverShadow);
            if (imageView != null) {
                scale.setX(1.1); // 10% larger
                scale.setY(1.1);
            }
        });

        button.setOnMouseExited(event -> {
            button.setStyle(originalStyle);
            button.setEffect(defaultShadow);
            if (imageView != null) {
                scale.setX(1.0);
                scale.setY(1.0);
            }
        });

        // Set cursor to hand on hover
        button.setCursor(javafx.scene.Cursor.HAND);
    }

    private void openNewWindow(String fxmlPath, String title, double width, double height) {
        try {
            // Load the FXML file
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            if (loader.getLocation() == null) {
                throw new IOException("FXML file not found: " + fxmlPath);
            }
            // Load the root node
            Parent root = loader.load();
            // Create scene with the specified size
            Scene scene = new Scene(root, width, height);
            Stage stage = new Stage();
            stage.setTitle(title);
            stage.setScene(scene);
            stage.setResizable(false); // Prevent window resizing
            stage.show();
        } catch (IOException e) {
            System.err.println("Error loading FXML: " + fxmlPath);
            e.printStackTrace();
        }
    }

    @FXML
    private void handle_puzzle(javafx.event.ActionEvent event) {
        if (!stylesApplied) {
            applyStylesToButtons((Button) event.getSource());
        }
        openNewWindow("/com/example/scribble/puzzle.fxml", "Puzzle", 1000, 500);
    }

    @FXML
    private void handle_tic_tac_toe(javafx.event.ActionEvent event) {
        if (!stylesApplied) {
            applyStylesToButtons((Button) event.getSource());
        }
        openNewWindow("/com/example/scribble/TicTacToe.fxml", "Tic Tac Toe", 444, 542);
    }
}