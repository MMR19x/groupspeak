package org.openjfx.model;

public class User {
    public String id;
    public String username;
    public String displayName;
    public boolean isOnline;

    public User(String id, String username, String displayName, boolean isOnline) {
        this.id = id;
        this.username = username;
        this.displayName = displayName;
        this.isOnline = isOnline;
    }
}
