package com.example.scribble.communityChat.models;

import java.sql.Timestamp;

public class ChatMessage {
    private int messageId;
    private int groupId;
    private int senderId;
    private String message;
    private Timestamp createdAt;

    public ChatMessage(int messageId, int groupId, int senderId, String message, Timestamp createdAt) {
        this.messageId = messageId;
        this.groupId = groupId;
        this.senderId = senderId;
        this.message = message;
        this.createdAt = createdAt;
    }

    public int getMessageId() { return messageId; }
    public int getGroupId() { return groupId; }
    public int getSenderId() { return senderId; }
    public String getMessage() { return message; }
    public Timestamp getCreatedAt() { return createdAt; }

    // ✅ Setters
    public void setMessageId(int messageId) { this.messageId = messageId; }
    public void setGroupId(int groupId) { this.groupId = groupId; }
    public void setSenderId(int senderId) { this.senderId = senderId; }
    public void setMessage(String message) { this.message = message; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return "[" + createdAt + "] User " + senderId + ": " + message;
    }
}
