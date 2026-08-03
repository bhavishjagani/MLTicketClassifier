package com.SwiftSort.MLTicketClassifier.repository;

import com.SwiftSort.MLTicketClassifier.model.Ticket;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TicketRepository extends JpaRepository<Ticket, Long> {

    @Query(value = "SELECT t FROM Ticket t WHERE " +
            "(:search IS NULL OR LOWER(t.subject) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(t.message) LIKE LOWER(CONCAT('%', :search, '%'))) " +
            "AND (:status IS NULL OR t.status = :status) " +
            "AND (:priority IS NULL OR t.priority = :priority) " +
            "AND (:category IS NULL OR t.category = :category) " +
            "AND (:product IS NULL OR LOWER(t.product) = LOWER(:product)) " +
            "AND (:sentiment IS NULL OR t.sentiment = :sentiment) " +
            "AND (:agentId IS NULL OR t.assignedAgent.id = :agentId) " +
            "AND (:team IS NULL OR LOWER(t.assignedTeam) = LOWER(:team))",
            countQuery = "SELECT COUNT(t) FROM Ticket t WHERE " +
                    "(:search IS NULL OR LOWER(t.subject) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(t.message) LIKE LOWER(CONCAT('%', :search, '%'))) " +
                    "AND (:status IS NULL OR t.status = :status) " +
                    "AND (:priority IS NULL OR t.priority = :priority) " +
                    "AND (:category IS NULL OR t.category = :category) " +
                    "AND (:product IS NULL OR LOWER(t.product) = LOWER(:product)) " +
                    "AND (:sentiment IS NULL OR t.sentiment = :sentiment) " +
                    "AND (:agentId IS NULL OR t.assignedAgent.id = :agentId) " +
                    "AND (:team IS NULL OR LOWER(t.assignedTeam) = LOWER(:team))")
    Page<Ticket> searchAdvanced(@Param("search") String search,
                                @Param("status") Ticket.Status status,
                                @Param("priority") Ticket.Priority priority,
                                @Param("category") Ticket.Category category,
                                @Param("product") String product,
                                @Param("sentiment") Ticket.Sentiment sentiment,
                                @Param("agentId") Long agentId,
                                @Param("team") String team,
                                Pageable pageable);

    @Query(value = "SELECT t FROM Ticket t WHERE " +
            "(:search IS NULL OR LOWER(t.subject) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(t.message) LIKE LOWER(CONCAT('%', :search, '%'))) " +
            "AND (:status IS NULL OR t.status = :status) " +
            "AND (:priority IS NULL OR t.priority = :priority)",
            countQuery = "SELECT COUNT(t) FROM Ticket t WHERE " +
                    "(:search IS NULL OR LOWER(t.subject) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(t.message) LIKE LOWER(CONCAT('%', :search, '%'))) " +
                    "AND (:status IS NULL OR t.status = :status) " +
                    "AND (:priority IS NULL OR t.priority = :priority)")
    Page<Ticket> search(@Param("search") String search,
                        @Param("status") Ticket.Status status,
                        @Param("priority") Ticket.Priority priority,
                        Pageable pageable);

    long countByStatus(Ticket.Status status);
    long countByPriority(Ticket.Priority priority);
    List<Ticket> findByOrderByCreatedAtDesc(Pageable pageable);
    List<Ticket> findByCategory(Ticket.Category category);
    List<Ticket> findByProduct(String product);
    List<Ticket> findByAssignedAgentId(Long agentId);

    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.status IN (com.SwiftSort.MLTicketClassifier.model.Ticket.Status.NEW, com.SwiftSort.MLTicketClassifier.model.Ticket.Status.OPEN, com.SwiftSort.MLTicketClassifier.model.Ticket.Status.IN_PROGRESS)")
    long countOpenTickets();

    @Query("SELECT t FROM Ticket t WHERE t.status IN (com.SwiftSort.MLTicketClassifier.model.Ticket.Status.NEW, com.SwiftSort.MLTicketClassifier.model.Ticket.Status.OPEN, com.SwiftSort.MLTicketClassifier.model.Ticket.Status.IN_PROGRESS, com.SwiftSort.MLTicketClassifier.model.Ticket.Status.REOPENED)")
    List<Ticket> findOpenTickets();

    @Query(value = "SELECT AVG(EXTRACT(EPOCH FROM (updated_at - created_at)) / 3600.0) FROM tickets WHERE status = 'RESOLVED'", nativeQuery = true)
    Double avgResolutionHours();

    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.status = com.SwiftSort.MLTicketClassifier.model.Ticket.Status.REOPENED")
    long countReopened();

    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.status = com.SwiftSort.MLTicketClassifier.model.Ticket.Status.DUPLICATE")
    long countDuplicates();

    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.sentiment IN (com.SwiftSort.MLTicketClassifier.model.Ticket.Sentiment.NEGATIVE, com.SwiftSort.MLTicketClassifier.model.Ticket.Sentiment.ANGRY, com.SwiftSort.MLTicketClassifier.model.Ticket.Sentiment.FRUSTRATED)")
    long countNegativeSentiment();

    @Query("SELECT t.category, COUNT(t) FROM Ticket t GROUP BY t.category")
    List<Object[]> countByCategory();

    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.category = :category")
    long countByCategoryEnum(@Param("category") Ticket.Category category);

    @Query("SELECT t.priority, COUNT(t) FROM Ticket t GROUP BY t.priority")
    List<Object[]> countByPriorityGrouped();

    @Query(value = "SELECT DATE_TRUNC('week', created_at) as week, sentiment, COUNT(*) FROM tickets GROUP BY week, sentiment ORDER BY week", nativeQuery = true)
    List<Object[]> sentimentTrendRaw();

    @Query(value = "SELECT DATE(created_at) as date, COUNT(*) FROM tickets GROUP BY date ORDER BY date", nativeQuery = true)
    List<Object[]> countByDay();

    @Query("SELECT t FROM Ticket t WHERE t.assignedAgent.id = :agentId AND t.status NOT IN (com.SwiftSort.MLTicketClassifier.model.Ticket.Status.RESOLVED, com.SwiftSort.MLTicketClassifier.model.Ticket.Status.CLOSED, com.SwiftSort.MLTicketClassifier.model.Ticket.Status.DUPLICATE)")
    List<Ticket> findAgentQueue(@Param("agentId") Long agentId);

    @Query("SELECT t FROM Ticket t WHERE t.priority = com.SwiftSort.MLTicketClassifier.model.Ticket.Priority.CRITICAL AND t.status NOT IN (com.SwiftSort.MLTicketClassifier.model.Ticket.Status.RESOLVED, com.SwiftSort.MLTicketClassifier.model.Ticket.Status.CLOSED)")
    List<Ticket> findCriticalOpen();
}
