package com.SwiftSort.MLTicketClassifier.dto;

import com.SwiftSort.MLTicketClassifier.model.User;

public class UserUpdateRequest {
    private String name;
    private User.Role role;
    private Boolean active;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public User.Role getRole() {
        return role;
    }

    public void setRole(User.Role role) {
        this.role = role;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }
}
