package com.example.chat.server;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.UUID;

public class Message {
    private String messageId;
    private String conversationId;
    private String senderId;
    private String content; // Stores the encrypted ciphertext
    private String createdAt;

    private static SQLiteDatabase db;

    public Message(String conversationId, String senderId, String content) {
        this.messageId = UUID.randomUUID().toString();
        this.conversationId = conversationId;
        this.senderId = senderId;
        this.content = content;
        this.createdAt = LocalDateTime.now().toString();
    }

    public static void initialize(SQLiteDatabase database) {
        db = database;
    }

    public static java.util.List<Message> findByConversationId(String conversationId) throws SQLException {
        if (db == null) return new java.util.ArrayList<>();
        
        String sql = "SELECT * FROM MESSAGES WHERE conversation_id = ? ORDER BY created_at ASC";
        java.util.List<Message> messages = new java.util.ArrayList<>();

        try (Connection conn = db.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, conversationId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Message m = new Message(
                    rs.getString("conversation_id"),
                    rs.getString("sender_id"),
                    rs.getString("content")
                );
                // We need to set the ID and timestamp from DB, but constructor generates new ones.
                // Let's use a private constructor or reflection, or just set fields if not final.
                // Since fields are private and no setters, let's add a constructor for DB retrieval.
                m.messageId = rs.getString("message_id");
                m.createdAt = rs.getString("created_at");
                messages.add(m);
            }
        } catch (SQLException e) {
            System.err.println("DB Error finding messages: " + e.getMessage());
            throw e;
        }
        return messages;
    }

    public void save() throws SQLException {
        if (db == null)
            throw new IllegalStateException("Database connection not initialized.");

        String sql = "INSERT INTO MESSAGES(message_id, conversation_id, sender_id, content, created_at) "
                + "VALUES(?, ?, ?, ?, ?)";

        try (Connection conn = db.connect();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, this.messageId);
            pstmt.setString(2, this.conversationId);
            pstmt.setString(3, this.senderId);
            pstmt.setString(4, this.content);
            pstmt.setString(5, this.createdAt);

            pstmt.executeUpdate();
            System.out.println("DB: Saved new message in conversation " + this.conversationId);

        } catch (SQLException e) {
            System.err.println("DB Error saving message: " + e.getMessage());
            throw e;
        }
    }

    public String getMessageId() {
        return messageId;
    }

    public String getConversationId() {
        return conversationId;
    }

    public String getSenderId() {
        return senderId;
    }

    public String getContent() {
        return content;
    }

    public String getCreatedAt() {
        return createdAt;
    }
}