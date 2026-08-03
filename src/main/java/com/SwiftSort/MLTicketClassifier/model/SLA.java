package com.SwiftSort.MLTicketClassifier.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "sla_rules")
public class SLA {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Ticket.Priority priority;

    @Column(nullable = false)
    private Integer responseHours;

    @Column(nullable = false)
    private Integer resolutionHours;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Ticket.Priority getPriority() {
        return priority;
    }

    public void setPriority(Ticket.Priority priority) {
        this.priority = priority;
    }

    public Integer getResponseHours() {
        return responseHours;
    }

    public void setResponseHours(Integer responseHours) {
        this.responseHours = responseHours;
    }

    public Integer getResolutionHours() {
        return resolutionHours;
    }

    public void setResolutionHours(Integer resolutionHours) {
        this.resolutionHours = resolutionHours;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
