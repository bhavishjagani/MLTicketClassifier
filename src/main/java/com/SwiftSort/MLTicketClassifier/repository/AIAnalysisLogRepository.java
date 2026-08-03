package com.SwiftSort.MLTicketClassifier.repository;

import com.SwiftSort.MLTicketClassifier.model.AIAnalysisLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AIAnalysisLogRepository extends JpaRepository<AIAnalysisLog, Long> {
    List<AIAnalysisLog> findByTicketIdOrderByAnalyzedAtDesc(Long ticketId);
}
