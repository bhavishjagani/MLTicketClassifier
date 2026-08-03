package com.SwiftSort.MLTicketClassifier.repository;

import com.SwiftSort.MLTicketClassifier.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByRecipientEmailOrderByCreatedAtDesc(String email);
    List<Notification> findByRecipientEmailAndReadFalseOrderByCreatedAtDesc(String email);
    long countByRecipientEmailAndReadFalse(String email);
}
