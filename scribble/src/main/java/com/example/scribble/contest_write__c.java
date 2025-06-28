package com.example.scribble;


import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.stage.Modality;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Logger;
import com.example.scribble.AppState_c;

import java.util.concurrent.atomic.AtomicReference;


public class contest_write__c {

    private static final Logger LOGGER = Logger.getLogger(contest_write__c.class.getName());


    @FXML
    private Button back_button;

    @FXML
    private Button upload_button;

    @FXML
    private Button cover_photo_button;

    @FXML
    private TextField book_tittle;

    @FXML
    private Label genre_name;

    @FXML
    private ImageView cover_photo;

    @FXML
    private TextArea writing_area;

    private int contestId;
    private String genre;
    private int userId;
    private String username;
    private String userPhotoPath;
    private String selectedCoverPhotoPath;

    @FXML private nav_bar__c mainController;

    public void setMainController(nav_bar__c mainController) {
        this.mainController = mainController;
        LOGGER.info("Set mainController in contest_write__c");
    }

    public void initData(int contestId, String genre, int userId, String username, String userPhotoPath) {
        this.contestId = contestId;
        this.genre = genre;
        this.userId = userId;
        this.username = username;
        this.userPhotoPath = userPhotoPath;

        book_tittle.setText("");
        book_tittle.setPromptText("write the title of this book");

        book_tittle.setStyle("-fx-background-color: transparent; -fx-text-fill: white;");
        genre_name.setText("(genre: " + genre.toLowerCase() + ")");
        selectedCoverPhotoPath = null;
        try {
            String defaultImagePath = "/images/contest_book_cover/demo_cover_photo.png";
            java.net.URL resource = getClass().getResource(defaultImagePath);
            if (resource != null) {
                cover_photo.setImage(new Image(resource.toExternalForm()));
            } else {
                showErrorAlert("Resource Error", "Default cover photo not found in classpath.");
                cover_photo.setImage(null);
            }
        } catch (Exception e) {
            e.printStackTrace();
            showErrorAlert("Resource Error", "Failed to load default cover photo.");
            cover_photo.setImage(null);
        }
        writing_area.setText("");
    }

    @FXML
    private void handle_back_button(ActionEvent event) {
        if (mainController == null) {
            LOGGER.severe("Main controller is null, cannot navigate back from contest write page");
            showErrorAlert("Error", "Navigation failed: main controller is not initialized.");
            return;
        }

        try {
            // Retrieve previous FXML from AppState_c
            String previousFXML = AppState_c.getInstance().getPreviousFXML();
            if (previousFXML == null || previousFXML.isEmpty()) {
                previousFXML = "/com/example/scribble/contest_entries.fxml"; // Fallback
                LOGGER.warning("No previous FXML set in AppState_c, using default: " + previousFXML);
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource(previousFXML));
            if (loader.getLocation() == null) {
                showErrorAlert("Resource Error", "Previous page resource not found: " + previousFXML);
                return;
            }

            Parent root = loader.load();
            Object controller = loader.getController();

            // Pass mainController to the target controller if supported
            if (controller instanceof contest_entries__c entriesController) {
                entriesController.initData(contestId, genre, userId, username);
                entriesController.setMainController(mainController);
            } else if (controller instanceof contest__c contestController) {
                contestController.setMainController(mainController);
            } else {
                LOGGER.info("Target controller does not support setMainController: " + controller.getClass().getName());
            }

            // Load into centerPane to preserve nav bar
            mainController.getCenterPane().getChildren().setAll(root);
            LOGGER.info("Navigated back to " + previousFXML);
        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Navigation Error", "Failed to return to the previous page: " + e.getMessage());
        }
    }

    @FXML
    private void handle_upload_button(ActionEvent event) {
        System.out.println("Upload button clicked at " + new java.util.Date());
        String entryTitle = book_tittle.getText().trim();
        String content = writing_area.getText().trim();

        if (entryTitle.isEmpty() || content.isEmpty()) {
            showErrorAlert("Input Error", "Please provide a title and content for your entry.");
            System.out.println("Validation failed: Empty title or content");
            return;
        }

        if (!UserSession.getInstance().isLoggedIn()) {
            showErrorAlert("Session Error", "You must be logged in to submit an entry.");
            System.out.println("Validation failed: User not logged in");
            return;
        }

        if (userId != UserSession.getInstance().getUserId()) {
            showErrorAlert("Session Error", "User ID mismatch. Please log in with the correct account.");
            System.out.println("Validation failed: userId " + userId + " does not match session userId " + UserSession.getInstance().getUserId());
            return;
        }

        if (mainController == null) {
            LOGGER.severe("Main controller is null, cannot navigate to contest entries page");
            showErrorAlert("Error", "Navigation failed: main controller is not initialized.");
            return;
        }


        // New validation: Check for minimum 200 words in content
        String[] words = content.split("\\s+");
        if (words.length < 200) {
            showErrorAlert("Input Error", "Please provide at least 200 words in the content.");
            System.out.println("Validation failed: Content has only " + words.length + " words");
            return;
        }

        // New validation: Check for cover photo
        if (selectedCoverPhotoPath == null) {
            showErrorAlert("Input Error", "Please upload a cover photo for your entry.");
            System.out.println("Validation failed: No cover photo selected");
            return;
        }

        try (Connection conn = db_connect.getConnection()) {
            System.out.println("Database connection established: " + (conn != null));
            conn.setAutoCommit(false);

            // Validate contestId exists and matches genre
            String validateContestSQL = "SELECT contest_id FROM contests WHERE contest_id = ? AND genre = ? COLLATE utf8mb4_bin";
            try (PreparedStatement validateStmt = conn.prepareStatement(validateContestSQL)) {
                validateStmt.setInt(1, contestId);
                validateStmt.setString(2, genre);
                ResultSet rs = validateStmt.executeQuery();
                if (!rs.next()) {
                    conn.rollback();
                    showErrorAlert("Contest Error", "Invalid contest ID or genre mismatch.");
                    System.out.println("Validation failed: Contest ID " + contestId + " not found or genre mismatch for genre " + genre);
                    return;
                }
            }

            // Check for duplicate entry title for this contest
            String checkDuplicateSQL = "SELECT entry_id FROM contest_entries WHERE contest_id = ? AND entry_title = ?";
            try (PreparedStatement checkStmt = conn.prepareStatement(checkDuplicateSQL)) {
                checkStmt.setInt(1, contestId);
                checkStmt.setString(2, entryTitle);
                ResultSet rs = checkStmt.executeQuery();
                if (rs.next()) {
                    conn.rollback();
                    showErrorAlert("Input Error", "An entry with this title already exists for this contest.");
                    System.out.println("Validation failed: Duplicate entry title '" + entryTitle + "' for contestId " + contestId);
                    return;
                }
            }

            // Insert entry
            String insertEntrySQL = "INSERT INTO contest_entries (contest_id, user_id, entry_title, content, cover_photo) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(insertEntrySQL)) {
                stmt.setInt(1, contestId);
                stmt.setInt(2, userId);
                stmt.setString(3, entryTitle);
                stmt.setString(4, content);
                stmt.setString(5, selectedCoverPhotoPath);
                System.out.println("Executing entry insert with contestId: " + contestId + ", userId: " + userId);
                int rowsAffected = stmt.executeUpdate();
                System.out.println("Rows affected for entry: " + rowsAffected);
            }

            conn.commit();
            System.out.println("Transaction committed successfully");
            showInfoAlert("Success", "Your entry has been submitted successfully!");
            clearForm();

            // Navigate to contest_entries page using centerPane
            try {
                java.net.URL resource = getClass().getResource("/com/example/scribble/contest_entries.fxml");
                if (resource == null) {
                    System.err.println("Resource not found: /com/example/scribble/contest_entries.fxml");
                    showErrorAlert("Resource Error", "Contest entries FXML file not found. Check the file path in src/main/resources.");
                    return;
                }
                System.out.println("Resource found at: " + resource);
                FXMLLoader loader = new FXMLLoader(resource);
                Parent root = loader.load();
                contest_entries__c controller = loader.getController();
                controller.initData(contestId, genre, userId, username);
                controller.setMainController(mainController); // Pass mainController

                // Set previous FXML in AppState_c
                AppState_c.getInstance().setPreviousFXML("/com/example/scribble/contest.fxml");

                // Load into centerPane
                mainController.getCenterPane().getChildren().setAll(root);
                LOGGER.info("Navigated to contest_entries.fxml");
            } catch (IOException e) {
                e.printStackTrace();
                showErrorAlert("Navigation Error", "Failed to load the contest entries page: " + e.getMessage());
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showErrorAlert("Database Error", "Failed to submit your entry. Error: " + e.getMessage());
            System.out.println("SQLException caught: " + e.getMessage());
        }
    }




    @FXML
    private void handle_cover_photo_button(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Cover Photo");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );

        File selectedFile = fileChooser.showOpenDialog(cover_photo_button.getScene().getWindow());
        if (selectedFile != null) {
            openCropStage(selectedFile);
        }
    }

    private void openCropStage(File selectedFile) {
        Stage cropStage = new Stage();
        cropStage.initModality(Modality.APPLICATION_MODAL);
        cropStage.setTitle("Crop Cover Photo");

        Image originalImage = new Image(selectedFile.toURI().toString());
        ImageView imageView = new ImageView(originalImage);
        imageView.setPreserveRatio(true);

        double displayWidth = Math.min(originalImage.getWidth(), 600);
        double displayHeight = (displayWidth / originalImage.getWidth()) * originalImage.getHeight();
        imageView.setFitWidth(displayWidth);
        imageView.setFitHeight(displayHeight);

        final double ASPECT_RATIO = 150.0 / 222.0; // Width / Height
        final double MIN_WIDTH = 50.0;
        final double MAX_WIDTH = displayWidth;

        Rectangle cropRect = new Rectangle(150, 222);
        cropRect.setArcWidth(30);
        cropRect.setArcHeight(30);
        cropRect.setFill(Color.TRANSPARENT);
        cropRect.setStroke(Color.RED);
        cropRect.setStrokeWidth(2);

        Circle resizeHandle = new Circle(cropRect.getX() + cropRect.getWidth(), cropRect.getY() + cropRect.getHeight(), 5, Color.BLUE);

        Pane pane = new Pane(imageView, cropRect, resizeHandle);
        pane.setPrefSize(displayWidth, displayHeight);

        double[] dragStart = new double[2];
        cropRect.setOnMousePressed(e -> {
            dragStart[0] = e.getX() - cropRect.getX();
            dragStart[1] = e.getY() - cropRect.getY();
        });

        cropRect.setOnMouseDragged(e -> {
            double newX = e.getX() - dragStart[0];
            double newY = e.getY() - dragStart[1];
            newX = Math.max(0, Math.min(newX, displayWidth - cropRect.getWidth()));
            newY = Math.max(0, Math.min(newY, displayHeight - cropRect.getHeight()));
            cropRect.setX(newX);
            cropRect.setY(newY);
            resizeHandle.setCenterX(newX + cropRect.getWidth());
            resizeHandle.setCenterY(newY + cropRect.getHeight());
        });

        double[] resizeStart = new double[2];
        AtomicReference<Double> initialWidth = new AtomicReference<>(cropRect.getWidth());
        resizeHandle.setOnMousePressed(e -> {
            resizeStart[0] = e.getX();
            resizeStart[1] = e.getY();
            initialWidth.set(cropRect.getWidth());
        });

        resizeHandle.setOnMouseDragged(e -> {
            double deltaX = e.getX() - resizeStart[0];
            double newWidth = initialWidth.get() + deltaX;
            newWidth = Math.max(MIN_WIDTH, Math.min(newWidth, MAX_WIDTH));
            double newHeight = newWidth * (222.0 / 150.0); // Correct aspect ratio: height = width * (height/width)
            if (cropRect.getX() + newWidth <= displayWidth && cropRect.getY() + newHeight <= displayHeight) {
                cropRect.setWidth(newWidth);
                cropRect.setHeight(newHeight);
                resizeHandle.setCenterX(cropRect.getX() + newWidth);
                resizeHandle.setCenterY(cropRect.getY() + newHeight);
            }
        });

        Button cropButton = new Button("Crop and Save");
        cropButton.setOnAction(e -> {
            try {
                double scale = originalImage.getWidth() / displayWidth;
                int cropX = (int) (cropRect.getX() * scale);
                int cropY = (int) (cropRect.getY() * scale);
                int cropWidth = (int) (cropRect.getWidth() * scale);
                int cropHeight = (int) (cropRect.getHeight() * scale);

                BufferedImage bufferedImage = new BufferedImage(150, 222, BufferedImage.TYPE_INT_ARGB);
                java.awt.Graphics2D g2d = bufferedImage.createGraphics();
                g2d.setClip(new java.awt.geom.RoundRectangle2D.Double(0, 0, 150, 222, 30, 30));
                BufferedImage sourceImage = ImageIO.read(selectedFile);
                g2d.drawImage(sourceImage, 0, 0, 150, 222, cropX, cropY, cropX + cropWidth, cropY + cropHeight, null);
                g2d.dispose();

                Path directoryPath = Path.of("src/main/resources/images/contest_book_cover");
                Files.createDirectories(directoryPath);

                int nextNumber = 1;
                try (DirectoryStream<Path> stream = Files.newDirectoryStream(directoryPath, "ccp_[0-9]*.{png,jpg,jpeg}")) {
                    for (Path file : stream) {
                        String filename = file.getFileName().toString();
                        String numberPart = filename.replace("ccp_", "").replaceAll("\\..*", "");
                        try {
                            int num = Integer.parseInt(numberPart);
                            if (num >= nextNumber) {
                                nextNumber = num + 1;
                            }
                        } catch (NumberFormatException ignored) {}
                    }
                }

                String newFilename = "ccp_" + nextNumber + ".png";
                Path destinationPath = directoryPath.resolve(newFilename);

                ImageIO.write(bufferedImage, "png", destinationPath.toFile());
                selectedCoverPhotoPath = newFilename;
                cover_photo.setImage(new Image("file:" + destinationPath.toString()));
                cropStage.close();
            } catch (IOException ex) {
                ex.printStackTrace();
                showErrorAlert("Image Error", "Failed to crop and save the cover photo.");
            }
        });

        VBox layout = new VBox(10, pane, cropButton);
        layout.setPadding(new javafx.geometry.Insets(10));
        Scene cropScene = new Scene(layout);
        cropStage.setScene(cropScene);
        cropStage.showAndWait();
    }

    // Helper method to get file extension
    private String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');
        return (lastDotIndex == -1) ? "png" : filename.substring(lastDotIndex + 1).toLowerCase();
    }


    private void clearForm() {
        book_tittle.setText("");
        writing_area.setText("");
        selectedCoverPhotoPath = null;
        try {
            String defaultImagePath = "/images/contest_book_cover/demo_cover_photo.png";
            java.net.URL resource = getClass().getResource(defaultImagePath);
            if (resource != null) {
                cover_photo.setImage(new Image(resource.toExternalForm()));
            } else {
                showErrorAlert("Resource Error", "Default cover photo not found in classpath.");
                cover_photo.setImage(null);
            }
        } catch (Exception e) {
            e.printStackTrace();
            showErrorAlert("Resource Error", "Failed to load default cover photo.");
            cover_photo.setImage(null);
        }
    }

    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfoAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}