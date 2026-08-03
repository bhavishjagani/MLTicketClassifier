package com.SwiftSort.MLTicketClassifier;

import com.SwiftSort.MLTicketClassifier.dto.*;
import com.SwiftSort.MLTicketClassifier.model.TicketHistory;
import com.SwiftSort.MLTicketClassifier.service.TicketService;
import org.springframework.data.domain.*;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/tickets")
public class TicketController {

    private final TicketService ticketService;

    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @GetMapping
    public String listPage() {
        return "layout :: layout(view='tickets')";
    }

    @GetMapping("/new")
    public String newTicketPage() {
        return "layout :: layout(view='new-ticket')";
    }

    @GetMapping("/{id}")
    public String detailPage() {
        return "layout :: layout(view='ticket-details')";
    }

    @GetMapping("/api/tickets")
    @ResponseBody
    public Page<TicketResponse> getTickets(@RequestParam(defaultValue = "0") int page,
                                           @RequestParam(defaultValue = "10") int size,
                                           @RequestParam(required = false) String search,
                                           @RequestParam(required = false) String status,
                                           @RequestParam(required = false) String priority,
                                           @RequestParam(required = false) String category,
                                           @RequestParam(required = false) String product,
                                           @RequestParam(required = false) String sentiment,
                                           @RequestParam(required = false) Long agentId,
                                           @RequestParam(required = false) String team,
                                           @RequestParam(required = false) String sort) {
        Sort s = Sort.by(Sort.Direction.DESC, "createdAt");
        if ("oldest".equals(sort)) s = Sort.by(Sort.Direction.ASC, "createdAt");
        if ("priority".equals(sort)) s = Sort.by(Sort.Direction.DESC, "priority");
        return ticketService.findAll(PageRequest.of(page, size, s), search, status, priority, category, product, sentiment, agentId, team);
    }

    @GetMapping("/api/tickets/{id}")
    @ResponseBody
    public TicketResponse getTicket(@PathVariable Long id) {
        return ticketService.getTicketById(id);
    }

    @PostMapping("/api/tickets")
    @ResponseBody
    public TicketResponse createTicket(@RequestBody TicketCreateRequest request) {
        return ticketService.createTicket(request);
    }

    @PostMapping("/api/tickets/import")
    @ResponseBody
    public List<TicketResponse> importCsv(@RequestParam("file") MultipartFile file) throws IOException {
        return ticketService.importFromCsv(file);
    }

    @PatchMapping("/api/tickets/{id}/status")
    @ResponseBody
    public void updateStatus(@PathVariable Long id, @RequestBody StatusUpdateRequest request, Authentication auth) {
        ticketService.updateStatus(id, request.getStatus(), auth != null ? auth.getName() : "system");
    }

    @PatchMapping("/api/tickets/{id}/priority")
    @ResponseBody
    public void updatePriority(@PathVariable Long id, @RequestBody PriorityUpdateRequest request, Authentication auth) {
        ticketService.updatePriority(id, request.getPriority(), auth != null ? auth.getName() : "system");
    }

    @PatchMapping("/api/tickets/{id}/assign")
    @ResponseBody
    public void assignAgent(@PathVariable Long id, @RequestBody AssignRequest request, Authentication auth) {
        ticketService.assignAgent(id, request.getAgentId(), auth != null ? auth.getName() : "system");
    }

    @PatchMapping("/api/tickets/{id}/team")
    @ResponseBody
    public void assignTeam(@PathVariable Long id, @RequestBody Map<String, String> body, Authentication auth) {
        ticketService.assignTeam(id, body.get("team"), auth != null ? auth.getName() : "system");
    }

    @PostMapping("/api/tickets/{id}/comments")
    @ResponseBody
    public void addComment(@PathVariable Long id, @RequestBody CommentRequest request, Authentication auth) {
        ticketService.addComment(id, request.getText(), request.isInternal(), auth != null ? auth.getName() : null);
    }

    @GetMapping("/api/tickets/{id}/comments")
    @ResponseBody
    public List<CommentResponse> getComments(@PathVariable Long id, @RequestParam(defaultValue = "false") boolean includeInternal) {
        return ticketService.getComments(id, includeInternal);
    }

    @PostMapping("/api/tickets/{id}/response")
    @ResponseBody
    public void sendResponse(@PathVariable Long id, @RequestBody ResponseRequest request, Authentication auth) {
        ticketService.sendResponse(id, request.getResponse(), auth != null ? auth.getName() : "system");
    }

    @PostMapping("/api/tickets/{id}/response/approve")
    @ResponseBody
    public void approveResponse(@PathVariable Long id, Authentication auth) {
        ticketService.approveResponse(id, auth != null ? auth.getName() : "system");
    }

    @PostMapping("/api/tickets/{id}/response/reject")
    @ResponseBody
    public void rejectResponse(@PathVariable Long id) {
        ticketService.rejectResponse(id);
    }

    @PostMapping("/api/tickets/{id}/response/regenerate")
    @ResponseBody
    public Map<String, String> regenerateResponse(@PathVariable Long id, @RequestBody(required = false) Map<String, String> body) {
        String tone = body != null ? body.get("tone") : null;
        String response = ticketService.regenerateResponse(id, tone);
        return Map.of("response", response);
    }

    @PatchMapping("/api/tickets/{id}/resolution")
    @ResponseBody
    public void updateResolution(@PathVariable Long id, @RequestBody Map<String, String> body) {
        ticketService.updateResolutionNotes(id, body.get("notes"));
    }

    @GetMapping("/api/tickets/{id}/similar")
    @ResponseBody
    public List<TicketResponse> getSimilarTickets(@PathVariable Long id) {
        return ticketService.findSimilarTickets(id);
    }

    @PostMapping("/api/tickets/{id}/duplicate")
    @ResponseBody
    public void markDuplicate(@PathVariable Long id, @RequestBody Map<String, Long> body, Authentication auth) {
        ticketService.markDuplicate(id, body.get("duplicateOf"), auth != null ? auth.getName() : "system");
    }

    @GetMapping("/api/tickets/{id}/history")
    @ResponseBody
    public List<TicketHistory> getHistory(@PathVariable Long id) {
        return ticketService.getTicketHistory(id);
    }

    @GetMapping("/api/tickets/queue/{agentId}")
    @ResponseBody
    public List<TicketResponse> getAgentQueue(@PathVariable Long agentId) {
        return ticketService.getAgentQueue(agentId);
    }

    @PostMapping("/api/tickets/{id}/attachments")
    @ResponseBody
    public Map<String, Object> uploadAttachment(@PathVariable Long id, @RequestParam("file") MultipartFile file) throws IOException {
        return ticketService.uploadAttachment(id, file);
    }
}
