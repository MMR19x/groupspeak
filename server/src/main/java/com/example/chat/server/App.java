package com.example.chat.server;

public class App {

    public static void main(String[] args) {

        SQLiteDatabase db = new SQLiteDatabase();

        User.initialize(db);
        UserSession.initialize(db);
        Conversation.initialize(db);
        ConversationParticipant.initialize(db);
        Message.initialize(db);

        AuthManager authManager = new AuthManager();

    }
}