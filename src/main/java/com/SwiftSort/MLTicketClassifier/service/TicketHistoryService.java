package com.SwiftSort.MLTicketClassifier.service;

import com.SwiftSort.MLTicketClassifier.model.Ticket;
import com.SwiftSort.MLTicketClassifier.model.TicketHistory;
import com.SwiftSort.MLTicketClassifier.repository.TicketHistoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class TicketHistoryService {

    private final TicketHistoryRepository historyRepo;

    public TicketHistoryService(TicketHistoryRepository historyRepo) {
        this.historyRepo = historyRepo;
    }

    public void record(Ticket ticket, String eventType, String description, String performedBy) {
        TicketHistory h = new TicketHistory();
        h.setTicket(ticket);
        h.setEventType(eventType);
        h.setDescription(description);
        h.setPerformedBy(performedBy != null ? performedBy : "system");
        historyRepo.save(h);
    }

    @Transactional(readOnly = true)
    public List<TicketHistory> getHistory(Long ticketId) {
        return historyRepo.findByTicket_IdOrderByCreatedAtAsc(ticketId);
    }
}
