package com.example.scribble.communityChat.dao;

import com.example.scribble.db_connect;

import com.example.scribble.communityChat.models.ChatMessage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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