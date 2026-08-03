package com.SwiftSort.MLTicketClassifier.dto;

import java.time.LocalDateTime;
import java.util.List;

public class TicketResponse {
    private Long id;
    private String subject;
    private String message;
    private String maskedMessage;
    private String customerReference;
    private String product;
    private String source;
    private String category;
    private String priority;
    private String status;
    private String assignedTeam;
    private String recommendedTeam;
    private String assignedAgent;
    private Long assignedAgentId;
    private String sentiment;
    private String urgency;
    private String summary;
    private Double confidence;
    private String suggestedResponse;
    private String responseTone;
    private Boolean responseApproved;
    private Boolean responseSent;
    private String resolutionNotes;
    private List<String> tags;
    private String aiModelName;
    private LocalDateTime slaDeadline;
    private Long slaMinutesRemaining;
    private Boolean slaOverdue;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long duplicateOfId;
    private double similarityScore;

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
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getAssignedTeam() { return assignedTeam; }
    public void setAssignedTeam(String assignedTeam) { this.assignedTeam = assignedTeam; }
    public String getRecommendedTeam() { return recommendedTeam; }
    public void setRecommendedTeam(String recommendedTeam) { this.recommendedTeam = recommendedTeam; }
    public String getAssignedAgent() { return assignedAgent; }
    public void setAssignedAgent(String assignedAgent) { this.assignedAgent = assignedAgent; }
    public Long getAssignedAgentId() { return assignedAgentId; }
    public void setAssignedAgentId(Long assignedAgentId) { this.assignedAgentId = assignedAgentId; }
    public String getSentiment() { return sentiment; }
    public void setSentiment(String sentiment) { this.sentiment = sentiment; }
    public String getUrgency() { return urgency; }
    public void setUrgency(String urgency) { this.urgency = urgency; }
    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }
    public Double getConfidence() { return confidence; }
    public void setConfidence(Double confidence) { this.confidence = confidence; }
    public String getSuggestedResponse() { return suggestedResponse; }
    public void setSuggestedResponse(String suggestedResponse) { this.suggestedResponse = suggestedResponse; }
    public String getResponseTone() { return responseTone; }
    public void setResponseTone(String responseTone) { this.responseTone = responseTone; }
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
    public LocalDateTime getSlaDeadline() { return slaDeadline; }
    public void setSlaDeadline(LocalDateTime slaDeadline) { this.slaDeadline = slaDeadline; }
    public Long getSlaMinutesRemaining() { return slaMinutesRemaining; }
    public void setSlaMinutesRemaining(Long slaMinutesRemaining) { this.slaMinutesRemaining = slaMinutesRemaining; }
    public Boolean getSlaOverdue() { return slaOverdue; }
    public void setSlaOverdue(Boolean slaOverdue) { this.slaOverdue = slaOverdue; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public Long getDuplicateOfId() { return duplicateOfId; }
    public void setDuplicateOfId(Long duplicateOfId) { this.duplicateOfId = duplicateOfId; }
    public double getSimilarityScore() { return similarityScore; }
    public void setSimilarityScore(double similarityScore) { this.similarityScore = similarityScore; }
}
