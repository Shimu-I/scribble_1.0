package com.example.scribble.communityChat.ui;/*import dao.ChatMessageDAO;

public class main {
    public static void main(String[] args) {
        int groupId = 12;  // Replace with an actual group_id
        int senderId = 12; // Replace with an actual user_id

        // ✅ Test: Send a message
        ChatMessageDAO.addMessage(groupId, senderId, "Hello everyone in this group!");

        // ✅ Test: Retrieve messages
        System.out.println("\n📩 Chat History:");
        ChatMessageDAO.getMessages(groupId).forEach(System.out::println);
    }
} */

/*import dao.UserGroupStatusDAO;

public class main {
    public static void main(String[] args) {
        int groupId = 2;  // Replace with an actual group_id
        int userId = 1;   // Replace with an actual user_id

        // Add the user to the group and set their status to 'joined'
        UserGroupStatusDAO.addUserToGroupStatus(groupId, userId, "joined");

        // Simulate the user leaving the group after some time
        UserGroupStatusDAO.updateUserStatus(groupId, userId, "left");
    }
}*/


//ADDING MEMEBERS TO THE GROUP WITH THE FOLLOWING CODE


import com.example.scribble.communityChat.dao.GroupMemberDAO;

public class main {
    public static void main(String[] args) {
        int groupId = 2;
        int userId = 1;

        // Add user if not already in the group
        if (!GroupMemberDAO.isUserInGroup(groupId, userId)) {
            GroupMemberDAO.addUserToGroup(groupId, userId);
        }

        // Update user status to offline
        GroupMemberDAO.setUserStatus(groupId, userId, "offline");

        // Update last active timestamp
        GroupMemberDAO.updateLastActive(groupId, userId);
    }
}






