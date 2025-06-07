/*package dao;

import database.DatabaseConnector;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;

public class GroupMemberDAO {

    // Add a user to a group when they start reading a book
    public static void addUserToGroup(int groupId, int userId) {
        String sql = "INSERT INTO group_members (group_id, user_id, joined_at) VALUES (?, ?, ?)";

        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, groupId);
            stmt.setInt(2, userId);
            stmt.setTimestamp(3, new Timestamp(System.currentTimeMillis())); // Current time

            int rowsInserted = stmt.executeUpdate();
            if (rowsInserted > 0) {
                System.out.println("✅ User " + userId + " added to group " + groupId);
            }
        } catch (SQLException e) {
            System.err.println("❌ Error adding user to group: " + e.getMessage());
        }
    }
}*/

package com.example.scribble.communityChat.dao;

import com.example.scribble.db_connect;

import java.sql.*;

public class GroupMemberDAO {

    // ✅ Add a user to a group when they start reading a book
    public static void addUserToGroup(int groupId, int userId) {
        String sql = "INSERT INTO group_members (group_id, user_id, joined_at, online_status, last_active) " +
                "VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = db_connect.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            Timestamp now = new Timestamp(System.currentTimeMillis());

            stmt.setInt(1, groupId);
            stmt.setInt(2, userId);
            stmt.setTimestamp(3, now);  // joined_at
            stmt.setString(4, "online");  // Default online status
            stmt.setTimestamp(5, now);  // last_active

            int rowsInserted = stmt.executeUpdate();
            if (rowsInserted > 0) {
                System.out.println("✅ User " + userId + " added to group " + groupId);
            }
        } catch (SQLException e) {
            System.err.println("❌ Error adding user to group: " + e.getMessage());
        }
    }

    // ✅ Update a user's online status (e.g., "online" or "offline")
    public static void setUserStatus(int groupId, int userId, String status) {
        String sql = "UPDATE group_members SET online_status = ? WHERE group_id = ? AND user_id = ?";

        try (Connection conn = db_connect.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, status);
            stmt.setInt(2, groupId);
            stmt.setInt(3, userId);

            int rowsUpdated = stmt.executeUpdate();
            if (rowsUpdated > 0) {
                System.out.println("✅ User " + userId + " is now " + status + " in group " + groupId);
            }
        } catch (SQLException e) {
            System.err.println("❌ Error updating user status: " + e.getMessage());
        }
    }

    // ✅ Update last active timestamp when a user sends a message
    public static void updateLastActive(int groupId, int userId) {
        String sql = "UPDATE group_members SET last_active = ? WHERE group_id = ? AND user_id = ?";

        try (Connection conn = db_connect.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
            stmt.setInt(2, groupId);
            stmt.setInt(3, userId);

            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("❌ Error updating last active time: " + e.getMessage());
        }
    }

    // ✅ Check if a user is already in the group
    public static boolean isUserInGroup(int groupId, int userId) {
        String sql = "SELECT COUNT(*) FROM group_members WHERE group_id = ? AND user_id = ?";

        try (Connection conn = db_connect.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, groupId);
            stmt.setInt(2, userId);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("❌ Error checking user membership: " + e.getMessage());
        }
        return false;
    }
}
