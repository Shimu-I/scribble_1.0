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
import java.util.ArrayList;
import java.util.List;
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
    @FXML
    private Button delete_profile_pic;

    private profile__c parentController;
    private nav_bar__c mainController; // Fixed: Changed from boolean to nav_bar__c
    private File selectedImageFile;
    private BufferedImage croppedImage;
    private int userId;

    public void setParentController(profile__c parentController) {
        this.parentController = parentController;
        System.out.println("setParentController called in profile_info_edit__c with parentController: " + (parentController != null ? "set" : "null"));
    }

    public void setMainController(nav_bar__c mainController) {
        this.mainController = mainController;
        System.out.println("setMainController called in profile_info_edit__c with mainController: " + (mainController != null ? "set" : "null"));
    }

    public void setUserId(int userId) {
        this.userId = userId;
        System.out.println("setUserId called in profile_info_edit__c with userId: " + userId);
        loadUserData();
    }

    @FXML
    public void initialize() {
        System.out.println("profile_info_edit__c initialized");
        setDefaultProfileImage();
    }

    private void setDefaultProfileImage() {
        try {
            String defaultImagePath = "/images/profiles/hollow_circle.png";
            Image defaultImage = new Image(getClass().getResource(defaultImagePath).toExternalForm());
            profileImageView.setImage(defaultImage);
            System.out.println("Set default profile image: " + defaultImagePath);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load default profile image: " + e.getMessage());
        }
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
                    loadProfileImage(profilePic);
                } else {
                    setDefaultProfileImage();
                }
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "User not found");
                setDefaultProfileImage();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load user data: " + e.getMessage());
            setDefaultProfileImage();
        }
    }

    private void loadProfileImage(String profilePicName) {
        if (profilePicName != null && !profilePicName.isEmpty()) {
            try {
                // Try loading from filesystem first
                java.io.File uploadFile = new java.io.File("Uploads/profiles/" + profilePicName);
                if (uploadFile.exists()) {
                    Image image = new Image("file:" + uploadFile.getAbsolutePath(), true);
                    if (!image.isError()) {
                        profileImageView.setImage(image);
                        System.out.println("Loaded profile image from filesystem: file:" + uploadFile.getAbsolutePath());
                        return;
                    } else {
                        System.out.println("Failed to load profile image from filesystem (image error): " + profilePicName);
                    }
                } else {
                    // Fall back to classpath
                    String imagePath = "/images/profiles/" + profilePicName;
                    java.net.URL resource = getClass().getResource(imagePath);
                    if (resource != null) {
                        Image image = new Image(resource.toExternalForm(), true);
                        if (!image.isError()) {
                            profileImageView.setImage(image);
                            System.out.println("Loaded profile image from classpath: " + imagePath);
                            return;
                        } else {
                            System.out.println("Failed to load profile image from classpath (image error): " + profilePicName);
                        }
                    } else {
                        System.out.println("Profile image not found: " + profilePicName);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("Failed to load profile image: " + profilePicName + ", falling back to default");
            }
        } else {
            System.out.println("No profile picture provided, using default");
        }
        setDefaultProfileImage();
    }

    @FXML
    private void handle_end(ActionEvent event) {
        System.out.println("End button clicked, closing modal");
        Stage stage = (Stage) end.getScene().getWindow();
        stage.close();
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
                if (image.isError()) {
                    throw new IOException("Failed to load selected image: " + image.getException().getMessage());
                }
                profileImageView.setImage(image);
                selectedImageFile = file;
                System.out.println("Selected profile picture: " + file.getAbsolutePath());
                croppedImage = showCroppingDialog(selectedImageFile);
                if (croppedImage != null) {
                    Image croppedFxImage = convertBufferedImageToFxImage(croppedImage);
                    profileImageView.setImage(croppedFxImage);
                    System.out.println("Cropped image set in ImageView");
                } else {
                    System.out.println("Cropping cancelled, reverting to original image");
                    profileImageView.setImage(image);
                }
            } catch (Exception e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to load profile picture: " + e.getMessage());
                setDefaultProfileImage();
            }
        } else {
            System.out.println("No file selected.");
        }
    }

    @FXML
    private void handle_delete_save_button(ActionEvent event) {
        if (userId == 0) {
            showAlert(Alert.AlertType.ERROR, "Error", "No user ID set");
            return;
        }

        try (Connection conn = db_connect.getConnection()) {
            PreparedStatement selectStmt = conn.prepareStatement(
                    "SELECT profile_picture FROM users WHERE user_id = ?");
            selectStmt.setInt(1, userId);
            ResultSet rs = selectStmt.executeQuery();

            String profilePicName = null;
            if (rs.next()) {
                profilePicName = rs.getString("profile_picture");
            }

            PreparedStatement updateStmt = conn.prepareStatement(
                    "UPDATE users SET profile_picture = NULL WHERE user_id = ?");
            updateStmt.setInt(1, userId);
            updateStmt.executeUpdate();

            if (profilePicName != null && !profilePicName.isEmpty()) {
                File profilePicFile = new File("Uploads/profiles/" + profilePicName); // Fixed: Check Uploads/profiles/
                if (profilePicFile.exists()) {
                    Files.deleteIfExists(profilePicFile.toPath());
                    System.out.println("Deleted profile picture file: " + profilePicFile.getAbsolutePath());
                }
            }

            setDefaultProfileImage();
            selectedImageFile = null;
            croppedImage = null;
            UserSession.getInstance().setUserPhotoPath(null);
            UserSession.getInstance().saveToFile();
            // Notify nav_bar__c to refresh UI
            if (mainController != null) {
                mainController.updateUIVisibility();
                System.out.println("Notified nav_bar__c to refresh UI after profile picture deletion");
            } else {
                System.err.println("mainController is null; cannot notify nav_bar__c to refresh UI");
            }

            showAlert(Alert.AlertType.INFORMATION, "Success", "Profile picture has been deleted.");
        } catch (SQLException | IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete profile picture: " + e.getMessage());
        }
    }

    @FXML
    private void handle_save_button(ActionEvent event) {
        System.out.println("Save button clicked");
        String name = edit_name.getText().trim();
        String email = edit_email.getText().trim();

        if (name.isEmpty() && email.isEmpty() && croppedImage == null) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "At least one field must be provided to update");
            return;
        }

        if (!email.isEmpty() && !email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Invalid email format");
            return;
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

            String profilePicPath = null;
            if (croppedImage != null) {
                String destDirPath = "Uploads/profiles/";
                Files.createDirectories(Paths.get(destDirPath));
                String fileName = "profile_" + userId + "_" + System.currentTimeMillis() + ".png";
                File outputFile = new File(destDirPath + fileName);
                ImageIO.write(croppedImage, "png", outputFile);
                profilePicPath = fileName;
                System.out.println("Cropped profile picture saved to: " + outputFile.getAbsolutePath());
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
                    (profilePicPath != null ? "profile_picture=" + profilePicPath : ""));

            // Update UserSession with new profile picture path
            if (profilePicPath != null) {
                UserSession.getInstance().setUserPhotoPath(profilePicPath);
                UserSession.getInstance().saveToFile();
                // Notify nav_bar__c to refresh UI
                if (mainController != null) {
                    mainController.updateUIVisibility();
                    System.out.println("Notified nav_bar__c to refresh UI with new profile picture: " + profilePicPath);
                } else {
                    System.err.println("mainController is null; cannot notify nav_bar__c to refresh UI");
                }
            }

            // Notify parentController to refresh
            if (parentController != null) {
                parentController.loadUserProfile();
                System.out.println("Notified parentController to refresh profile");
            }

            Stage stage = (Stage) save_button.getScene().getWindow();
            stage.close();
            showAlert(Alert.AlertType.INFORMATION, "Success", "Profile updated successfully");
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

            final BufferedImage[] croppedImageResult = {null};

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

                    croppedImageResult[0] = outputImage;
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

            return croppedImageResult[0];
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load image for cropping: " + e.getMessage());
            return null;
        }
    }

    private Image convertBufferedImageToFxImage(BufferedImage bufferedImage) {
        try {
            File tempFile = File.createTempFile("cropped_", ".png");
            ImageIO.write(bufferedImage, "png", tempFile);
            Image fxImage = new Image(tempFile.toURI().toString());
            tempFile.deleteOnExit();
            return fxImage;
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to convert cropped image: " + e.getMessage());
            return null;
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}