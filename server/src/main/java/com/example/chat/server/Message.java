package com.example.chat.server;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;
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
}