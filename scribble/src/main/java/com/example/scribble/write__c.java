package com.example.scribble;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicReference;
import java.sql.*;
import java.util.logging.Logger;

public class write__c {

    @FXML
    public BorderPane rootPane;

    private static final Logger LOGGER = Logger.getLogger(write__c.class.getName());

    @FXML
    public Button back_to_books;

    @FXML
    private Button book_image_button;

    @FXML
    private ImageView bookCoverImageView;

    private String coverPhotoPath;

    @FXML
    private TextField book_title;

    @FXML
    private TextArea book_description;

    @FXML
    private ComboBox<String> genreComboBox;

    @FXML
    private ComboBox<String> statusComboBox;

    @FXML
    private Button write_button;

    @FXML
    private nav_bar__c mainController;

    public void setMainController(nav_bar__c mainController) {
        this.mainController = mainController;
    }

    @FXML
    private void initialize() {
        genreComboBox.getItems().addAll(
                "Fantasy", "Thriller", "Mystery", "Thriller Mystery", "Youth Fiction",
                "Crime", "Horror", "Romance", "Science Fiction", "Adventure", "Historical"
        );
        genreComboBox.getSelectionModel().selectFirst();

        statusComboBox.getItems().addAll(
                "Ongoing", "Complete", "Hiatus"
        );
        statusComboBox.getSelectionModel().selectFirst();

        // Apply rounded corners to bookCoverImageView
        Rectangle clip = new Rectangle(150, 222);
        clip.setArcWidth(30);
        clip.setArcHeight(30);
        bookCoverImageView.setClip(clip);
    }

    @FXML
    private void handle_back_to_books(ActionEvent actionEvent) {
        if (mainController != null) {
            mainController.loadFXML("reading_list.fxml"); // Navigate back to Books
        } else {
            System.err.println("Main controller is null in write__c.");
        }
    }

    @FXML
    private void handle_book_cover(ActionEvent actionEvent) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Book Cover Image");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );
        File selectedFile = fileChooser.showOpenDialog(book_image_button.getScene().getWindow());
        if (selectedFile != null) {
            openCropStage(selectedFile);
        }
    }

    private void openCropStage(File selectedFile) {
        Stage cropStage = new Stage();
        cropStage.initModality(Modality.APPLICATION_MODAL);
        cropStage.setTitle("Crop Book Cover");

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
            double newHeight = newWidth * (222.0 / 150.0); // Height = width * (height/width)
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

                Path directoryPath = Path.of("src/main/resources/images/book_covers");
                Files.createDirectories(directoryPath);

                int nextNumber = 1;
                try (DirectoryStream<Path> stream = Files.newDirectoryStream(directoryPath, "bc_[0-9]*.png")) {
                    for (Path file : stream) {
                        String filename = file.getFileName().toString();
                        String numberPart = filename.replace("bc_", "").replaceAll("\\..*", "");
                        try {
                            int num = Integer.parseInt(numberPart);
                            if (num >= nextNumber) {
                                nextNumber = num + 1;
                            }
                        } catch (NumberFormatException ignored) {}
                    }
                }

                String newFilename = "bc_" + nextNumber + ".png";
                Path destinationPath = directoryPath.resolve(newFilename);

                ImageIO.write(bufferedImage, "png", destinationPath.toFile());
                coverPhotoPath = newFilename;
                bookCoverImageView.setImage(new Image("file:" + destinationPath.toString()));
                Rectangle clip = new Rectangle(150, 222);
                clip.setArcWidth(30);
                clip.setArcHeight(30);
                bookCoverImageView.setClip(clip);
                cropStage.close();
            } catch (IOException ex) {
                ex.printStackTrace();
                showAlert("Image Error", "Failed to crop and save the cover photo.");
            }
        });

        VBox layout = new VBox(10, pane, cropButton);
        layout.setPadding(new javafx.geometry.Insets(10));
        Scene cropScene = new Scene(layout);
        cropStage.setScene(cropScene);
        cropStage.showAndWait();
    }

    @FXML
    private void handleWriteButton(ActionEvent event) {
        LOGGER.info("Write button clicked");
        String title = book_title.getText();
        String description = book_description.getText();
        String genre = genreComboBox.getSelectionModel().getSelectedItem();
        String status = statusComboBox.getSelectionModel().getSelectedItem();
        String coverPhoto = coverPhotoPath;

        // Validate inputs
        if (title.isEmpty() || description.isEmpty()) {
            showAlert("Error", "Please fill in title and description.");
            return;
        }
        if (genre == null || status == null) {
            showAlert("Error", "Please select a genre and status.");
            return;
        }

        // Get user_id from UserSession
        UserSession session = UserSession.getInstance();
        if (!session.isLoggedIn()) {
            showAlert("Error", "You must be logged in to create or edit a book.");
            navigateToSignIn();
            return;
        }
        int userId = session.getUserId();

        // Save to database
        Connection conn = null;
        try {
            conn = db_connect.getConnection();
            if (conn == null) {
                showAlert("Error", "Failed to connect to the database.");
                return;
            }
            conn.setAutoCommit(false); // Start transaction

            int bookId = AppState.getInstance().getCurrentBookId(); // Check if editing existing book
            if (bookId > 0 && doesBookExist(bookId, conn)) {
                // Update existing book
                String updateSql = "UPDATE books SET title = ?, description = ?, genre = ?, status = ?, cover_photo = ? WHERE book_id = ?";
                try (PreparedStatement stmt = conn.prepareStatement(updateSql)) {
                    stmt.setString(1, title);
                    stmt.setString(2, description);
                    stmt.setString(3, genre);
                    stmt.setString(4, status);
                    stmt.setString(5, coverPhoto);
                    stmt.setInt(6, bookId);
                    stmt.executeUpdate();
                }
                conn.commit();
                showAlert("Success", "Book updated successfully!");
            } else {
                // Insert new book
                String bookSql = "INSERT INTO books (title, description, genre, status, cover_photo, view_count) VALUES (?, ?, ?, ?, ?, 0)";
                PreparedStatement bookPstmt = conn.prepareStatement(bookSql, PreparedStatement.RETURN_GENERATED_KEYS);
                bookPstmt.setString(1, title);
                bookPstmt.setString(2, description);
                bookPstmt.setString(3, genre);
                bookPstmt.setString(4, status);
                bookPstmt.setString(5, coverPhoto);
                bookPstmt.executeUpdate();

                ResultSet generatedKeys = bookPstmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    bookId = generatedKeys.getInt(1);
                } else {
                    throw new SQLException("Failed to retrieve book_id.");
                }

                String authorSql = "INSERT INTO book_authors (book_id, user_id, role) VALUES (?, ?, 'Owner')";
                try (PreparedStatement authorPstmt = conn.prepareStatement(authorSql)) {
                    authorPstmt.setInt(1, bookId);
                    authorPstmt.setInt(2, userId);
                    authorPstmt.executeUpdate();
                }
                conn.commit();
                showAlert("Success", "Book created successfully!");
            }

            // Navigate to chapter page
            navigateToChapterPage(bookId, title, userId);
            clearForm();
        } catch (SQLException e) {
            try {
                if (conn != null) conn.rollback();
            } catch (SQLException rollbackEx) {
                showAlert("Error", "Rollback failed: " + rollbackEx.getMessage());
            }
            showAlert("Error", "Failed to save book: " + e.getMessage());
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException closeEx) {
                    showAlert("Error", "Failed to close connection: " + closeEx.getMessage());
                }
            }
        }
    }

    private boolean doesBookExist(int bookId, Connection conn) throws SQLException {
        String query = "SELECT COUNT(*) FROM books WHERE book_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, bookId);
            ResultSet rs = stmt.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        }
    }

    private void navigateToSignIn() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("sign_in.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) write_button.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            showAlert("Error", "Failed to load sign-in page: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void navigateToChapterPage(int bookId, String bookName, int authorId) {
        if (mainController != null) {
            System.out.println("Loading write_chapter.fxml via mainController");
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("write_chapter.fxml"));
                mainController.getCenterPane().getChildren().setAll((Node) loader.load());
                write_chapter__c chapterController = loader.getController();
                chapterController.setMainController(mainController);
                chapterController.setBookDetails(bookId, bookName, authorId);
            } catch (IOException e) {
                showAlert("Error", "Failed to load chapter page: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.err.println("Main controller is null in write__c.");
            showAlert("Error", "Cannot navigate to chapter page: Main controller is null.");
        }
    }


    public void setBookId(int bookId) {
        if (bookId <= 0) {
            LOGGER.warning("Invalid bookId provided: " + bookId);
            showAlert("Error", "Invalid book ID.");
            return;
        }
        try (Connection conn = db_connect.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT title, description, genre, status, cover_photo FROM books WHERE book_id = ?")) {
            stmt.setInt(1, bookId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                if (book_title != null) {
                    book_title.setText(rs.getString("title"));
                }
                if (book_description != null) {
                    book_description.setText(rs.getString("description"));
                }
                if (genreComboBox != null) {
                    genreComboBox.getSelectionModel().select(rs.getString("genre"));
                }
                if (statusComboBox != null) {
                    statusComboBox.getSelectionModel().select(rs.getString("status"));
                }
                String coverPhoto = rs.getString("cover_photo");
                if (coverPhoto != null && !coverPhoto.isEmpty() && bookCoverImageView != null) {
                    try {
                        String imagePath = "file:src/main/resources/images/book_covers/" + coverPhoto;
                        Image image = new Image(imagePath);
                        if (!image.isError()) {
                            bookCoverImageView.setImage(image);
                            coverPhotoPath = coverPhoto;
                            Rectangle clip = new Rectangle(150, 222);
                            clip.setArcWidth(30);
                            clip.setArcHeight(30);
                            bookCoverImageView.setClip(clip);
                        } else {
                            LOGGER.warning("Failed to load cover photo: " + coverPhoto);
                        }
                    } catch (Exception e) {
                        LOGGER.warning("Failed to load cover photo: " + coverPhoto + " - " + e.getMessage());
                    }
                }
                LOGGER.info("Loaded book details for bookId: " + bookId);
            } else {
                showAlert("Error", "Book not found for book_id: " + bookId);
            }
        } catch (SQLException e) {
            LOGGER.severe("Failed to load book details for bookId: " + bookId + " - " + e.getMessage());
            showAlert("Error", "Failed to load book details: " + e.getMessage());
        }
    }

    private void clearForm() {
        book_title.clear();
        book_description.clear();
        genreComboBox.getSelectionModel().clearSelection();
        statusComboBox.getSelectionModel().clearSelection();
        bookCoverImageView.setImage(null);
        coverPhotoPath = null;
        Rectangle clip = new Rectangle(150, 222);
        clip.setArcWidth(30);
        clip.setArcHeight(30);
        bookCoverImageView.setClip(clip);
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}