package com.SwiftSort.MLTicketClassifier.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tickets")
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String subject;

    @Column(columnDefinition = "TEXT")
    private String message;

    @Column(columnDefinition = "TEXT")
    private String maskedMessage;

    private String customerReference;
    private String product;

    @Enumerated(EnumType.STRING)
    private Source source = Source.MANUAL;

    @Enumerated(EnumType.STRING)
    private Category category;

    @Enumerated(EnumType.STRING)
    private Priority priority = Priority.MEDIUM;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.NEW;

    private String assignedTeam;
    private String recommendedTeam;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_agent_id")
    private User assignedAgent;

    @Enumerated(EnumType.STRING)
    private Sentiment sentiment;

    @Enumerated(EnumType.STRING)
    private Urgency urgency;

    @Column(columnDefinition = "TEXT")
    private String summary;

    private Double confidence;

    @Column(columnDefinition = "TEXT")
    private String suggestedResponse;

    @Enumerated(EnumType.STRING)
    private ResponseTone responseTone;

    private Boolean responseApproved;
    private Boolean responseSent;

    @Column(columnDefinition = "TEXT")
    private String resolutionNotes;

    @ElementCollection
    @CollectionTable(name = "ticket_tags", joinColumns = @JoinColumn(name = "ticket_id"))
    @Column(name = "tag")
    private List<String> tags = new ArrayList<>();

    private String aiModelName;
    private String aiPromptVersion;

    @Column(columnDefinition = "TEXT")
    private String embedding;

    private LocalDateTime slaDeadline;
    private LocalDateTime firstResponseAt;
    private LocalDateTime resolvedAt;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime updatedAt = LocalDateTime.now();

    @ManyToOne
    @JoinColumn(name = "duplicate_of_id")
    private Ticket duplicateOf;

    @PreUpdate
    public void onUpdate() { this.updatedAt = LocalDateTime.now(); }

    public enum Source {
        MOBILE_APP, WEBSITE, EMAIL, CONTACT_FORM, CHAT,
        SOCIAL_MEDIA, APP_REVIEW, MANUAL, API
    }

    public enum Category {
        TECHNICAL_BUG, LOGIN_ISSUE, ACCOUNT_ISSUE, PAYMENT_ISSUE,
        SUBSCRIPTION_ISSUE, TUTOR_BOOKING_ISSUE, CLASS_SCHEDULING_ISSUE,
        COURSE_RELATED_QUESTION, TUTOR_FEEDBACK, FEATURE_REQUEST, COMPLAINT,
        REFUND_REQUEST, GENERAL_ENQUIRY, SECURITY_ISSUE, OTHER
    }

    public enum Priority { LOW, MEDIUM, HIGH, CRITICAL }

    public enum Status {
        NEW, OPEN, IN_PROGRESS, WAITING_FOR_CUSTOMER,
        WAITING_FOR_INTERNAL_TEAM, RESOLVED, CLOSED,
        REOPENED, DUPLICATE, REJECTED
    }

    public enum Sentiment {
        POSITIVE, NEUTRAL, NEGATIVE, ANGRY, FRUSTRATED, CONFUSED, URGENT
    }

    public enum Urgency { LOW, MEDIUM, HIGH, CRITICAL }

    public enum ResponseTone {
        PROFESSIONAL, FRIENDLY, APOLOGETIC, SIMPLE, CONCISE, REASSURING, FORMAL
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getMaskedMessage() { return maskedMessage; }
    public void setMaskedMessage(String maskedMessage) { this.maskedMessage = maskedMessage; }
    public String getCustomerReference() { return customerReference; }
    public void setCustomerReference(String customerReference) { this.customerReference = customerReference; }
    public String getProduct() { return product; }
    public void setProduct(String product) { this.product = product; }
    public Source getSource() { return source; }
    public void setSource(Source source) { this.source = source; }
    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }
    public Priority getPriority() { return priority; }
    public void setPriority(Priority priority) { this.priority = priority; }
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
    public String getAssignedTeam() { return assignedTeam; }
    public void setAssignedTeam(String assignedTeam) { this.assignedTeam = assignedTeam; }
    public String getRecommendedTeam() { return recommendedTeam; }
    public void setRecommendedTeam(String recommendedTeam) { this.recommendedTeam = recommendedTeam; }
    public User getAssignedAgent() { return assignedAgent; }
    public void setAssignedAgent(User assignedAgent) { this.assignedAgent = assignedAgent; }
    public Sentiment getSentiment() { return sentiment; }
    public void setSentiment(Sentiment sentiment) { this.sentiment = sentiment; }
    public Urgency getUrgency() { return urgency; }
    public void setUrgency(Urgency urgency) { this.urgency = urgency; }
    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }
    public Double getConfidence() { return confidence; }
    public void setConfidence(Double confidence) { this.confidence = confidence; }
    public String getSuggestedResponse() { return suggestedResponse; }
    public void setSuggestedResponse(String suggestedResponse) { this.suggestedResponse = suggestedResponse; }
    public ResponseTone getResponseTone() { return responseTone; }
    public void setResponseTone(ResponseTone responseTone) { this.responseTone = responseTone; }
    public Boolean getResponseApproved() { return responseApproved; }
    public void setResponseApproved(Boolean responseApproved) { this.responseApproved = responseApproved; }
    public Boolean getResponseSent() { return responseSent; }
    public void setResponseSent(Boolean responseSent) { this.responseSent = responseSent; }
    public String getResolutionNotes() { return resolutionNotes; }
    public void setResolutionNotes(String resolutionNotes) { this.resolutionNotes = resolutionNotes; }
    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }
    public String getAiModelName() { return aiModelName; }
    public void setAiModelName(String aiModelName) { this.aiModelName = aiModelName; }
    public String getAiPromptVersion() { return aiPromptVersion; }
    public void setAiPromptVersion(String aiPromptVersion) { this.aiPromptVersion = aiPromptVersion; }
    public String getEmbedding() { return embedding; }
    public void setEmbedding(String embedding) { this.embedding = embedding; }
    public LocalDateTime getSlaDeadline() { return slaDeadline; }
    public void setSlaDeadline(LocalDateTime slaDeadline) { this.slaDeadline = slaDeadline; }
    public LocalDateTime getFirstResponseAt() { return firstResponseAt; }
    public void setFirstResponseAt(LocalDateTime firstResponseAt) { this.firstResponseAt = firstResponseAt; }
    public LocalDateTime getResolvedAt() { return resolvedAt; }
    public void setResolvedAt(LocalDateTime resolvedAt) { this.resolvedAt = resolvedAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public Ticket getDuplicateOf() { return duplicateOf; }
    public void setDuplicateOf(Ticket duplicateOf) { this.duplicateOf = duplicateOf; }
}
