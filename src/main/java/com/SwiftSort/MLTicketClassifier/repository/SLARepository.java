package com.SwiftSort.MLTicketClassifier.repository;

import com.SwiftSort.MLTicketClassifier.model.SLA;
import com.SwiftSort.MLTicketClassifier.model.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SLARepository extends JpaRepository<SLA, Long> {
    SLA findByPriority(Ticket.Priority priority);
}
