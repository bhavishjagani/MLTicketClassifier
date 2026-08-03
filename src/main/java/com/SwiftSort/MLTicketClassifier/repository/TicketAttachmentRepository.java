package com.SwiftSort.MLTicketClassifier.repository;

import com.SwiftSort.MLTicketClassifier.model.TicketAttachment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TicketAttachmentRepository extends JpaRepository<TicketAttachment, Long> {
    List<TicketAttachment> findByTicket_Id(Long ticketId);
}
