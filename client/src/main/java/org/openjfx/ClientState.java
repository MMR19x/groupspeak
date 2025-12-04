package org.openjfx;

public class ClientState {
    private static ClientState instance;
    private Connection connection;

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
}
