package com.example.scribble;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.ArrayList;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.Cursor;

public class profile_info_edit__c {

    @FXML
    private Button end;
    @FXML
    private Button profile_pic;
    @FXML
    private Button save_button;
    @FXML
    private TextField edit_name;
    @FXML
    private TextField edit_email;
    @FXML
    private TextField old_password;
    @FXML
    private TextField new_password;
    @FXML
    private ImageView profileImageView;

    private profile__c parentController;
    private File selectedImageFile;
    private int userId;

    public void setParentController(profile__c parentController) {
        this.parentController = parentController;
        System.out.println("setParentController called in profile_info_edit__c with parentController: " + (parentController != null ? "set" : "null"));
    }

    public void setUserId(int userId) {
        this.userId = userId;
        System.out.println("setUserId called in profile_info_edit__c with userId: " + userId);
        loadUserData();
    }

    @FXML
    public void initialize() {
        System.out.println("profile_info_edit__c initialized");
    }

    private void loadUserData() {
        if (userId == 0) {
            showAlert(Alert.AlertType.ERROR, "Error", "No user ID set");
            return;
        }
        try (Connection conn = db_connect.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT username, email, profile_picture FROM users WHERE user_id = ?")) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                edit_name.setText(rs.getString("username"));
                edit_email.setText(rs.getString("email"));
                String profilePic = rs.getString("profile_picture");
                if (profilePic != null && !profilePic.isEmpty()) {
                    profileImageView.setImage(new Image("file:src/main/resources/images/profiles/" + profilePic));
                }
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "User not found");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load user data: " + e.getMessage());
        }
    }

    @FXML
    private void handle_end(ActionEvent event) {
        System.out.println("End button clicked, closing edit overlay");
        if (parentController != null) {
            parentController.closeEditOverlay();
        } else {
            showAlert(Alert.AlertType.ERROR, "Error", "Cannot close edit overlay: parentController is null");
        }
    }

    @FXML
    private void handle_profile_pic(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose Profile Picture");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );

        File file = fileChooser.showOpenDialog(profile_pic.getScene().getWindow());
        if (file != null) {
            try {
                Image image = new Image(file.toURI().toString());
                profileImageView.setImage(image);
                selectedImageFile = file;
                System.out.println("Selected profile picture: " + file.getAbsolutePath());
            } catch (Exception e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to load profile picture: " + e.getMessage());
            }
        } else {
            System.out.println("No file selected.");
        }
    }

    @FXML
    private void handle_save_button(ActionEvent event) {
        System.out.println("Save button clicked");
        String name = edit_name.getText().trim();
        String email = edit_email.getText().trim();
        String oldPassword = old_password.getText().trim();
        String newPassword = new_password.getText().trim();

        if (name.isEmpty() && email.isEmpty() && newPassword.isEmpty() && selectedImageFile == null) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "At least one field must be provided to update");
            return;
        }

        if (!email.isEmpty() && !email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Invalid email format");
            return;
        }

        if (!newPassword.isEmpty()) {
            if (oldPassword.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Validation Error", "Old password is required to change password");
                return;
            }
            if (newPassword.trim().isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Validation Error", "New password cannot be empty");
                return;
            }
        }

        try (Connection conn = db_connect.getConnection()) {
            if (!name.isEmpty() || !email.isEmpty()) {
                PreparedStatement checkStmt = conn.prepareStatement(
                        "SELECT user_id FROM users WHERE (username = ? OR email = ?) AND user_id != ?");
                checkStmt.setString(1, name.isEmpty() ? null : name);
                checkStmt.setString(2, email.isEmpty() ? null : email);
                checkStmt.setInt(3, userId);
                ResultSet rs = checkStmt.executeQuery();
                if (rs.next()) {
                    showAlert(Alert.AlertType.WARNING, "Validation Error", "Username or email already in use");
                    return;
                }
            }

            if (!oldPassword.isEmpty()) {
                PreparedStatement pwdStmt = conn.prepareStatement(
                        "SELECT password FROM users WHERE user_id = ?");
                pwdStmt.setInt(1, userId);
                ResultSet pwdRs = pwdStmt.executeQuery();
                if (pwdRs.next()) {
                    String storedPassword = pwdRs.getString("password");
                    if (!storedPassword.equals(oldPassword)) {
                        showAlert(Alert.AlertType.WARNING, "Validation Error", "Incorrect old password");
                        return;
                    }
                } else {
                    showAlert(Alert.AlertType.ERROR, "Error", "User not found");
                    return;
                }
            }

            String profilePicPath = null;
            if (selectedImageFile != null) {
                String destDirPath = "src/main/resources/images/profiles/";
                Files.createDirectories(Paths.get(destDirPath));
                String fileName = "profile_" + userId + "_" + System.currentTimeMillis() + ".png";
                File outputFile = new File(destDirPath + fileName);

                System.out.println("Opening cropping dialog for: " + selectedImageFile.getAbsolutePath());
                BufferedImage croppedImage = showCroppingDialog(selectedImageFile);
                if (croppedImage != null) {
                    ImageIO.write(croppedImage, "png", outputFile);
                    profilePicPath = fileName;
                    System.out.println("Cropped profile picture saved to: " + outputFile.getAbsolutePath());
                    profileImageView.setImage(new Image(outputFile.toURI().toString()));
                } else {
                    System.out.println("Cropping cancelled by user");
                    showAlert(Alert.AlertType.INFORMATION, "Cancelled", "Profile picture update cancelled.");
                    return;
                }
            }

            StringBuilder updateQuery = new StringBuilder("UPDATE users SET ");
            List<String> updateParts = new ArrayList<>();
            List<Object> params = new ArrayList<>();
            if (!name.isEmpty()) {
                updateParts.add("username = ?");
                params.add(name);
            }
            if (!email.isEmpty()) {
                updateParts.add("email = ?");
                params.add(email);
            }
            if (profilePicPath != null) {
                updateParts.add("profile_picture = ?");
                params.add(profilePicPath);
            }
            if (!newPassword.isEmpty()) {
                updateParts.add("password = ?");
                params.add(newPassword);
            }
            if (updateParts.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Validation Error", "No valid fields to update");
                return;
            }
            updateQuery.append(String.join(", ", updateParts));
            updateQuery.append(" WHERE user_id = ?");
            params.add(userId);

            PreparedStatement updateStmt = conn.prepareStatement(updateQuery.toString());
            for (int i = 0; i < params.size(); i++) {
                updateStmt.setObject(i + 1, params.get(i));
            }
            updateStmt.executeUpdate();
            System.out.println("User profile updated: " +
                    (name.isEmpty() ? "" : "username=" + name + ", ") +
                    (email.isEmpty() ? "" : "email=" + email + ", ") +
                    (profilePicPath != null ? "profile_picture=" + profilePicPath + ", " : "") +
                    (newPassword.isEmpty() ? "" : "password updated"));

            if (parentController != null) {
                parentController.closeEditOverlay();
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Cannot close edit overlay: parentController is null");
            }
        } catch (SQLException | IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to save profile: " + e.getMessage());
        }
    }

    private BufferedImage showCroppingDialog(File imageFile) {
        try {
            Image fxImage = new Image(imageFile.toURI().toString(), 400, 400, true, true);
            if (fxImage.isError()) {
                throw new IOException("Failed to load image: " + fxImage.getException().getMessage());
            }
            System.out.println("Image loaded: " + imageFile.getAbsolutePath() + ", width: " + fxImage.getWidth() + ", height: " + fxImage.getHeight());

            Stage dialog = new Stage();
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setTitle("Crop Profile Picture");

            Pane pane = new Pane();
            ImageView imageView = new ImageView(fxImage);
            imageView.setFitWidth(400);
            imageView.setFitHeight(400);
            imageView.setPreserveRatio(true);
            imageView.setSmooth(true);

            double imgWidth = fxImage.getWidth();
            double imgHeight = fxImage.getHeight();
            imageView.setX((400 - imageView.getFitWidth()) / 2);
            imageView.setY((400 - imageView.getFitHeight()) / 2);

            double initialRadius = Math.min(imageView.getFitWidth(), imageView.getFitHeight()) * 0.3;
            Circle cropCircle = new Circle(
                    imageView.getFitWidth() / 2 + imageView.getX(),
                    imageView.getFitHeight() / 2 + imageView.getY(),
                    initialRadius
            );
            cropCircle.setFill(Color.color(0, 0, 0, 0.3));
            cropCircle.setStroke(Color.RED);
            cropCircle.setStrokeWidth(2);

            final double[] dragStartX = {0};
            final double[] dragStartY = {0};
            final boolean[] isResizing = {false};

            // Change cursor when hovering near edge
            cropCircle.setOnMouseMoved(event -> {
                double dx = event.getX() - cropCircle.getCenterX();
                double dy = event.getY() - cropCircle.getCenterY();
                double distance = Math.sqrt(dx * dx + dy * dy);
                if (Math.abs(distance - cropCircle.getRadius()) < 15) {
                    cropCircle.setCursor(Cursor.CROSSHAIR);
                } else {
                    cropCircle.setCursor(Cursor.MOVE);
                }
            });

            cropCircle.setOnMousePressed(event -> {
                dragStartX[0] = event.getX();
                dragStartY[0] = event.getY();
                double dx = event.getX() - cropCircle.getCenterX();
                double dy = event.getY() - cropCircle.getCenterY();
                double distance = Math.sqrt(dx * dx + dy * dy);
                isResizing[0] = Math.abs(distance - cropCircle.getRadius()) < 15;
                System.out.println("Mouse pressed: isResizing=" + isResizing[0] + ", x=" + event.getX() + ", y=" + event.getY());
            });

            cropCircle.setOnMouseDragged(event -> {
                double deltaX = event.getX() - dragStartX[0];
                double deltaY = event.getY() - dragStartY[0];
                if (isResizing[0]) {
                    // Resize based on distance from center
                    double dx = event.getX() - cropCircle.getCenterX();
                    double dy = event.getY() - cropCircle.getCenterY();
                    double newRadius = Math.sqrt(dx * dx + dy * dy);
                    newRadius = Math.max(30, Math.min(newRadius, Math.min(imageView.getFitWidth(), imageView.getFitHeight()) / 2));
                    cropCircle.setRadius(newRadius);
                    System.out.println("Resizing: newRadius=" + newRadius);
                } else {
                    double newCenterX = cropCircle.getCenterX() + deltaX;
                    double newCenterY = cropCircle.getCenterY() + deltaY;
                    newCenterX = Math.max(imageView.getX() + cropCircle.getRadius(), Math.min(newCenterX, imageView.getX() + imageView.getFitWidth() - cropCircle.getRadius()));
                    newCenterY = Math.max(imageView.getY() + cropCircle.getRadius(), Math.min(newCenterY, imageView.getY() + imageView.getFitHeight() - cropCircle.getRadius()));
                    cropCircle.setCenterX(newCenterX);
                    cropCircle.setCenterY(newCenterY);
                    System.out.println("Moving: newCenterX=" + newCenterX + ", newCenterY=" + newCenterY);
                }
                dragStartX[0] = event.getX();
                dragStartY[0] = event.getY();
            });

            pane.getChildren().addAll(imageView, cropCircle);

            Button confirmButton = new Button("Confirm Crop");
            confirmButton.setLayoutX(10);
            confirmButton.setLayoutY(imageView.getFitHeight() + imageView.getY() + 10);
            Button cancelButton = new Button("Cancel");
            cancelButton.setLayoutX(100);
            cancelButton.setLayoutY(imageView.getFitHeight() + imageView.getY() + 10);

            final BufferedImage[] croppedImage = {null};

            confirmButton.setOnAction(e -> {
                try {
                    BufferedImage originalImage = ImageIO.read(imageFile);
                    double scaleX = originalImage.getWidth() / imageView.getFitWidth();
                    double scaleY = originalImage.getHeight() / imageView.getFitHeight();
                    int radius = (int) (cropCircle.getRadius() * scaleX);
                    int centerX = (int) ((cropCircle.getCenterX() - imageView.getX()) * scaleX);
                    int centerY = (int) ((cropCircle.getCenterY() - imageView.getY()) * scaleY);

                    int size = radius * 2;
                    BufferedImage outputImage = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
                    Graphics2D g2d = outputImage.createGraphics();
                    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2d.setClip(new Ellipse2D.Float(0, 0, size, size));
                    g2d.drawImage(originalImage, -centerX + radius, -centerY + radius, null);
                    g2d.dispose();

                    croppedImage[0] = outputImage;
                    System.out.println("Crop confirmed: size=" + size + ", centerX=" + centerX + ", centerY=" + centerY);
                    dialog.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to crop image: " + ex.getMessage());
                }
            });

            cancelButton.setOnAction(e -> {
                System.out.println("Crop cancelled");
                dialog.close();
            });

            pane.getChildren().addAll(confirmButton, cancelButton);

            Scene scene = new Scene(pane, 420, imageView.getFitHeight() + imageView.getY() + 50);
            dialog.setScene(scene);
            dialog.setResizable(false);
            System.out.println("Showing cropping dialog");
            dialog.showAndWait();

            return croppedImage[0];
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load image for cropping: " + e.getMessage());
            return null;
        }
    }

    private String getExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        return (dotIndex == -1) ? "" : fileName.substring(dotIndex);
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}