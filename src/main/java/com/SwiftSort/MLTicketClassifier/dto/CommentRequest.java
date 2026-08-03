package com.SwiftSort.MLTicketClassifier.dto;

public class CommentRequest {
    private String text;
    private boolean internal;

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
    public boolean isInternal() { return internal; }
    public void setInternal(boolean internal) { this.internal = internal; }
}
