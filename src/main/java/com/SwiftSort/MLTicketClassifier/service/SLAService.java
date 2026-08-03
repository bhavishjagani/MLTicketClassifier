package com.SwiftSort.MLTicketClassifier.service;

import com.SwiftSort.MLTicketClassifier.model.SLA;
import com.SwiftSort.MLTicketClassifier.model.Ticket;
import com.SwiftSort.MLTicketClassifier.repository.SLARepository;
import com.SwiftSort.MLTicketClassifier.repository.TicketRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional(readOnly = true)
public class SLAService {

    private final SLARepository slaRepo;
    private final TicketRepository ticketRepo;

    public SLAService(SLARepository slaRepo, TicketRepository ticketRepo) {
        this.slaRepo = slaRepo;
        this.ticketRepo = ticketRepo;
    }

    public LocalDateTime calculateDeadline(Ticket.Priority priority) {
        return slaRepo.findByPriority(priority)
                .map(sla -> LocalDateTime.now().plusHours(sla.getResponseHours()))
                .orElse(LocalDateTime.now().plusHours(24));
    }

    public boolean isOverdue(Ticket ticket) {
        if (ticket.getSlaDeadline() == null) {
            return false;
        }
        return LocalDateTime.now().isAfter(ticket.getSlaDeadline())
                && ticket.getStatus() != Ticket.Status.RESOLVED
                && ticket.getStatus() != Ticket.Status.CLOSED;
    }

    public long getMinutesRemaining(Ticket ticket) {
        if (ticket.getSlaDeadline() == null) {
            return 0;
        }
        return java.time.Duration.between(LocalDateTime.now(), ticket.getSlaDeadline()).toMinutes();
    }

    public Map<String, Object> getSlaReport() {
        List<Ticket> open = ticketRepo.findOpenTickets();
        long overdue = open.stream().filter(this::isOverdue).count();
        long atRisk = open.stream().filter(t -> {
            long mins = getMinutesRemaining(t);
            return mins > 0 && mins <= 60;
        }).count();
        Map<String, Object> report = new HashMap<>();
        report.put("totalOpen", open.size());
        report.put("overdue", overdue);
        report.put("atRisk", atRisk);
        report.put("rules", slaRepo.findAll());
        return report;
    }
}
