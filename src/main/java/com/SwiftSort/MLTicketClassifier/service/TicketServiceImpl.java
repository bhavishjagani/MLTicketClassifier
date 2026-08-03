package com.SwiftSort.MLTicketClassifier.service;

import com.SwiftSort.MLTicketClassifier.ai.AIService;
import com.SwiftSort.MLTicketClassifier.ai.EmbeddingService;
import com.SwiftSort.MLTicketClassifier.dto.CommentResponse;
import com.SwiftSort.MLTicketClassifier.dto.DashboardStats;
import com.SwiftSort.MLTicketClassifier.dto.TicketCreateRequest;
import com.SwiftSort.MLTicketClassifier.dto.TicketResponse;
import com.SwiftSort.MLTicketClassifier.model.Ticket;
import com.SwiftSort.MLTicketClassifier.model.TicketComment;
import com.SwiftSort.MLTicketClassifier.model.TicketHistory;
import com.SwiftSort.MLTicketClassifier.model.User;
import com.SwiftSort.MLTicketClassifier.repository.TicketCommentRepository;
import com.SwiftSort.MLTicketClassifier.repository.TicketRepository;
import com.SwiftSort.MLTicketClassifier.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class TicketServiceImpl implements TicketService {

    private final TicketRepository ticketRepo;
    private final TicketCommentRepository commentRepo;
    private final UserRepository userRepo;
    private final AIService aiService;
    private final SLAService slaService;
    private final NotificationService notificationService;
    private final TicketHistoryService historyService;
    private final AttachmentService attachmentService;
    private final EmbeddingService embeddingService;
    private final AuditService auditService;

    public TicketServiceImpl(TicketRepository ticketRepo, TicketCommentRepository commentRepo,
                             UserRepository userRepo, AIService aiService, SLAService slaService,
                             NotificationService notificationService, TicketHistoryService historyService,
                             AttachmentService attachmentService, EmbeddingService embeddingService,
                             AuditService auditService) {
        this.ticketRepo = ticketRepo;
        this.commentRepo = commentRepo;
        this.userRepo = userRepo;
        this.aiService = aiService;
        this.slaService = slaService;
        this.notificationService = notificationService;
        this.historyService = historyService;
        this.attachmentService = attachmentService;
        this.embeddingService = embeddingService;
        this.auditService = auditService;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TicketResponse> findAll(Pageable pageable, String search, String status, String priority,
                                        String category, String product, String sentiment, Long agentId, String team) {
        return ticketRepo.searchAdvanced(
                blankToNull(search),
                parseEnum(status, Ticket.Status.class),
                parseEnum(priority, Ticket.Priority.class),
                parseEnum(category, Ticket.Category.class),
                blankToNull(product),
                parseEnum(sentiment, Ticket.Sentiment.class),
                agentId,
                blankToNull(team),
                pageable
        ).map(t -> toResponse(t, slaService));
    }

    @Override
    @Transactional(readOnly = true)
    public TicketResponse getTicketById(Long id) {
        return toResponse(findOrThrow(id), slaService);
    }

    @Override
    public TicketResponse createTicket(TicketCreateRequest req) {
        Ticket t = new Ticket();
        t.setSubject(req.getSubject());
        t.setMessage(req.getMessage());
        t.setCustomerReference(req.getCustomerReference());
        t.setProduct(req.getProduct());
        if (req.getSource() != null && !req.getSource().isBlank()) {
            try { t.setSource(Ticket.Source.valueOf(req.getSource().toUpperCase())); } catch (IllegalArgumentException ignored) {}
        }
        if (req.getCategory() != null && !req.getCategory().isBlank()) {
            try { t.setCategory(Ticket.Category.valueOf(req.getCategory().toUpperCase())); } catch (IllegalArgumentException ignored) {}
        }
        if (req.getPriority() != null && !req.getPriority().isBlank()) {
            try { t.setPriority(Ticket.Priority.valueOf(req.getPriority().toUpperCase())); } catch (IllegalArgumentException ignored) {}
        }
        Ticket saved = ticketRepo.save(t);
        aiService.classifyTicket(saved);
        saved.setSlaDeadline(slaService.calculateDeadline(saved.getPriority()));
        saved = ticketRepo.save(saved);
        historyService.record(saved, "TICKET_CREATED", "Ticket created", "system");
        historyService.record(saved, "AI_ANALYSIS", "AI classification completed", "system");
        if (saved.getPriority() == Ticket.Priority.CRITICAL) {
            notificationService.notifyCriticalTicket(saved, "admin@swiftsort.dev");
        }
        auditService.log("CREATE", "TICKET", saved.getId(), "system", saved.getSubject());
        return toResponse(saved, slaService);
    }

    @Override
    public List<TicketResponse> importFromCsv(MultipartFile file) throws IOException {
        List<TicketResponse> results = new ArrayList<>();
        try (CSVParser parser = CSVFormat.DEFAULT.builder().setHeader().setSkipHeaderRecord(true).build()
                .parse(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            for (CSVRecord record : parser) {
                TicketCreateRequest req = new TicketCreateRequest();
                req.setSubject(record.get("subject"));
                req.setMessage(record.get("message"));
                if (record.isMapped("product")) req.setProduct(record.get("product"));
                if (record.isMapped("customerReference")) req.setCustomerReference(record.get("customerReference"));
                if (record.isMapped("source")) req.setSource(record.get("source"));
                results.add(createTicket(req));
            }
        }
        return results;
    }

    @Override
    public void updateStatus(Long id, String status, String performedBy) {
        Ticket t = findOrThrow(id);
        Ticket.Status old = t.getStatus();
        t.setStatus(Ticket.Status.valueOf(status.toUpperCase()));
        if (t.getStatus() == Ticket.Status.RESOLVED || t.getStatus() == Ticket.Status.CLOSED) {
            t.setResolvedAt(LocalDateTime.now());
        }
        if (t.getStatus() == Ticket.Status.REOPENED) {
            notificationService.notifyReopened(t, performedBy);
        }
        ticketRepo.save(t);
        historyService.record(t, "STATUS_CHANGED", old + " -> " + t.getStatus(), performedBy);
        auditService.log("UPDATE_STATUS", "TICKET", id, performedBy, status);
    }

    @Override
    public void updatePriority(Long id, String priority, String performedBy) {
        Ticket t = findOrThrow(id);
        t.setPriority(Ticket.Priority.valueOf(priority.toUpperCase()));
        t.setSlaDeadline(slaService.calculateDeadline(t.getPriority()));
        ticketRepo.save(t);
        historyService.record(t, "PRIORITY_CHANGED", "Priority set to " + priority, performedBy);
        auditService.log("UPDATE_PRIORITY", "TICKET", id, performedBy, priority);
    }

    @Override
    public void assignAgent(Long id, Long agentId, String performedBy) {
        Ticket t = findOrThrow(id);
        if (agentId == null) {
            t.setAssignedAgent(null);
        } else {
            User agent = userRepo.findById(agentId).orElseThrow(() -> new EntityNotFoundException("User not found: " + agentId));
            t.setAssignedAgent(agent);
            notificationService.notifyAssignment(t, agent.getEmail());
        }
        ticketRepo.save(t);
        historyService.record(t, "AGENT_ASSIGNED", "Agent ID: " + agentId, performedBy);
        auditService.log("ASSIGN", "TICKET", id, performedBy, "agent:" + agentId);
    }

    @Override
    public void assignTeam(Long id, String team, String performedBy) {
        Ticket t = findOrThrow(id);
        t.setAssignedTeam(team);
        ticketRepo.save(t);
        historyService.record(t, "TEAM_ASSIGNED", "Team: " + team, performedBy);
    }

    @Override
    public void addComment(Long id, String text, boolean internal, String authorEmail) {
        Ticket t = findOrThrow(id);
        TicketComment c = new TicketComment();
        c.setTicket(t);
        c.setText(text);
        c.setInternalNote(internal);
        if (authorEmail != null) {
            userRepo.findByEmail(authorEmail).ifPresent(c::setAuthor);
        }
        commentRepo.save(c);
        historyService.record(t, internal ? "INTERNAL_NOTE" : "COMMENT_ADDED", text, authorEmail);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentResponse> getComments(Long id, boolean includeInternal) {
        return commentRepo.findByTicket_IdOrderByCreatedAtAsc(id).stream()
                .filter(c -> includeInternal || !c.isInternalNote())
                .map(c -> {
                    CommentResponse r = new CommentResponse();
                    r.setText(c.getText());
                    r.setCreatedAt(c.getCreatedAt());
                    r.setInternal(c.isInternalNote());
                    if (c.getAuthor() != null) r.setAuthor(c.getAuthor().getName());
                    return r;
                }).toList();
    }

    @Override
    public void approveResponse(Long id, String performedBy) {
        Ticket t = findOrThrow(id);
        t.setResponseApproved(true);
        ticketRepo.save(t);
        historyService.record(t, "RESPONSE_APPROVED", "Response approved", performedBy);
        auditService.log("APPROVE_RESPONSE", "TICKET", id, performedBy, null);
    }

    @Override
    public void rejectResponse(Long id) {
        Ticket t = findOrThrow(id);
        t.setResponseApproved(false);
        ticketRepo.save(t);
        historyService.record(t, "RESPONSE_REJECTED", "Response rejected", null);
    }

    @Override
    public String regenerateResponse(Long id, String tone) {
        Ticket t = findOrThrow(id);
        Ticket.ResponseTone responseTone = Ticket.ResponseTone.PROFESSIONAL;
        if (tone != null && !tone.isBlank()) {
            try { responseTone = Ticket.ResponseTone.valueOf(tone.toUpperCase()); } catch (IllegalArgumentException ignored) {}
        }
        t.setResponseTone(responseTone);
        String response = aiService.generateSuggestedResponse(t, responseTone);
        t.setSuggestedResponse(response);
        t.setResponseApproved(false);
        ticketRepo.save(t);
        historyService.record(t, "RESPONSE_REGENERATED", "Tone: " + responseTone, null);
        return response;
    }

    @Override
    public void sendResponse(Long id, String response, String performedBy) {
        Ticket t = findOrThrow(id);
        t.setSuggestedResponse(response);
        t.setResponseSent(true);
        t.setResponseApproved(true);
        if (t.getFirstResponseAt() == null) {
            t.setFirstResponseAt(LocalDateTime.now());
        }
        t.setStatus(Ticket.Status.IN_PROGRESS);
        ticketRepo.save(t);
        historyService.record(t, "RESPONSE_SENT", "Response sent to customer", performedBy);
    }

    @Override
    public void updateResolutionNotes(Long id, String notes) {
        Ticket t = findOrThrow(id);
        t.setResolutionNotes(notes);
        ticketRepo.save(t);
        historyService.record(t, "RESOLUTION_NOTES", notes, null);
    }

    @Override
    @Transactional(readOnly = true)
    public DashboardStats getDashboardStats() {
        DashboardStats stats = new DashboardStats();
        long total = ticketRepo.count();
        stats.setTotalTickets(total);
        stats.setNewTickets(ticketRepo.countByStatus(Ticket.Status.NEW));
        stats.setOpenTickets(ticketRepo.countOpenTickets());
        stats.setResolvedTickets(ticketRepo.countByStatus(Ticket.Status.RESOLVED) + ticketRepo.countByStatus(Ticket.Status.CLOSED));
        stats.setCriticalTickets(ticketRepo.countByPriority(Ticket.Priority.CRITICAL));
        stats.setAwaitingResponse(ticketRepo.countByStatus(Ticket.Status.WAITING_FOR_CUSTOMER));
        Double avgRes = ticketRepo.avgResolutionHours();
        stats.setAvgResolutionHours(avgRes != null ? avgRes : 0);
        stats.setNegativeSentimentPct(total > 0 ? (double) ticketRepo.countNegativeSentiment() / total * 100 : 0);
        stats.setTopCategory(ticketRepo.countByCategory().stream()
                .max(Comparator.comparingLong(arr -> (Long) arr[1]))
                .map(arr -> arr[0] != null ? arr[0].toString() : "UNKNOWN")
                .orElse("N/A"));
        return stats;
    }

    @Override
    @Transactional(readOnly = true)
    public List<TicketResponse> getRecentTickets(int limit) {
        return ticketRepo.findByOrderByCreatedAtDesc(PageRequest.of(0, limit)).stream()
                .map(t -> toResponse(t, slaService)).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TicketResponse> findSimilarTickets(Long id) {
        Ticket ticket = findOrThrow(id);
        float[] queryVec = ticket.getEmbedding() != null
                ? embeddingService.fromStorageString(ticket.getEmbedding())
                : embeddingService.embed(ticket.getSubject() + " " + ticket.getMessage());

        return ticketRepo.findAll().stream()
                .filter(t -> !t.getId().equals(id))
                .map(t -> {
                    float[] vec = t.getEmbedding() != null
                            ? embeddingService.fromStorageString(t.getEmbedding())
                            : embeddingService.embed(t.getSubject() + " " + t.getMessage());
                    TicketResponse r = toResponse(t, slaService);
                    r.setSimilarityScore(embeddingService.cosineSimilarity(queryVec, vec));
                    return r;
                })
                .filter(r -> r.getSimilarityScore() > 0.3)
                .sorted(Comparator.comparingDouble(TicketResponse::getSimilarityScore).reversed())
                .limit(5)
                .collect(Collectors.toList());
    }

    @Override
    public void markDuplicate(Long id, Long duplicateOfId, String performedBy) {
        Ticket duplicate = findOrThrow(id);
        Ticket original = findOrThrow(duplicateOfId);
        duplicate.setStatus(Ticket.Status.DUPLICATE);
        duplicate.setDuplicateOf(original);
        ticketRepo.save(duplicate);
        historyService.record(duplicate, "MARKED_DUPLICATE", "Duplicate of #" + duplicateOfId, performedBy);
        long similarCount = findSimilarTickets(duplicateOfId).size() + 1;
        if (similarCount >= 3) {
            notificationService.notifyDuplicatePattern(original, "admin@swiftsort.dev", (int) similarCount);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<TicketResponse> getAgentQueue(Long agentId) {
        return ticketRepo.findAgentQueue(agentId).stream()
                .map(t -> toResponse(t, slaService)).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TicketHistory> getTicketHistory(Long id) {
        return historyService.getHistory(id);
    }

    @Override
    public Map<String, Object> uploadAttachment(Long ticketId, MultipartFile file) throws IOException {
        Ticket t = findOrThrow(ticketId);
        var attachment = attachmentService.upload(t, file);
        auditService.log("UPLOAD", "ATTACHMENT", attachment.getId(), "system", attachment.getFileName());
        Map<String, Object> result = new HashMap<>();
        result.put("id", attachment.getId());
        result.put("fileName", attachment.getFileName());
        result.put("fileSize", attachment.getFileSize());
        return result;
    }

    private Ticket findOrThrow(Long id) {
        return ticketRepo.findById(id).orElseThrow(() -> new EntityNotFoundException("Ticket not found: " + id));
    }

    static TicketResponse toResponse(Ticket t, SLAService slaService) {
        TicketResponse r = new TicketResponse();
        r.setId(t.getId());
        r.setSubject(t.getSubject());
        r.setMessage(t.getMessage());
        r.setMaskedMessage(t.getMaskedMessage());
        r.setCustomerReference(t.getCustomerReference());
        r.setProduct(t.getProduct());
        r.setSource(t.getSource() != null ? t.getSource().name() : null);
        r.setCategory(t.getCategory() != null ? t.getCategory().name() : null);
        r.setPriority(t.getPriority() != null ? t.getPriority().name() : null);
        r.setStatus(t.getStatus() != null ? t.getStatus().name() : null);
        r.setAssignedTeam(t.getAssignedTeam());
        r.setRecommendedTeam(t.getRecommendedTeam());
        if (t.getAssignedAgent() != null) {
            r.setAssignedAgent(t.getAssignedAgent().getName());
            r.setAssignedAgentId(t.getAssignedAgent().getId());
        }
        r.setSentiment(t.getSentiment() != null ? t.getSentiment().name() : null);
        r.setUrgency(t.getUrgency() != null ? t.getUrgency().name() : null);
        r.setSummary(t.getSummary());
        r.setConfidence(t.getConfidence());
        r.setSuggestedResponse(t.getSuggestedResponse());
        r.setResponseTone(t.getResponseTone() != null ? t.getResponseTone().name() : null);
        r.setResponseApproved(t.getResponseApproved());
        r.setResponseSent(t.getResponseSent());
        r.setResolutionNotes(t.getResolutionNotes());
        r.setTags(t.getTags());
        r.setAiModelName(t.getAiModelName());
        r.setSlaDeadline(t.getSlaDeadline());
        r.setSlaMinutesRemaining(slaService.getMinutesRemaining(t));
        r.setSlaOverdue(slaService.isOverdue(t));
        r.setCreatedAt(t.getCreatedAt());
        r.setUpdatedAt(t.getUpdatedAt());
        if (t.getDuplicateOf() != null) r.setDuplicateOfId(t.getDuplicateOf().getId());
        return r;
    }

    private <E extends Enum<E>> E parseEnum(String value, Class<E> enumClass) {
        if (value == null || value.isBlank()) return null;
        try { return Enum.valueOf(enumClass, value.toUpperCase()); } catch (IllegalArgumentException ex) { return null; }
    }

    private String blankToNull(String s) {
        return (s == null || s.isBlank()) ? null : s;
    }
}
