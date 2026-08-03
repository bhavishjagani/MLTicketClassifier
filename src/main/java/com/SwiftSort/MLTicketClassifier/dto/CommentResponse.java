package com.SwiftSort.MLTicketClassifier.dto;

import java.time.LocalDateTime;

public class CommentResponse {
    private String text;
    private String author;
    private boolean internal;
    private LocalDateTime createdAt;

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }
    public boolean isInternal() { return internal; }
    public void setInternal(boolean internal) { this.internal = internal; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
