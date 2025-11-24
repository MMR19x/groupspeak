package com.example.chat.server;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public final class SQLiteDatabase {
    private static final String DATABASE_URL = "jdbc:sqlite:encrypted_app.db";

    public SQLiteDatabase() {
        createTables();
    }

    public Connection connect() throws SQLException {
        return DriverManager.getConnection(DATABASE_URL);
    }

    private void createTables() {
        String userTable = "CREATE TABLE IF NOT EXISTS USERS ("
                + "    user_id TEXT PRIMARY KEY,"
                + "    username TEXT UNIQUE NOT NULL,"
                + "    email TEXT UNIQUE,"
                + "    password_hash TEXT NOT NULL,"
                + "    display_name TEXT NOT NULL,"
                + "    avatar_url TEXT,"
                + "    is_online INTEGER,"
                + "    last_seen TEXT,"
                + "    created_at TEXT"
                + ");";

        String sessionTable = "CREATE TABLE IF NOT EXISTS USER_SESSIONS ("
                + "    session_id TEXT PRIMARY KEY,"
                + "    user_id TEXT NOT NULL,"
                + "    session_token TEXT UNIQUE NOT NULL,"
                + "    device_info TEXT,"
                + "    created_at TEXT,"
                + "    expires_at TEXT,"
                + "    last_activity TEXT,"
                + "    FOREIGN KEY (user_id) REFERENCES USERS(user_id) ON DELETE CASCADE"
                + ");";

        String conversationTable = "CREATE TABLE IF NOT EXISTS CONVERSATIONS ("
                + "    conversation_id TEXT PRIMARY KEY,"
                + "    name TEXT,"
                + "    is_group INTEGER,"
                + "    created_at TEXT"
                + ");";

        String participantTable = "CREATE TABLE IF NOT EXISTS CONVERSATION_PARTICIPANTS ("
                + "    participant_id TEXT PRIMARY KEY,"
                + "    conversation_id TEXT NOT NULL,"
                + "    user_id TEXT NOT NULL,"
                + "    joined_at TEXT,"
                + "    FOREIGN KEY (conversation_id) REFERENCES CONVERSATIONS(conversation_id) ON DELETE CASCADE,"
                + "    FOREIGN KEY (user_id) REFERENCES USERS(user_id) ON DELETE CASCADE"
                + ");";

        String messageTable = "CREATE TABLE IF NOT EXISTS MESSAGES ("
                + "    message_id TEXT PRIMARY KEY,"
                + "    conversation_id TEXT NOT NULL,"
                + "    sender_id TEXT NOT NULL,"
                + "    content TEXT NOT NULL,"
                + "    created_at TEXT,"
                + "    FOREIGN KEY (conversation_id) REFERENCES CONVERSATIONS(conversation_id) ON DELETE CASCADE,"
                + "    FOREIGN KEY (sender_id) REFERENCES USERS(user_id) ON DELETE CASCADE"
                + ");";

        try (Connection conn = connect();
                Statement stmt = conn.createStatement()) {

            stmt.execute(userTable);
            stmt.execute(sessionTable);
            stmt.execute(conversationTable);
            stmt.execute(participantTable);
            stmt.execute(messageTable);

            System.out.println("SQLite: All database tables created/verified successfully.");

        } catch (SQLException e) {
            System.err.println("Error setting up database tables: " + e.getMessage());
        }
    }
}