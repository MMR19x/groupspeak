package org.openjfx;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class ClientState {
    private static ClientState instance;
    private Connection connection;

    private String sessionToken;
    private String currentUserId;
    private String currentUsername;
    private String currentDisplayName;
    private String currentEmail;

    private boolean asyncMode = false;
    private final BlockingQueue<String> responseQueue = new LinkedBlockingQueue<>();

    private ClientState() {}

    public static synchronized ClientState getInstance() {
        if (instance == null) {
            instance = new ClientState();
        }
        return instance;
    }

    public Connection getConnection() {
        return connection;
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    public String getSessionToken() { return sessionToken; }
    public void setSessionToken(String sessionToken) { this.sessionToken = sessionToken; }

    public String getCurrentUserId() { return currentUserId; }
    public void setCurrentUserId(String currentUserId) { this.currentUserId = currentUserId; }

    public String getCurrentUsername() { return currentUsername; }
    public void setCurrentUsername(String currentUsername) { this.currentUsername = currentUsername; }

    public String getCurrentDisplayName() { return currentDisplayName; }
    public void setCurrentDisplayName(String currentDisplayName) { this.currentDisplayName = currentDisplayName; }

    public String getCurrentEmail() { return currentEmail; }
    public void setCurrentEmail(String currentEmail) { this.currentEmail = currentEmail; }

    public boolean isAsyncMode() { return asyncMode; }
    public void setAsyncMode(boolean asyncMode) { this.asyncMode = asyncMode; }

    public BlockingQueue<String> getResponseQueue() { return responseQueue; }
}
