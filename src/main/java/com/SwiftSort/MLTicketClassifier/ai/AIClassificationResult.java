package com.SwiftSort.MLTicketClassifier.ai;

import com.SwiftSort.MLTicketClassifier.model.Ticket;

import java.util.List;

public class AIClassificationResult {
    private Ticket.Category category;
    private Ticket.Priority priority;
    private Ticket.Sentiment sentiment;
    private Ticket.Urgency urgency;
    private String recommendedTeam;
    private List<String> suggestedTags;
    private double confidence;
    private String summary;
    private String suggestedResponse;
    private String modelName;
    private String promptVersion;
    private long responseTimeMs;
    private boolean validationPassed;

    public Ticket.Category getCategory() { return category; }
    public void setCategory(Ticket.Category category) { this.category = category; }
    public Ticket.Priority getPriority() { return priority; }
    public void setPriority(Ticket.Priority priority) { this.priority = priority; }
    public Ticket.Sentiment getSentiment() { return sentiment; }
    public void setSentiment(Ticket.Sentiment sentiment) { this.sentiment = sentiment; }
    public Ticket.Urgency getUrgency() { return urgency; }
    public void setUrgency(Ticket.Urgency urgency) { this.urgency = urgency; }
    public String getRecommendedTeam() { return recommendedTeam; }
    public void setRecommendedTeam(String recommendedTeam) { this.recommendedTeam = recommendedTeam; }
    public List<String> getSuggestedTags() { return suggestedTags; }
    public void setSuggestedTags(List<String> suggestedTags) { this.suggestedTags = suggestedTags; }
    public double getConfidence() { return confidence; }
    public void setConfidence(double confidence) { this.confidence = confidence; }
    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }
    public String getSuggestedResponse() { return suggestedResponse; }
    public void setSuggestedResponse(String suggestedResponse) { this.suggestedResponse = suggestedResponse; }
    public String getModelName() { return modelName; }
    public void setModelName(String modelName) { this.modelName = modelName; }
    public String getPromptVersion() { return promptVersion; }
    public void setPromptVersion(String promptVersion) { this.promptVersion = promptVersion; }
    public long getResponseTimeMs() { return responseTimeMs; }
    public void setResponseTimeMs(long responseTimeMs) { this.responseTimeMs = responseTimeMs; }
    public boolean isValidationPassed() { return validationPassed; }
    public void setValidationPassed(boolean validationPassed) { this.validationPassed = validationPassed; }
}
