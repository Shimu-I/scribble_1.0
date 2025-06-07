package com.example.scribble.communityChat.dao;

import com.example.scribble.db_connect;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class UserGroupStatusDAO {

    // Add a user to a group and set their status as 'joined'
    public static void addUserToGroupStatus(int groupId, int userId, String status) {
        String sql = "INSERT INTO user_group_status (user_id, group_id, status) VALUES (?, ?, ?)";

        try (Connection conn = db_connect.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            stmt.setInt(2, groupId);
            stmt.setString(3, status);  // ENUM values 'joined' or 'left'

            stmt.executeUpdate();
            System.out.println("✅ User " + userId + " status updated to " + status + " for group " + groupId);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Update the user status to 'left' when they leave a group
    public static void updateUserStatus(int groupId, int userId, String status) {
        String sql = "UPDATE user_group_status SET status = ? WHERE group_id = ? AND user_id = ?";

        try (Connection conn = db_connect.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, status);  // ENUM values 'joined' or 'left'
            stmt.setInt(2, groupId);
            stmt.setInt(3, userId);

            stmt.executeUpdate();
            System.out.println("✅ User " + userId + " status updated to " + status + " for group " + groupId);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
