/*package ui;

import dao.ChatMessageDAO;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import models.ChatMessage;

import java.util.List;

public class ChatAreaController {
    @FXML private VBox chatMessagesContainer;
    @FXML private TextField messageInput;
    @FXML private Button sendButton;

    private int groupId; // Group ID for filtering messages
    private int userId;  // Current user's ID

    public void initialize(int groupId, int userId) {
        this.groupId = groupId;
        this.userId = userId;
        loadChatMessages();
    }

    private void loadChatMessages() {
        chatMessagesContainer.getChildren().clear();
        List<ChatMessage> messages = ChatMessageDAO.getMessages(groupId);

        for (ChatMessage message : messages) {
            displayMessage(message);
        }
    }

    private void displayMessage(ChatMessage message) {
        Text text = new Text(message.getMessage());
        TextFlow textFlow = new TextFlow(text);

        if (message.getSenderId() == userId) {
            textFlow.setStyle("-fx-background-color: lightblue; -fx-padding: 10px; -fx-background-radius: 10px;");
        } else {
            textFlow.setStyle("-fx-background-color: lightgray; -fx-padding: 10px; -fx-background-radius: 10px;");
        }

        chatMessagesContainer.getChildren().add(textFlow);
    }

    @FXML
    private void sendMessage() {
        String text = messageInput.getText().trim();
        if (!text.isEmpty()) {
            ChatMessageDAO.addMessage(groupId, userId, text);
            loadChatMessages(); // Reload chat to display new message
            messageInput.clear();
        }
    }
}
package ui;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.io.*;
import java.net.*;

public class ChatAreaController {
    @FXML private TextField groupIdField;
    @FXML private TextField userIdField;
    @FXML private ListView<String> chatListView;
    @FXML private TextField messageField;

    private PrintWriter out;
    private BufferedReader in;
    private Socket socket;

    public void joinGroup() {
        try {
            int groupId = Integer.parseInt(groupIdField.getText());
            int senderId = Integer.parseInt(userIdField.getText());

            socket = new Socket("localhost", 12345);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // Send groupId,senderId as the first message
            out.println(groupId + "," + senderId);

            // Start a thread to listen for messages
            new Thread(this::receiveMessages).start();

            showMessage("✅ Joined Group " + groupId + " as User " + senderId);

        } catch (Exception e) {
            showMessage("❌ Error joining group: " + e.getMessage());
        }
    }

    public void sendMessage() {
        if (out == null) {
            showMessage("⚠️ Please join a group first!");
            return;
        }

        String message = messageField.getText();
        if (!message.isEmpty()) {
            out.println(message);
            messageField.clear();
        }
    }

    private void receiveMessages() {
        try {
            String message;
            while ((message = in.readLine()) != null) {
                showMessage(message);
            }
        } catch (IOException e) {
            showMessage("❌ Disconnected from server.");
        }
    }

    private void showMessage(String message) {
        chatListView.getItems().add(message);
    }
}




 */


/*package com.example.scribble.communityChat.ui;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ChatAreaController {

    @FXML private TextField groupIdField;
    @FXML private TextField userIdField;
    @FXML private TextField messageField;
    @FXML private ListView<String> chatList;

    private PrintWriter out;
    private BufferedReader in;
    private int groupId;
    private int userId;

    public void connectToServer() {
        try {
            Socket socket = new Socket("localhost", 12345);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            groupId = Integer.parseInt(groupIdField.getText().trim());
            userId = Integer.parseInt(userIdField.getText().trim());

            out.println(groupId + "," + userId); // handshake to server

            // Start listening thread
            new Thread(() -> {
                try {
                    String message;
                    while ((message = in.readLine()) != null) {
                        String displayMsg = "New message: " + message;
                        Platform.runLater(() -> chatList.getItems().add(displayMsg));
                    }
                } catch (IOException e) {
                    Platform.runLater(() -> chatList.getItems().add("Disconnected from server."));
                }
            }).start();

            chatList.getItems().add("Connected to server as User ID " + userId);

        } catch (IOException e) {
            chatList.getItems().add("Error connecting: " + e.getMessage());
        }
    }

    public void sendMessage() {
        String text = messageField.getText().trim();
        if (!text.isEmpty() && out != null) {
            out.println(text); // Send to server

            // Create ChatMessage using the constructor with parameters
            //Timestamp timestamp = Timestamp.valueOf(LocalDateTime.now());
            //ChatMessage msg = new ChatMessage(0, groupId, userId, text, timestamp); // Use constructor with 5 parameters

            // Use the DAO's addMessage() method to save to the DB
            //ChatMessageDAO.addMessage(groupId, userId, text); // Save the message

            // Optionally, update your chat UI
            //chatList.getItems().add("Me: " + text);
            messageField.clear();
        }
    }
} */

package com.example.scribble.communityChat.ui;

import com.example.scribble.UserSession;
import com.example.scribble.communityChat.dao.BookGroupDAO;
import com.example.scribble.communityChat.dao.CommunityGroupDAO;
import com.example.scribble.communityChat.dao.ChatMessageDAO;
import com.example.scribble.communityChat.dao.GroupMemberDAO;
import com.example.scribble.communityChat.models.Book;
import com.example.scribble.communityChat.models.ChatMessage;
import com.example.scribble.communityChat.models.CommunityGroup;


import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.control.*;
import java.io.*;
import java.net.Socket;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import javafx.fxml.Initializable;
import javafx.scene.layout.HBox;

import java.net.URL;

/*public class ChatAreaController {
public class ChatAreaController implements Initializable {

    @FXML private TextField groupIdField;
    @FXML private TextField userIdField;
    @FXML private TextField messageField;
    @FXML private ListView<String> chatList;
    //@FXML private ListView<String> groupList;
    @FXML
    private ListView<CommunityGroup> groupList;

    @FXML private Button leaveGroupButton;
    @FXML private Label welcomeLabel;
    @FXML private ComboBox<String> groupSearchBox;
    @FXML private Label groupSearchMessage;

    private Map<String, Integer> groupNameToIdMap = new HashMap<>();


    private PrintWriter out;
    private BufferedReader in;
    private int groupId;
    private int userId;
    private Socket socket;

    public static class CommunityGroup {
        private int groupId;
        private String groupName;

        public CommunityGroup(int groupId, String groupName) {
            this.groupId = groupId;
            this.groupName = groupName;
        }

        public int getGroupId() {
            return groupId;
        }

        public String getGroupName() {
            return groupName;
        }

        @Override
        public String toString() {
            return groupId + " - " + groupName;
        }
    }


    @Override
    public void initialize(URL url, ResourceBundle rb) {
        userId = UserSession.getInstance().getUserId();
        userIdField.setText(String.valueOf(userId));
        userIdField.setDisable(true); // Prevent user from editing the field

        //welcomeLabel.setText("👋 Welcome, User " + userId + " to the Community");

        String username = UserSession.getInstance().getUsername();
        welcomeLabel.setText("👋 Welcome, " + username + " to the Community");


        loadUserGroups(userId);
        initializeGroupSelection();
        loadAllGroupNames();


        chatList.setCellFactory(listView -> new ChatMessageCell());
    }
    private void loadAllGroupNames() {
        groupNameToIdMap = CommunityGroupDAO.getAllGroupNamesWithIds();
        groupSearchBox.getItems().clear();
        groupSearchBox.getItems().addAll(groupNameToIdMap.keySet());
    }

    @FXML
    private void handleGroupSelection() {
        String selectedGroupName = groupSearchBox.getValue();
        if (selectedGroupName == null) return;

        int selectedGroupId = groupNameToIdMap.get(selectedGroupName);
        int currentUserId = UserSession.getInstance().getUserId();

        boolean alreadyInGroup = GroupMemberDAO.isUserInGroup(selectedGroupId, currentUserId);

        if (alreadyInGroup) {
            groupSearchMessage.setText("⚠️ You're already enrolled in this group.");
        } else {
            GroupMemberDAO.addUserToGroup(selectedGroupId, currentUserId);
            groupSearchMessage.setText("✅ You've been enrolled in the group: " + selectedGroupName);
            loadUserGroups(currentUserId); // refresh group list
        }
    }


    /*private void loadUserGroups(int userId) {
        groupList.getItems().clear();

        List<Integer> groupIds = GroupMemberDAO.getUserGroups(userId);
        if (groupIds.isEmpty()) {
            groupList.getItems().add("❌ No groups found for User ID: " + userId);
        } else {
            for (int gid : groupIds) {
                groupList.getItems().add("Group ID: " + gid);
            }
        }
    }*/

   /* private void loadUserGroups(int userId) {
        groupList.getItems().clear();

        List<CommunityGroup> userGroups = CommunityGroupDAO.getGroupsByUserId(userId);
        groupList.getItems().addAll(userGroups);
    }


    private void initializeGroupSelection() {
        groupList.setOnMouseClicked(event -> {
            String selectedItem = String.valueOf(groupList.getSelectionModel().getSelectedItem());
            if (selectedItem != null && selectedItem.startsWith("Group ID: ")) {
                String groupIdStr = selectedItem.replace("Group ID: ", "").trim();
                groupIdField.setText(groupIdStr);
            }
        });
    }



    public void connectToServer() {
        try {
            groupId = Integer.parseInt(groupIdField.getText().trim());
            //userId = Integer.parseInt(userIdField.getText().trim());
            userId = UserSession.getInstance().getUserId();
            String username = UserSession.getInstance().getUsername(); // Get the logged-in username

            socket = new Socket("localhost", 12345);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            out.println(groupId + "," + userId); // handshake to server

            // Start message listening in background
            /*new Thread(() -> {
                try {
                    String message;
                    while ((message = in.readLine()) != null) {
                        String displayMsg = "New message: " + message;
                        Platform.runLater(() -> chatList.getItems().add(displayMsg));
                    }
                } catch (IOException e) {
                    Platform.runLater(() -> chatList.getItems().add("Disconnected from server."));
                }
            }).start(); */

           /* new Thread(() -> {
                try {
                    String message;
                    while ((message = in.readLine()) != null) {
                        String finalMessage = message;
                        Platform.runLater(() -> {
                            String displayMsg;

                            // Detect if the message is sent by current user
                            if (finalMessage.startsWith(username + ":")) {
                                displayMsg = "me: " + finalMessage.substring(username.length() + 1).trim();
                            } else {
                                displayMsg = finalMessage;
                            }

                            chatList.getItems().add(displayMsg);
                        });
                    }
                } catch (IOException e) {
                    Platform.runLater(() -> chatList.getItems().add("❌ Disconnected from server."));
                }
            }).start();

            // ✅ Now update the chat list and group list immediately
            chatList.getItems().add("Connected to group " + groupId + " as User ID " + userId);

            // ✅ Call it here instead, right after connection
            showUserGroups();

        } catch (IOException | NumberFormatException e) {
            chatList.getItems().add("Error connecting: " + e.getMessage());
        }
    }


    public void showUserGroups() {
        try {
            //int userId = Integer.parseInt(userIdField.getText().trim());
            int userId = UserSession.getInstance().getUserId();

            // Clear existing groups before adding new ones
            groupList.getItems().clear();

            List<Integer> groups = GroupMemberDAO.getUserGroups(userId);
            if (groups.isEmpty()) {
                groupList.getItems().add("❌ No groups found for User ID: " + userId);
            } else {
                for (int gid : groups) {
                    groupList.getItems().add("Group ID: " + gid);
                }
            }

        } catch (NumberFormatException e) {
            chatList.getItems().add("❌ Invalid User ID");
        }
    }
    public void sendMessage() {
        String text = messageField.getText().trim();
        if (!text.isEmpty() && out != null) {
            out.println(text);
            messageField.clear();
        }
    }

    @FXML
    public void handleLeaveGroup() {
        if (groupId == 0 || userId == 0) {
            chatList.getItems().add("Group/User not initialized.");
            return;
        }

        boolean success = GroupMemberDAO.leaveGroup(groupId, userId);
        if (success) {
            chatList.getItems().add("You have left the group " + groupId);

            if (socket != null && !socket.isClosed()) {
                try {
                    socket.close();
                } catch (IOException e) {
                    chatList.getItems().add("Error closing socket: " + e.getMessage());
                }
            }

            groupId = 0;
            out = null;
            in = null;
            groupList.getItems().removeIf(item -> item.contains("Group " + groupId));
        } else {
            chatList.getItems().add("Error leaving the group.");
        }
    }
} */

public class ChatAreaController implements Initializable {

    @FXML private TextField groupIdField;
    @FXML private TextField userIdField;
    @FXML private TextField messageField;
    @FXML private ListView<String> chatList;
    @FXML private ListView<CommunityGroup> groupList;  // ✅ Correct ListView type
    @FXML private ComboBox<Book> bookGroupComboBox;
    @FXML private Button createGroupButton;
    @FXML private Label currently_joined_group;



    @FXML private Button leaveGroupButton;
    @FXML private Label welcomeLabel;
    @FXML private ComboBox<String> groupSearchBox;
    @FXML private Label groupSearchMessage;

    @FXML private TextField groupSearchTextField;
    private ContextMenu groupSuggestions; // Add this for dropdown suggestions

    private String currentGroupName = null; // Tracks the currently joined group name

    private Map<String, Integer> groupNameToIdMap = new HashMap<>();

    private PrintWriter out;
    private BufferedReader in;
    private int groupId;
    private int userId;
    private Socket socket;

    // ✅ Moved CommunityGroup to its own top-level class if needed, or keep here if inner class
    /*public static class CommunityGroup {
        private int groupId;
        private String groupName;

        public CommunityGroup(int groupId, String groupName) {
            this.groupId = groupId;
            this.groupName = groupName;
        }

        public int getGroupId() {
            return groupId;
        }

        public String getGroupName() {
            return groupName;
        }

        @Override
        public String toString() {
            return groupId + " - " + groupName;
        }
    }*/

    private void loadBooksWithoutGroup() {
        List<Book> books = BookGroupDAO.getBooksWithoutGroupByAuthor(userId);
        bookGroupComboBox.getItems().setAll(books);
    }


    @Override
    public void initialize(URL url, ResourceBundle rb) {
        userId = UserSession.getInstance().getUserId();
        userIdField.setText(String.valueOf(userId));
        userIdField.setDisable(true);

        String username = UserSession.getInstance().getUsername();
        welcomeLabel.setText("👋 Welcome, " + username + " to the Community");
        welcomeLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: white;");

        if (UserSession.getInstance().isAuthor()) {
            loadBooksWithoutGroup();
            createGroupButton.setVisible(true);
        } else {
            createGroupButton.setVisible(false);
        }

        loadUserGroups(userId);
        initializeGroupSelection();
        loadAllGroupNames();
        setupGroupSearchTextField();

        chatList.setCellFactory(listView -> new ChatMessageCell());
        updateGroupUI(); // Set initial state of label and button

// Add custom cell factory for groupList with selection styling
        groupList.setCellFactory(listView -> new ListCell<CommunityGroup>() {
            @Override
            protected void updateItem(CommunityGroup group, boolean empty) {
                super.updateItem(group, empty);
                if (empty || group == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(group.toString()); // e.g., "1 - Book Exchange"
                    // Reset style when not selected
                    if (!isSelected()) {
                        setStyle("-fx-text-fill: black;"); // Default text color
                    } else {
                        setStyle("-fx-background-color: #C8E6C9; -fx-text-fill: black;"); // Custom selection color
                    }
                }
            }
        });



    }


    private void setupGroupSearchTextField() {
        groupSuggestions = new ContextMenu();
        groupSearchTextField.textProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue == null) {
                groupSuggestions.hide();
                return;
            }

            groupSuggestions.getItems().clear();
            String searchText = newValue.trim().toLowerCase();
            List<String> matchingGroups;

            // Show all groups if search text is empty
            if (searchText.isEmpty()) {
                matchingGroups = groupNameToIdMap.keySet().stream()
                        .sorted()
                        .toList();
            } else {
                matchingGroups = groupNameToIdMap.keySet().stream()
                        .filter(name -> name.toLowerCase().startsWith(searchText))
                        .sorted()
                        .toList();
            }

            if (matchingGroups.isEmpty()) {
                groupSuggestions.hide();
            } else {
                for (String groupName : matchingGroups) {
                    MenuItem item = new MenuItem(groupName);
                    item.setOnAction(e -> {
                        groupSearchTextField.setText(groupName);
                        groupSuggestions.hide();
                        handleGroupSearchTextField(new ActionEvent(item, null));
                    });
                    groupSuggestions.getItems().add(item);
                }
                groupSuggestions.show(groupSearchTextField, Side.BOTTOM, 0, 0);
            }
        });

        groupSearchTextField.focusedProperty().addListener((obs, wasFocused, isFocused) -> {
            if (isFocused && groupSearchTextField.getText().trim().isEmpty()) {
                // Show all groups when the text field is focused and empty
                groupSuggestions.getItems().clear();
                List<String> allGroups = groupNameToIdMap.keySet().stream()
                        .sorted()
                        .toList();
                for (String groupName : allGroups) {
                    MenuItem item = new MenuItem(groupName);
                    item.setOnAction(e -> {
                        groupSearchTextField.setText(groupName);
                        groupSuggestions.hide();
                        handleGroupSearchTextField(new ActionEvent(item, null));
                    });
                    groupSuggestions.getItems().add(item);
                }
                groupSuggestions.show(groupSearchTextField, Side.BOTTOM, 0, 0);
            } else if (!isFocused) {
                Platform.runLater(() -> {
                    if (!groupSuggestions.isShowing()) {
                        groupSuggestions.hide();
                    }
                });
            }
        });
    }

    /// ////
    private void loadUserGroups(int userId) {
        //List<CommunityGroup> groups = CommunityGroupDAO.getGroupsForUser(userId);
        List<CommunityGroup> groups = CommunityGroupDAO.getGroupsByUserId(userId);
        groupList.getItems().setAll(groups);

        groupList.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, selectedGroup) -> {
            if (selectedGroup != null) {
                groupId = selectedGroup.getGroupId();
                System.out.println("Selected group ID: " + groupId);
                // Optional: clear chat area and load messages for this groupId
            }
        });
    }



    /*

        private void loadAllGroupNames() {
        groupNameToIdMap = CommunityGroupDAO.getAllGroupNamesWithIds();
        groupSearchBox.getItems().clear();
        groupSearchBox.getItems().addAll(groupNameToIdMap.keySet());
    }

     */

    private void loadAllGroupNames() {
        groupNameToIdMap = CommunityGroupDAO.getAllGroupNamesWithIds();
    }


    private void updateGroupUI() {
        if (currentGroupName == null || groupId == 0) {
            currently_joined_group.setText("You are not joined any group");
            leaveGroupButton.setDisable(true);
        } else {
            currently_joined_group.setText("Your Currently Joined in " + currentGroupName);
            // Disable leave button for "Book Exchange" group (name or ID 21)
            leaveGroupButton.setDisable(currentGroupName.equals("Book Exchange") || groupId == 21);
        }
    }

    @FXML
    private void handleGroupSelection() {
        String selectedGroupName = groupSearchBox.getValue();
        if (selectedGroupName == null) return;

        int selectedGroupId = groupNameToIdMap.get(selectedGroupName);
        int currentUserId = UserSession.getInstance().getUserId();

        boolean alreadyInGroup = GroupMemberDAO.isUserInGroup(selectedGroupId, currentUserId);

        if (alreadyInGroup) {
            groupSearchMessage.setText("⚠️ You're already enrolled in this group.");
        } else {
            GroupMemberDAO.addUserToGroup(selectedGroupId, currentUserId);
            groupSearchMessage.setText("✅ Welcome to the group: " + selectedGroupName);
            loadUserGroups(currentUserId);
            currentGroupName = selectedGroupName; // Set current group name
            groupId = selectedGroupId; // Update groupId
            updateGroupUI(); // Update label and button state
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void handleCreateGroup() {
        Book selectedBook = bookGroupComboBox.getValue();
        if (selectedBook == null) {
            showAlert("❗ Warning", "Please select a book to create a group.");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Group Creation");
        alert.setHeaderText(null);
        alert.setContentText("Do you want to create a group for book: " + selectedBook.getTitle() + "?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            // Create the group and get­­
            int newGroupId = BookGroupDAO.createGroup(selectedBook.getBookId(), userId, selectedBook.getTitle());
            if (newGroupId != -1) {
                // Add the creator to the group_members table
                GroupMemberDAO.addUserToGroup(newGroupId, userId);
                // Refresh group search data for other users
                loadAllGroupNames();
                // Refresh user's group list
                loadUserGroups(userId);
                // Update UI to reflect the new group
                currentGroupName = selectedBook.getTitle();
                groupId = newGroupId;
                updateGroupUI();
                // Refresh the book combo box
                loadBooksWithoutGroup();
                showAlert("✅ Success", "Group for book '" + selectedBook.getTitle() + "' created successfully.");
            } else {
                showAlert("❌ Error", "Failed to create the group.");
            }
        }
    }

    // ✅ NEW showUserGroups() (fixing the incorrect previous one)
    public void showUserGroups() {
        try {
            int userId = UserSession.getInstance().getUserId();
            groupList.getItems().clear();

            List<CommunityGroup> groups = CommunityGroupDAO.getGroupsByUserId(userId);
            if (groups.isEmpty()) {
                groupList.getItems().add(new CommunityGroup(-1, "❌ No groups found"));
            } else {
                groupList.getItems().addAll(groups);
            }

        } catch (Exception e) {
            chatList.getItems().add("❌ Failed to load groups: " + e.getMessage());
        }
    }

    /*private void loadUserGroups(int userId) {
        System.out.println("📥 Loading user groups for: " + userId);

        groupList.getItems().clear();

        List<CommunityGroup> userGroups = CommunityGroupDAO.getGroupsByUserId(userId);
        groupList.getItems().addAll(userGroups);
    }*/

    private void initializeGroupSelection() {
        groupList.setOnMouseClicked(event -> {
            CommunityGroup selected = groupList.getSelectionModel().getSelectedItem();
            if (selected != null) {
                groupIdField.setText(String.valueOf(selected.getGroupId()));
            }
        });
    }

    /*private void loadPreviousMessages(int groupId) {
        List<ChatMessage> messages = ChatMessageDAO.getMessages(groupId);

        for (ChatMessage msg : messages) {
            String prefix = msg.getSenderId() == UserSession.getInstance().getUserId()
                    ? "me: "
                    : "User " + msg.getSenderId() + ": ";

            String formattedMessage = prefix + msg.getMessage();
            chatList.getItems().add(formattedMessage);
        }
    }*/


    public void connectToServer() {
        try {
            groupId = Integer.parseInt(groupIdField.getText().trim());
            userId = UserSession.getInstance().getUserId();
            String username = UserSession.getInstance().getUsername();

            socket = new Socket("localhost", 12345);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            out.println(groupId + "," + userId);

            new Thread(() -> {
                try {
                    String message;
                    while ((message = in.readLine()) != null) {
                        String finalMessage = message;
                        Platform.runLater(() -> {
                            String[] parts = finalMessage.split(":", 2);
                            String displayMsg;

                            if (parts.length == 2) {
                                String sender = parts[0].trim();
                                String content = parts[1].trim();

                                if (sender.equals(username)) {
                                    displayMsg = "[me] " + content;
                                } else {
                                    displayMsg = "[" + sender + "] " + content;
                                }
                            } else {
                                displayMsg = finalMessage;
                            }

                            chatList.getItems().add(displayMsg);
                        });
                    }
                } catch (IOException e) {
                    Platform.runLater(() -> chatList.getItems().add("❌ Disconnected from server."));
                }
            }).start();

            chatList.getItems().add("Connected to group " + groupId + " as User ID " + userId);
            List<String> previousMessages = ChatMessageDAO.getFormattedMessagesForGroup(groupId, userId);
            for (String msg : previousMessages) {
                chatList.getItems().add(msg);
            }

            // Set currentGroupName based on groupId
            CommunityGroup selectedGroup = groupList.getItems().stream()
                    .filter(group -> group.getGroupId() == groupId)
                    .findFirst()
                    .orElse(null);
            if (selectedGroup != null) {
                currentGroupName = selectedGroup.getGroupName();
            }
            updateGroupUI(); // Update label and button state
            loadUserGroups(userId);

        } catch (IOException | NumberFormatException e) {
            chatList.getItems().add("Error connecting: " + e.getMessage());
        }
    }

    /*public void connectToServer() {
        try {
            groupId = Integer.parseInt(groupIdField.getText().trim());
            userId = UserSession.getInstance().getUserId();
            String username = UserSession.getInstance().getUsername();

            socket = new Socket("localhost", 12345);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // Send group and user info to server
            out.println(groupId + "," + userId);

            // Start background thread to receive messages
            new Thread(() -> {
                try {
                    String message;
                    while ((message = in.readLine()) != null) {
                        String finalMessage = message;
                        Platform.runLater(() -> {
                            String[] parts = finalMessage.split(":", 2);

                            if (parts.length == 2) {
                                String sender = parts[0].trim();
                                String content = parts[1].trim();

                                Label messageLabel = new Label(content);
                                messageLabel.setWrapText(true);
                                messageLabel.setMaxWidth(300); // Optional: wrap long messages

                                HBox messageBox = new HBox(messageLabel);
                                messageBox.setStyle("-fx-padding: 5 10 5 10;");


                                if (sender.equals(username)) {
                                    // Message from me
                                    messageBox.setAlignment(Pos.CENTER_RIGHT);
                                    messageLabel.setStyle("-fx-background-color: lightgreen; -fx-padding: 8; -fx-background-radius: 10;");
                                } else {
                                    // Message from others
                                    messageBox.setAlignment(Pos.CENTER_LEFT);
                                    messageLabel.setStyle("-fx-background-color: lightgray; -fx-padding: 8; -fx-background-radius: 10;");
                                }

                                chatList.getItems().add(messageBox);
                            } else {
                                // Fallback if server sends raw system message
                                Label rawLabel = new Label(finalMessage);
                                HBox rawBox = new HBox(rawLabel);
                                rawBox.setAlignment(Pos.CENTER);
                                chatList.getItems().add(rawBox);
                            }
                        });
                    }
                } catch (IOException e) {
                    Platform.runLater(() -> {
                        Label errorLabel = new Label("❌ Disconnected from server.");
                        errorLabel.setStyle("-fx-text-fill: red;");
                        HBox errorBox = new HBox(errorLabel);
                        errorBox.setAlignment(Pos.CENTER);
                        chatList.getItems().add(errorBox);
                    });
                }
            }).start();

            // Show initial system message: connection success
            Label connectLabel = new Label("✅ Connected to group " + groupId + " as User ID " + userId);
            connectLabel.setStyle("-fx-text-fill: green;");
            HBox connectBox = new HBox(connectLabel);
            connectBox.setAlignment(Pos.CENTER);
            chatList.getItems().add(connectBox);

            loadUserGroups(userId);

        } catch (IOException | NumberFormatException e) {
            Label errorLabel = new Label("Error connecting: " + e.getMessage());
            errorLabel.setStyle("-fx-text-fill: red;");
            HBox errorBox = new HBox(errorLabel);
            errorBox.setAlignment(Pos.CENTER);
            chatList.getItems().add(errorBox);
        }
    }*/


    /*public void sendMessage() {
        String text = messageField.getText().trim();
        if (!text.isEmpty() && out != null) {
            out.println(text);
            messageField.clear();
        }
    }*/

    /*public void sendMessage() {
        String text = messageField.getText().trim();
        if (!text.isEmpty() && out != null) {
            String username = UserSession.getInstance().getUsername();
            out.println(username + ": " + text);  // Prefix the message with the username
            messageField.clear();
        }
    }*/
    @FXML
    private void sendMessage() {
        String message = messageField.getText().trim();

        if (!message.isEmpty() && out != null) {
            String username = UserSession.getInstance().getUsername();
            String time = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));

            // Format: username: message (HH:mm)
            String formattedMessage = username + ":" + message + " (" + time + ")";
            out.println(formattedMessage);  // Send to server

            messageField.clear();
        }
    }




    /*
    @FXML
    public void handleLeaveGroup() {
        if (groupId == 0 || userId == 0) {
            Label label = new Label("Group/User not initialized.");
            HBox hbox = new HBox(label);
            hbox.setAlignment(Pos.CENTER);
            chatList.getItems().add(hbox);
            return;
        }

        boolean success = GroupMemberDAO.leaveGroup(groupId, userId);
        if (success) {
            Label label = new Label("You have left the group " + groupId);
            HBox hbox = new HBox(label);
            hbox.setAlignment(Pos.CENTER);
            chatList.getItems().add(hbox);

            if (socket != null && !socket.isClosed()) {
                try {
                    socket.close();
                } catch (IOException e) {
                    Label errLabel = new Label("Error closing socket: " + e.getMessage());
                    HBox errBox = new HBox(errLabel);
                    errBox.setAlignment(Pos.CENTER);
                    chatList.getItems().add(errBox);
                }
            }

            out = null;
            in = null;
            groupId = 0;

            // Remove from CommunityGroup ListView by ID
            groupList.getItems().removeIf(item -> item.getGroupId() == groupId);

        } else {
            Label label = new Label("Error leaving the group.");
            HBox hbox = new HBox(label);
            hbox.setAlignment(Pos.CENTER);
            chatList.getItems().add(hbox);
        }
    }

    public void handleLeaveGroup() {
        if (groupId == 0 || userId == 0) {
            chatList.getItems().add("Group/User not initialized.");
            return;
        }

        boolean success = GroupMemberDAO.leaveGroup(groupId, userId);
        if (success) {
            chatList.getItems().add("You have left the group " + groupId);

            if (socket != null && !socket.isClosed()) {
                try {
                    socket.close();
                } catch (IOException e) {
                    chatList.getItems().add("Error closing socket: " + e.getMessage());
                }
            }

            out = null;
            in = null;
            groupId = 0;

            // ✅ remove from CommunityGroup ListView by ID
            groupList.getItems().removeIf(item -> item.getGroupId() == groupId);

        } else {
            chatList.getItems().add("Error leaving the group.");
        }
    }*/

    @FXML
    public void handleLeaveGroup() {
        if (groupId == 0 || userId == 0) {
            chatList.getItems().add("Group/User not initialized.");
            return;
        }

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Leave");
        confirmAlert.setHeaderText(null);
        // Check if user is admin
        boolean isAdmin = CommunityGroupDAO.isUserAdmin(groupId, userId);
        if (isAdmin) {
            confirmAlert.setContentText("You are the admin of this group. Leaving will delete the group and all its messages. Are you sure?");
        } else {
            confirmAlert.setContentText("Are you sure you want to leave this group?");
        }
        Optional<ButtonType> result = confirmAlert.showAndWait();

        if (result.isEmpty() || result.get() != ButtonType.OK) {
            return;
        }

        boolean success = GroupMemberDAO.leaveGroup(groupId, userId);
        if (success) {
            if (isAdmin) {
                chatList.getItems().add("You are deleting your own group"); // Exact line to add
                // Refresh group search data for other users
                loadAllGroupNames();
            } else {
                chatList.getItems().add("You have left the group " + groupId);
            }

            if (socket != null && !socket.isClosed()) {
                try {
                    socket.close();
                } catch (IOException e) {
                    chatList.getItems().add("Error closing socket: " + e.getMessage());
                }
            }

            out = null;
            in = null;
            groupList.getItems().removeIf(item -> item.getGroupId() == groupId);
            currentGroupName = null; // Clear current group name
            groupId = 0;
            updateGroupUI(); // Update label and button state
            loadUserGroups(userId); // Refresh group list
        } else {
            chatList.getItems().add("Error leaving the group.");
        }
    }

    @FXML
    public void handleGroupSearchTextField(ActionEvent actionEvent) {
        String groupName = groupSearchTextField.getText().trim();
        if (groupName.isEmpty()) {
            groupSearchMessage.setText("⚠️ Please enter a group name.");
            return;
        }

        Integer selectedGroupId = groupNameToIdMap.get(groupName);
        if (selectedGroupId == null) {
            groupSearchMessage.setText("⚠️ Group '" + groupName + "' not found.");
            return;
        }

        int currentUserId = UserSession.getInstance().getUserId();
        boolean alreadyInGroup = GroupMemberDAO.isUserInGroup(selectedGroupId, currentUserId);

        if (alreadyInGroup) {
            groupSearchMessage.setText("⚠️ You're already enrolled in this group.");
        } else {
            GroupMemberDAO.addUserToGroup(selectedGroupId, currentUserId);
            groupSearchMessage.setText("✅ Welcome to the group: " + groupName);
            loadUserGroups(currentUserId);
            currentGroupName = groupName;
            groupId = selectedGroupId;
            updateGroupUI();
        }
        groupSearchTextField.clear();
        groupSuggestions.hide();
    }


}






