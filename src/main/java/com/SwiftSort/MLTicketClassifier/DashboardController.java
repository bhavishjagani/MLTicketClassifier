package com.SwiftSort.MLTicketClassifier;

import com.SwiftSort.MLTicketClassifier.dto.DashboardStats;
import com.SwiftSort.MLTicketClassifier.dto.TicketResponse;
import com.SwiftSort.MLTicketClassifier.service.TicketService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class DashboardController {

    private final TicketService ticketService;

    public DashboardController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @GetMapping("/dashboard")
    public String dashboard() {
        return "layout :: layout(view='dashboard')";
    }

    @GetMapping("/api/dashboard/stats")
    @ResponseBody
    public DashboardStats getStats() {
        return ticketService.getDashboardStats();
    }

    @GetMapping("/api/dashboard/recent")
    @ResponseBody
    public List<TicketResponse> recentTickets(@RequestParam(defaultValue = "5") int limit) {
        return ticketService.getRecentTickets(limit);
    }
}