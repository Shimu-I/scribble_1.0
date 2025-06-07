package com.example.scribble.communityChat.dao;

import com.example.scribble.db_connect;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;

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
}
