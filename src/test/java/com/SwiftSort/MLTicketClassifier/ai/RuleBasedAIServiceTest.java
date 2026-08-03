package com.SwiftSort.MLTicketClassifier.ai;

import com.SwiftSort.MLTicketClassifier.model.Ticket;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RuleBasedAIServiceTest {

    @Mock
    private RAGService ragService;

    private RuleBasedAIService aiService;

    @BeforeEach
    void setUp() {
        aiService = new RuleBasedAIService(ragService);
        when(ragService.findBestMatch(anyString())).thenReturn(null);
    }

    @Test
    void classifyPaymentIssue() {
        Ticket ticket = new Ticket();
        ticket.setSubject("Payment not activated");
        ticket.setMessage("I paid but my subscription was not activated after payment was deducted");

        AIClassificationResult result = aiService.classifyTicket(ticket);

        assertEquals(Ticket.Category.PAYMENT_ISSUE, result.getCategory());
        assertNotNull(result.getPriority());
        assertNotNull(result.getSentiment());
        assertTrue(result.getConfidence() > 0);
        assertTrue(result.isValidationPassed());
        assertNotNull(result.getSuggestedResponse());
    }

    @Test
    void classifyFeatureRequest() {
        Ticket ticket = new Ticket();
        ticket.setSubject("Feature suggestion");
        ticket.setMessage("Please add recorded classes feature");

        AIClassificationResult result = aiService.classifyTicket(ticket);

        assertEquals(Ticket.Category.FEATURE_REQUEST, result.getCategory());
        assertNotNull(result.getRecommendedTeam());
    }

    @Test
    void summarizeShortText() {
        assertEquals("Hello", aiService.summarize("Hello"));
    }

    @Test
    void summarizeLongText() {
        String longText = "First sentence here. Second sentence here. Third sentence here. Fourth sentence.";
        String summary = aiService.summarize(longText);
        assertNotNull(summary);
        assertTrue(summary.length() <= longText.length());
    }

    @Test
    void generateResponseWithTone() {
        Ticket ticket = new Ticket();
        ticket.setCategory(Ticket.Category.PAYMENT_ISSUE);
        ticket.setMessage("Payment issue");

        String response = aiService.generateSuggestedResponse(ticket, Ticket.ResponseTone.APOLOGETIC);
        assertNotNull(response);
        assertTrue(response.toLowerCase().contains("apolog"));
    }

    @Test
    void generateWeeklyReport() {
        String report = aiService.generateWeeklyReport("42 tickets received");
        assertNotNull(report);
        assertTrue(report.contains("42"));
    }
}
