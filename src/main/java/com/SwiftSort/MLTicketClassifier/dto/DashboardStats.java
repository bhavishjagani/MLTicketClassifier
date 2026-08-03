package com.SwiftSort.MLTicketClassifier.dto;

public class DashboardStats {
    private long totalTickets;
    private long newTickets;
    private long openTickets;
    private long resolvedTickets;
    private long criticalTickets;
    private long awaitingResponse;
    private double avgResolutionHours;
    private double avgFirstResponseHours;
    private double negativeSentimentPct;
    private String topCategory;

    public DashboardStats() {}

    public long getTotalTickets() { return totalTickets; }
    public void setTotalTickets(long totalTickets) { this.totalTickets = totalTickets; }
    public long getNewTickets() { return newTickets; }
    public void setNewTickets(long newTickets) { this.newTickets = newTickets; }
    public long getOpenTickets() { return openTickets; }
    public void setOpenTickets(long openTickets) { this.openTickets = openTickets; }
    public long getResolvedTickets() { return resolvedTickets; }
    public void setResolvedTickets(long resolvedTickets) { this.resolvedTickets = resolvedTickets; }
    public long getCriticalTickets() { return criticalTickets; }
    public void setCriticalTickets(long criticalTickets) { this.criticalTickets = criticalTickets; }
    public long getAwaitingResponse() { return awaitingResponse; }
    public void setAwaitingResponse(long awaitingResponse) { this.awaitingResponse = awaitingResponse; }
    public double getAvgResolutionHours() { return avgResolutionHours; }
    public void setAvgResolutionHours(double avgResolutionHours) { this.avgResolutionHours = avgResolutionHours; }
    public double getAvgFirstResponseHours() { return avgFirstResponseHours; }
    public void setAvgFirstResponseHours(double avgFirstResponseHours) { this.avgFirstResponseHours = avgFirstResponseHours; }
    public double getNegativeSentimentPct() { return negativeSentimentPct; }
    public void setNegativeSentimentPct(double negativeSentimentPct) { this.negativeSentimentPct = negativeSentimentPct; }
    public String getTopCategory() { return topCategory; }
    public void setTopCategory(String topCategory) { this.topCategory = topCategory; }
}
