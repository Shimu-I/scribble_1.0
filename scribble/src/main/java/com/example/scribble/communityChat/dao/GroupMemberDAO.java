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

/*package com.example.scribble.communityChat.dao;

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
} will start new update*/


package com.example.scribble.communityChat.dao;

import com.example.scribble.db_connect;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

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
            stmt.setTimestamp(3, now);          // joined_at
            stmt.setString(4, "online");        // default status
            stmt.setTimestamp(5, now);          // last_active

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

    // ✅ NEW: Fetch all group IDs a user belongs to (active groups only)
    public static List<Integer> getUserGroups(int userId) {
        List<Integer> groupIds = new ArrayList<>();
        String sql = "SELECT group_id FROM group_members WHERE user_id = ?";

        try (Connection conn = db_connect.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                groupIds.add(rs.getInt("group_id"));
            }
        } catch (SQLException e) {
            System.err.println("❌ Error fetching user groups: " + e.getMessage());
        }

        return groupIds;
    }


    public static boolean leaveGroup(int groupId, int userId) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = db_connect.getConnection();
            // Check if the user is the admin
            String checkAdminSql = "SELECT admin_id FROM community_groups WHERE group_id = ?";
            stmt = conn.prepareStatement(checkAdminSql);
            stmt.setInt(1, groupId);
            rs = stmt.executeQuery();

            if (rs.next()) {
                int adminId = rs.getInt("admin_id");
                if (adminId == userId) {
                    // Admin is leaving: delete group and related data
                    // Delete from chat_messages
                    String deleteMessagesSql = "DELETE FROM chat_messages WHERE group_id = ?";
                    stmt = conn.prepareStatement(deleteMessagesSql);
                    stmt.setInt(1, groupId);
                    stmt.executeUpdate();

                    // Delete from group_members
                    String deleteMembersSql = "DELETE FROM group_members WHERE group_id = ?";
                    stmt = conn.prepareStatement(deleteMembersSql);
                    stmt.setInt(1, groupId);
                    stmt.executeUpdate();

                    // Delete from community_groups
                    String deleteGroupSql = "DELETE FROM community_groups WHERE group_id = ?";
                    stmt = conn.prepareStatement(deleteGroupSql);
                    stmt.setInt(1, groupId);
                    int rowsDeleted = stmt.executeUpdate();

                    if (rowsDeleted > 0) {
                        System.out.println("✅ Group " + groupId + " deleted by admin " + userId);
                        return true;
                    }
                } else {
                    // Non-admin user: remove from group_members
                    String removeUserSql = "DELETE FROM group_members WHERE group_id = ? AND user_id = ?";
                    stmt = conn.prepareStatement(removeUserSql);
                    stmt.setInt(1, groupId);
                    stmt.setInt(2, userId);
                    int rowsDeleted = stmt.executeUpdate();

                    if (rowsDeleted > 0) {
                        System.out.println("✅ User " + userId + " left group " + groupId);
                        return true;
                    }
                }
            }
            return false; // Group not found
        } catch (SQLException e) {
            System.err.println("❌ Error removing user or deleting group: " + e.getMessage());
            return false;
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                System.err.println("❌ Error closing resources: " + e.getMessage());
            }
        }
    }

}





