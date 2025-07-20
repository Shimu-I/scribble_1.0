package com.example.scribble;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

public class nav_bar__c {

    @FXML
    private BorderPane rootPane;

    @FXML
    private AnchorPane centerPane;

    @FXML
    private Button home;

    @FXML
    private Button read;

    @FXML
    private Button community;

    @FXML
    private Button contest;

    @FXML
    private Button profile;

    @FXML
    private Button openParallelPage;

    @FXML
    private ImageView user_photo;

    @FXML
    private ImageView user_sign_in; // Still an ImageView in sign-in Button's graphic

    @FXML
    private Button signOutButton; // New Button field for sign-out

    @FXML
    private Button testSignOutButton;

    private static final Logger LOGGER = Logger.getLogger(nav_bar__c.class.getName());

    @FXML
    public void initialize() {
        UserSession.loadFromFile(); // Explicitly load session
        if (user_photo == null) {
            System.err.println("user_photo ImageView is null!");
        } else {
            System.out.println("user_photo ImageView is initialized.");
        }
        if (signOutButton != null) {
            System.out.println("signOutButton initialized: visible=" + signOutButton.isVisible() +
                    ", disabled=" + signOutButton.isDisabled() +
                    ", layoutX=" + signOutButton.getLayoutX() +
                    ", layoutY=" + signOutButton.getLayoutY());
        } else {
            System.err.println("signOutButton is null in initialize!");
        }
        if (testSignOutButton != null) {
            System.out.println("testSignOutButton initialized: visible=" + testSignOutButton.isVisible());
        } else {
            System.err.println("testSignOutButton is null in initialize!");
        }
        loadFXML("Home.fxml");

        updateUIVisibility();
    }

    private Image loadProfileImage(String path) {
        if (path != null && !path.isEmpty()) {
            try {
                // Remove leading slash for filesystem path consistency
                String cleanPath = path.startsWith("/") ? path.substring(1) : path;
                // Try loading from filesystem first
                java.io.File uploadFile = new java.io.File("Uploads/profiles/" + cleanPath);
                if (uploadFile.exists()) {
                    Image image = new Image("file:" + uploadFile.getAbsolutePath(), true);
                    if (!image.isError()) {
                        LOGGER.info("Loaded profile image from filesystem: file:" + uploadFile.getAbsolutePath());
                        return image;
                    } else {
                        LOGGER.warning("Failed to load profile image from filesystem (image error): " + cleanPath);
                    }
                } else {
                    // Fall back to classpath
                    java.net.URL resource = getClass().getResource("/images/profiles/" + cleanPath);
                    if (resource != null) {
                        Image image = new Image(resource.toExternalForm(), true);
                        if (!image.isError()) {
                            LOGGER.info("Loaded profile image from classpath: " + resource.toExternalForm());
                            return image;
                        } else {
                            LOGGER.warning("Failed to load profile image from classpath (image error): " + cleanPath);
                        }
                    } else {
                        LOGGER.warning("Profile image not found: " + cleanPath);
                    }
                }
            } catch (Exception e) {
                LOGGER.severe("Failed to load profile image: " + path + " - " + e.getMessage());
            }
        } else {
            LOGGER.info("No profile image path provided, using default profile image");
        }
        // Default to hollow_circle2.png
        Image defaultImage = new Image(getClass().getResource("/images/profiles/hollow_circle2.png").toExternalForm());
        LOGGER.info("Loaded default profile image: hollow_circle2.png");
        return defaultImage;
    }

    public void updateUIVisibility() {
        boolean isLoggedIn = UserSession.getInstance().isLoggedIn();
        LOGGER.info("Updating UI: isLoggedIn=" + isLoggedIn + ", userId=" + UserSession.getInstance().getUserId());
        if (user_photo != null) {
            user_photo.setVisible(isLoggedIn);
            if (isLoggedIn) {
                String photoPath = UserSession.getInstance().getUserPhotoPath();
                if (photoPath == null || photoPath.isEmpty() || photoPath.equals("demo_profile.png") || photoPath.equals("/images/profiles/demo_profile.png")) {
                    photoPath = "hollow_circle2.png";
                    LOGGER.info("Using default photo: hollow_circle2.png");
                }
                Image image = loadProfileImage(photoPath);
                user_photo.setImage(image);
                LOGGER.info("User photo set to: " + photoPath);
            }
        } else {
            LOGGER.severe("user_photo is null in updateUIVisibility");
        }
        if (signOutButton != null) {
            signOutButton.setVisible(isLoggedIn);
            LOGGER.info("signOutButton visibility set to: " + isLoggedIn);
        } else {
            LOGGER.severe("signOutButton is null in updateUIVisibility");
        }
        if (user_sign_in != null) {
            user_sign_in.setVisible(!isLoggedIn);
            LOGGER.info("user_sign_in visibility set to: " + !isLoggedIn);
        }
        if (testSignOutButton != null) {
            testSignOutButton.setVisible(isLoggedIn);
            LOGGER.info("testSignOutButton visibility set to: " + isLoggedIn);
        }
    }

    private Image loadImage(String path) {
        try {
            if (path.startsWith("/")) {
                java.net.URL resource = getClass().getResource(path);
                if (resource != null) {
                    return new Image(resource.toExternalForm());
                } else {
                    System.err.println("Classpath resource not found: " + path);
                }
            }
            File file = new File(path);
            if (file.exists()) {
                return new Image(file.toURI().toString());
            } else {
                System.err.println("File not found: " + path);
            }
        } catch (Exception e) {
            System.err.println("Exception loading image: " + path + " - " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    @FXML
    void handle_user_sign_in(ActionEvent event) {
        System.out.println("Navigating to sign-in page.");
        navigateToPage(event, "sign_in.fxml", "Sign In");
    }

    @FXML
    void handle_user_sign_out(ActionEvent event) {
        System.out.println("Sign-out button clicked; executing handle_user_sign_out...");
        UserSession.getInstance().clearSession();
        System.out.println("After clearSession: isLoggedIn=" + UserSession.getInstance().isLoggedIn() + ", userId=" + UserSession.getInstance().getUserId());
        UserSession.getInstance().saveToFile();
        updateUIVisibility();
        showAlert(Alert.AlertType.INFORMATION, "Signed Out", "You have signed out. Now you are in guest mode.");
        System.out.println("Loading Home page...");
        loadFXML("Home.fxml");
        System.out.println("Sign-out completed; current page should be Home.fxml");
    }

    @FXML
    void action_home(ActionEvent event) {
        System.out.println("Home page is opening!!!");
        loadFXML("Home.fxml");
    }

    @FXML
    void action_games(ActionEvent event) {
        System.out.println("Games page is opening!!!");
        loadFXML("games.fxml");
    }

    @FXML
    void action_books(ActionEvent event) {
        System.out.println("Books page is opening!!!");
        loadFXML("reading_list.fxml");
    }

    @FXML

    void action_community(ActionEvent event) {
        System.out.println("Community page access attempted!!!");
        if (UserSession.getInstance().isLoggedIn()) {
            System.out.println("Community page is opening!!!");
            loadFXML("chat_area.fxml");
        } else {
            System.out.println("Guest user; login required to view community.");
            showAlert(Alert.AlertType.WARNING, "Login Required", "Please sign in to access the community section.");
        }
    }

    @FXML
    void action_contest(ActionEvent event) {
        System.out.println("Contest page is opening!!!");
        loadFXML("Contest.fxml");
    }

    @FXML
    void action_profile(ActionEvent event) {
        System.out.println("Profile page access attempted!!!");
        if (UserSession.getInstance().isLoggedIn()) {
            System.out.println("Profile page is opening!!!");
            loadFXML("profile.fxml");
        } else {
            System.out.println("Guest user; login required to view profile.");
            showAlert(Alert.AlertType.WARNING, "Login Required", "Please sign in to view your profile information.");
        }
    }

    @FXML
    void action_openParallelPage(ActionEvent event) {
        System.out.println("Opening Add Book page!!!");
        loadFXML("write.fxml");
    }

    public void loadFXML(String fxmlFile) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Parent newPage = loader.load();
            centerPane.getChildren().setAll(newPage);
            AnchorPane.setTopAnchor(newPage, 0.0);
            AnchorPane.setBottomAnchor(newPage, 0.0);
            AnchorPane.setLeftAnchor(newPage, 0.0);
            AnchorPane.setRightAnchor(newPage, 0.0);

            Object controller = loader.getController();
            if (controller instanceof reading_list__c) {
                System.out.println("Injecting main controller into reading_list__c");
                ((reading_list__c) controller).setMainController(this);
            } else if (controller instanceof write__c) {
                System.out.println("Injecting main controller into write__c");
                ((write__c) controller).setMainController(this);
            } else if (controller instanceof read_chapter__c) {
                System.out.println("Injecting main controller into read_chapter__c");
                ((read_chapter__c) controller).setMainController(this);
            } else if (controller instanceof read_book__c) {
                System.out.println("Injecting main controller into read_book__c");
                ((read_book__c) controller).setMainController(this);
            } else if (controller instanceof home__con) {
                System.out.println("Injecting main controller into home__con");
                ((home__con) controller).setMainController(this);
            } else if (controller instanceof about_us__c) {
                System.out.println("Injecting main controller into about_us__c");
                ((about_us__c) controller).setMainController(this);
            } else if (controller instanceof profile__c) {
                System.out.println("Injecting main controller into profile__c");
                ((profile__c) controller).setMainController(this);
            } else if (controller instanceof author_profile__c) {
                System.out.println("Injecting main controller into author_profile__c");
                ((author_profile__c) controller).setMainController(this);
            } else if (controller instanceof support_author__c) {
                System.out.println("Injecting main controller into support_author__c");
                ((support_author__c) controller).setMainController(this);
            }else if (controller instanceof contest__c) {
                System.out.println("Injecting main controller into contest__c");
                ((contest__c) controller).setMainController(this);
            }else if (controller instanceof contest_entries__c) {
                System.out.println("Injecting main controller into contest_entries__c");
                ((contest_entries__c) controller).setMainController(this);
            }else if (controller instanceof contest_read_entry__c) {
                System.out.println("Injecting main controller into contest_read_entry__c");
                ((contest_read_entry__c) controller).setMainController(this);
            }else if (controller instanceof contest_write__c) {
                System.out.println("Injecting main controller into contest_write__c");
                ((contest_write__c) controller).setMainController(this);
            }else if (controller instanceof c__c) {
                System.out.println("Injecting main controller into c__c");
                ((c__c) controller).setMainController(this);
            } else {
                System.out.println("No matching controller for " + fxmlFile);
            }
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Unable to load " + fxmlFile);
        }
    }

    @FXML
    private void handleNewBook(ActionEvent event) {
        AppState.getInstance().clearCurrentBookId();
        AppState.getInstance().setPreviousFXML("/com/example/scribble/reading_list.fxml");
        loadFXML("write.fxml");
        LOGGER.info("Navigated to write.fxml for new book creation");
    }

    private void navigateToPage(ActionEvent event, String fxmlFile, String pageName) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Unable to open " + pageName + " page.");
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public AnchorPane getCenterPane() {
        if (centerPane == null) {
            System.err.println("centerPane is null in getCenterPane");
        }
        return centerPane;
    }

    private boolean checkUserAuthentication() {
        return UserSession.getInstance().isLoggedIn();
    }
}