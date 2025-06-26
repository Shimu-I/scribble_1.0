package com.example.scribble.communityChat.dao;

import com.example.scribble.communityChat.models.CommunityGroup;
import com.example.scribble.communityChat.ui.ChatAreaController;
import com.example.scribble.db_connect;

import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class CommunityGroupDAO {

    // Method to create a new community group
    public static void createCommunityGroup(int adminId) {
        String sql = "INSERT INTO community_groups (admin_id, created_at) VALUES (?, ?)";

        try (Connection conn = db_connect.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, adminId);
            stmt.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
            stmt.executeUpdate();

            System.out.println("✅ Community group created successfully!");
        } catch (SQLException e) {
            System.err.println("❌ Error creating community group: " + e.getMessage());
        }
    }

    public static Map<String, Integer> getAllGroupNamesWithIds() {
        Map<String, Integer> groups = new LinkedHashMap<>();
        String sql = "SELECT group_id, group_name FROM community_groups ORDER BY group_name ASC";

        try (Connection conn = db_connect.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                groups.put(rs.getString("group_name"), rs.getInt("group_id"));
            }

        } catch (SQLException e) {
            System.err.println("❌ Error fetching group names: " + e.getMessage());
        }

        return groups;
    }

    public static List<CommunityGroup> getGroupsByUserId(int userId) {
        List<CommunityGroup> groups = new ArrayList<>();
        String query = "SELECT cg.group_id, cg.group_name FROM community_groups cg " +
                "JOIN group_members gm ON cg.group_id = gm.group_id " +
                "WHERE gm.user_id = ?";

        try (Connection conn = db_connect.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                int groupId = rs.getInt("group_id");
                String groupName = rs.getString("group_name");
                //groups.add(new ChatAreaController.CommunityGroup(groupId, groupName));
                groups.add(new CommunityGroup(groupId, groupName));

            }

        } catch (SQLException e) {
            System.err.println("❌ Error loading user groups: " + e.getMessage());
        }

        return groups;
    }


    public static boolean isUserAdmin(int groupId, int userId) {
        String sql = "SELECT admin_id FROM community_groups WHERE group_id = ?";
        try (Connection conn = db_connect.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, groupId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("admin_id") == userId;
            }
            return false;
        } catch (SQLException e) {
            System.err.println("❌ Error checking admin status: " + e.getMessage());
            return false;
        }
    }
}
