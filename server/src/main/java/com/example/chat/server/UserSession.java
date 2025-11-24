package com.example.chat.server;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.UUID;

public class UserSession {
    private String sessionId;
    private String userId;
    private String sessionToken;
    private String deviceInfo;
    private String createdAt;
    private String expiresAt;
    private String lastActivity;

    private static SQLiteDatabase db;

    public UserSession(String userId, String sessionToken) {
        this.sessionId = UUID.randomUUID().toString();
        this.userId = userId;
        this.sessionToken = sessionToken;
        this.createdAt = LocalDateTime.now().toString();
        this.expiresAt = LocalDateTime.now().plusDays(1).toString();
    }

    public static void initialize(SQLiteDatabase database) {
        db = database;
    }

    public void save() {
        if (db == null) {
            System.err.println("DB Error: Database connection not initialized for UserSession.");
            return;
        }
        String sql = "INSERT INTO USER_SESSIONS(session_id, user_id, session_token, created_at, expires_at) "
                + "VALUES(?, ?, ?, ?, ?)";

        try (Connection conn = db.connect();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, this.sessionId);
            pstmt.setString(2, this.userId);
            pstmt.setString(3, this.sessionToken);
            pstmt.setString(4, this.createdAt);
            pstmt.setString(5, this.expiresAt);

            pstmt.executeUpdate();
            System.out.println("DB: Saved new session for user ID: " + this.userId);
        } catch (SQLException e) {
            System.err.println("DB Error saving session: " + e.getMessage());
        }
    }

}