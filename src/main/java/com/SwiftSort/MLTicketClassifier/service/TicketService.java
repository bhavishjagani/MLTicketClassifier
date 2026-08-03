package com.SwiftSort.MLTicketClassifier.service;

import com.SwiftSort.MLTicketClassifier.dto.CommentResponse;
import com.SwiftSort.MLTicketClassifier.dto.DashboardStats;
import com.SwiftSort.MLTicketClassifier.dto.TicketCreateRequest;
import com.SwiftSort.MLTicketClassifier.dto.TicketResponse;
import com.SwiftSort.MLTicketClassifier.model.Ticket;
import com.SwiftSort.MLTicketClassifier.model.TicketHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface TicketService {
    Page<TicketResponse> findAll(Pageable pageable, String search, String status, String priority,
                                   String category, String product, String sentiment, Long agentId, String team);
    TicketResponse getTicketById(Long id);
    TicketResponse createTicket(TicketCreateRequest request);
    List<TicketResponse> importFromCsv(MultipartFile file) throws IOException;
    void updateStatus(Long id, String status, String performedBy);
    void updatePriority(Long id, String priority, String performedBy);
    void assignAgent(Long id, Long agentId, String performedBy);
    void assignTeam(Long id, String team, String performedBy);
    void addComment(Long id, String text, boolean internal, String authorEmail);
    List<CommentResponse> getComments(Long id, boolean includeInternal);
    void approveResponse(Long id, String performedBy);
    void rejectResponse(Long id);
    String regenerateResponse(Long id, String tone);
    void sendResponse(Long id, String response, String performedBy);
    void updateResolutionNotes(Long id, String notes);
    DashboardStats getDashboardStats();
    List<TicketResponse> getRecentTickets(int limit);
    List<TicketResponse> findSimilarTickets(Long id);
    void markDuplicate(Long id, Long duplicateOfId, String performedBy);
    List<TicketResponse> getAgentQueue(Long agentId);
    List<TicketHistory> getTicketHistory(Long id);
    Map<String, Object> uploadAttachment(Long ticketId, MultipartFile file) throws IOException;
}
