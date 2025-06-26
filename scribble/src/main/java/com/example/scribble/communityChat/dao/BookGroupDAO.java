package com.example.scribble.communityChat.dao;

import com.example.scribble.communityChat.models.Book;
import com.example.scribble.db_connect;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BookGroupDAO {

    // ✅ Fetch books authored by the user that don't have a group yet
    public static List<Book> getBooksWithoutGroupByAuthor(int userId) {
        List<Book> books = new ArrayList<>();
        String sql = """
                SELECT b.book_id, b.title
                FROM books b
                JOIN book_authors ba ON b.book_id = ba.book_id
                WHERE ba.user_id = ?
                AND b.book_id NOT IN (
                    SELECT book_id FROM community_groups WHERE book_id IS NOT NULL
                )
                ORDER BY b.title ASC
                """;

        try (Connection conn = db_connect.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                books.add(new Book(rs.getInt("book_id"), rs.getString("title")));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return books;
    }

    // ✅ Create a new group
    public static int createGroup(int bookId, int userId, String groupName) {
        String sql = "INSERT INTO community_groups (group_name, admin_id, book_id) VALUES (?, ?, ?)";
        try (Connection conn = db_connect.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, groupName);
            stmt.setInt(2, userId);
            stmt.setInt(3, bookId);
            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                ResultSet generatedKeys = stmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1); // Return the new group_id
                }
            }
            return -1; // Indicate failure
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }
}
