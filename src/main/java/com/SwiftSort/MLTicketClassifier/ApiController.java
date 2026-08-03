package com.SwiftSort.MLTicketClassifier;

import com.SwiftSort.MLTicketClassifier.model.AuditLog;
import com.SwiftSort.MLTicketClassifier.model.Notification;
import com.SwiftSort.MLTicketClassifier.repository.AuditLogRepository;
import com.SwiftSort.MLTicketClassifier.service.NotificationService;
import com.SwiftSort.MLTicketClassifier.service.ReportService;
import com.SwiftSort.MLTicketClassifier.service.SLAService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ApiController {

    private final NotificationService notificationService;
    private final ReportService reportService;
    private final SLAService slaService;
    private final AuditLogRepository auditLogRepo;

    public ApiController(NotificationService notificationService, ReportService reportService,
                         SLAService slaService, AuditLogRepository auditLogRepo) {
        this.notificationService = notificationService;
        this.reportService = reportService;
        this.slaService = slaService;
        this.auditLogRepo = auditLogRepo;
    }

    @GetMapping("/notifications")
    public List<Notification> getNotifications(Authentication auth) {
        return notificationService.getForUser(auth.getName());
    }

    @GetMapping("/notifications/unread")
    public List<Notification> getUnread(Authentication auth) {
        return notificationService.getUnread(auth.getName());
    }

    @PatchMapping("/notifications/{id}/read")
    public void markRead(@PathVariable Long id) {
        notificationService.markRead(id);
    }

    @GetMapping("/reports/weekly")
    public Map<String, String> weeklyReport() {
        return Map.of("report", reportService.getWeeklyFounderReport());
    }

    @GetMapping("/reports/feature-requests")
    public List<Map<String, Object>> featureRequests() {
        return reportService.getFeatureRequestAnalytics();
    }

    @GetMapping("/reports/bugs")
    public List<Map<String, Object>> bugIntelligence() {
        return reportService.getBugIntelligence();
    }

    @GetMapping("/reports/product-comparison")
    public List<Map<String, Object>> productComparison() {
        return reportService.getProductComparison();
    }

    @GetMapping("/sla/report")
    public Map<String, Object> slaReport() {
        return slaService.getSlaReport();
    }

    @GetMapping("/audit-logs")
    public List<AuditLog> auditLogs(@RequestParam(defaultValue = "7") int days) {
        return auditLogRepo.findRecent(LocalDateTime.now().minusDays(days));
    }
}
