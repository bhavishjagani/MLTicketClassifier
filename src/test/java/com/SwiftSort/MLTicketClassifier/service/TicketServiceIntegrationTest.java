package com.SwiftSort.MLTicketClassifier.service;

import com.SwiftSort.MLTicketClassifier.dto.TicketCreateRequest;
import com.SwiftSort.MLTicketClassifier.dto.TicketResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class TicketServiceIntegrationTest {

    @Autowired
    private TicketService ticketService;

    @Test
    void createTicketWithAiClassification() {
        TicketCreateRequest req = new TicketCreateRequest();
        req.setSubject("Payment failed");
        req.setMessage("I paid but subscription not activated, very frustrated");
        req.setProduct("ECLTalk");
        req.setSource("WEBSITE");

        TicketResponse response = ticketService.createTicket(req);

        assertNotNull(response.getId());
        assertNotNull(response.getCategory());
        assertNotNull(response.getPriority());
        assertNotNull(response.getSentiment());
        assertNotNull(response.getSummary());
        assertNotNull(response.getSuggestedResponse());
        assertNotNull(response.getConfidence());
        assertNotNull(response.getSlaDeadline());
    }

    @Test
    void updateStatusAndHistory() {
        TicketCreateRequest req = new TicketCreateRequest();
        req.setSubject("Login issue");
        req.setMessage("Cannot login to account");
        TicketResponse created = ticketService.createTicket(req);

        ticketService.updateStatus(created.getId(), "OPEN", "admin@test.com");
        TicketResponse updated = ticketService.getTicketById(created.getId());

        assertEquals("OPEN", updated.getStatus());
        assertFalse(ticketService.getTicketHistory(created.getId()).isEmpty());
    }

    @Test
    void findSimilarTickets() {
        TicketCreateRequest req1 = new TicketCreateRequest();
        req1.setSubject("Payment not working");
        req1.setMessage("Paid but course still locked");
        TicketResponse t1 = ticketService.createTicket(req1);

        TicketCreateRequest req2 = new TicketCreateRequest();
        req2.setSubject("Subscription payment succeeded");
        req2.setMessage("Amount deducted but account not active");
        ticketService.createTicket(req2);

        var similar = ticketService.findSimilarTickets(t1.getId());
        assertNotNull(similar);
    }

    @Test
    void regenerateResponseWithTone() {
        TicketCreateRequest req = new TicketCreateRequest();
        req.setSubject("Refund request");
        req.setMessage("I want my money back");
        TicketResponse created = ticketService.createTicket(req);

        String response = ticketService.regenerateResponse(created.getId(), "APOLOGETIC");
        assertNotNull(response);
        assertFalse(response.isBlank());
    }
}
