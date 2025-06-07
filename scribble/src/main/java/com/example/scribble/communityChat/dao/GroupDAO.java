package com.example.scribble.communityChat.dao;/*package dao;

import database.DatabaseConnector;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class GroupDAO {

    // Method to get all community groups from the database
    public List<String> getAllGroups() {
        List<String> groups = new ArrayList<>();
        String query = "SELECT group_name FROM community_groups";  // Assuming table name

        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                groups.add(rs.getString("group_name"));  // Fetch group name
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return groups;
    }
}*/
