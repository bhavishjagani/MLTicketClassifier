package com.SwiftSort.MLTicketClassifier.service;

import com.SwiftSort.MLTicketClassifier.model.Notification;
import com.SwiftSort.MLTicketClassifier.model.Ticket;
import com.SwiftSort.MLTicketClassifier.repository.NotificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class NotificationService {

    private final NotificationRepository notificationRepo;

    public NotificationService(NotificationRepository notificationRepo) {
        this.notificationRepo = notificationRepo;
    }

    public void notifyCriticalTicket(Ticket ticket, String adminEmail) {
        create(ticket.getId(), adminEmail, "CRITICAL_TICKET",
                "Critical ticket #" + ticket.getId() + ": " + ticket.getSubject());
    }

    public void notifyAssignment(Ticket ticket, String agentEmail) {
        create(ticket.getId(), agentEmail, "TICKET_ASSIGNED",
                "Ticket #" + ticket.getId() + " has been assigned to you.");
    }

    public void notifySlaWarning(Ticket ticket, String email) {
        create(ticket.getId(), email, "SLA_WARNING",
                "SLA deadline approaching for ticket #" + ticket.getId());
    }

    public void notifyReopened(Ticket ticket, String email) {
        create(ticket.getId(), email, "TICKET_REOPENED",
                "Ticket #" + ticket.getId() + " has been reopened.");
    }

    public void notifyDuplicatePattern(Ticket ticket, String email, int count) {
        create(ticket.getId(), email, "DUPLICATE_PATTERN",
                count + " customers reported a similar issue to ticket #" + ticket.getId());
    }

    @Transactional(readOnly = true)
    public List<Notification> getForUser(String email) {
        return notificationRepo.findByRecipientEmailOrderByCreatedAtDesc(email);
    }

    @Transactional(readOnly = true)
    public List<Notification> getUnread(String email) {
        return notificationRepo.findByRecipientEmailAndReadFalseOrderByCreatedAtDesc(email);
    }

    public void markRead(Long id) {
        notificationRepo.findById(id).ifPresent(n -> {
            n.setRead(true);
            notificationRepo.save(n);
        });
    }

    private void create(Long ticketId, String email, String type, String message) {
        Notification n = new Notification();
        n.setTicketId(ticketId);
        n.setRecipientEmail(email);
        n.setType(type);
        n.setMessage(message);
        notificationRepo.save(n);
    }
}
