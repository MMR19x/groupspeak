package org.openjfx.model;

import java.time.LocalDateTime;

public class Message {
    public String id;
    public String senderId;
    public String content;
    public String createdAt;

    public Message(String id, String senderId, String content, String createdAt) {
        this.id = id;
        this.senderId = senderId;
        this.content = content;
        this.createdAt = createdAt;
    }

    public Message(String senderId, String content) {
        this.id = java.util.UUID.randomUUID().toString();
        this.senderId = senderId;
        this.content = content;
        this.createdAt = LocalDateTime.now().toString();
    }
}
