package com.SwiftSort.MLTicketClassifier.ai;

import com.SwiftSort.MLTicketClassifier.model.Ticket;

public interface AIService {
    AIClassificationResult classifyTicket(Ticket ticket);
    String summarize(String text);
    String generateSuggestedResponse(Ticket ticket, Ticket.ResponseTone tone);
    String generateWeeklyReport(String dataSummary);
}
