package com.example.scribble.communityChat.dao;

import com.example.scribble.db_connect;

import com.example.scribble.communityChat.models.ChatMessage;

import java.sql.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class ChatMessageDAO {

    // ✅ Insert a new message into chat_messages
    public static void addMessage(int groupId, int senderId, String message) {
        String sql = "INSERT INTO chat_messages (group_id, sender_id, message, created_at) VALUES (?, ?, ?, NOW())";
        try (Connection conn = db_connect.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, groupId);
            stmt.setInt(2, senderId);
            stmt.setString(3, message);
            stmt.executeUpdate();

            System.out.println("✅ Message sent successfully!");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static List<String> getFormattedMessagesForGroup(int groupId, int currentUserId) {
        List<String> messages = new ArrayList<>();
        String sql = "SELECT cm.message_id, cm.sender_id, u.username, cm.message, cm.created_at " +
                "FROM chat_messages cm " +
                "JOIN users u ON cm.sender_id = u.user_id " +
                "WHERE cm.group_id = ? ORDER BY cm.created_at ASC";

        try (Connection conn = db_connect.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, groupId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                int senderId = rs.getInt("sender_id");
                String sender = rs.getString("username");
                String message = rs.getString("message");
                Timestamp timestamp = rs.getTimestamp("created_at");

                String formattedTime = timestamp.toLocalDateTime().format(DateTimeFormatter.ofPattern("HH:mm"));
                String formattedMsg;

                if (senderId == currentUserId) {
                    formattedMsg = "[me] " + message + " (" + formattedTime + ")";
                } else {
                    formattedMsg = "[" + sender + "] " + message + " (" + formattedTime + ")";
                }

                messages.add(formattedMsg);
            }

        } catch (SQLException e) {
            System.err.println("❌ Error fetching chat history: " + e.getMessage());
        }

        return messages;
    }


    // ✅ Retrieve all messages from a specific group
    public static List<ChatMessage> getMessages(int groupId) {
        List<ChatMessage> messages = new ArrayList<>();
        String sql = "SELECT * FROM chat_messages WHERE group_id = ? ORDER BY created_at ASC";

        try (Connection conn = db_connect.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, groupId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                messages.add(new ChatMessage(
                        rs.getInt("message_id"),
                        rs.getInt("group_id"),
                        rs.getInt("sender_id"),
                        rs.getString("message"),
                        rs.getTimestamp("created_at")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return messages;
    }
}