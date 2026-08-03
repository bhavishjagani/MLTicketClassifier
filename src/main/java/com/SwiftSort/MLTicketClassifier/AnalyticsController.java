package com.SwiftSort.MLTicketClassifier;

import com.SwiftSort.MLTicketClassifier.dto.AnalyticsOverview;
import com.SwiftSort.MLTicketClassifier.service.AnalyticsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.Map;

@Controller
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping("/analytics")
    public String analyticsPage() {
        return "layout :: layout(view='analytics')";
    }

    @GetMapping("/api/analytics/overview")
    @ResponseBody
    public AnalyticsOverview getOverview() {
        return analyticsService.getOverview();
    }

    @GetMapping("/api/analytics/category-distribution")
    @ResponseBody
    public Map<String, Long> getCategoryDistribution() {
        return analyticsService.getCategoryDistribution();
    }

    @GetMapping("/api/analytics/priority-distribution")
    @ResponseBody
    public Map<String, Long> getPriorityDistribution() {
        return analyticsService.getPriorityDistribution();
    }

    @GetMapping("/api/analytics/sentiment-trend")
    @ResponseBody
    public List<Map<String, Object>> getSentimentTrend() {
        return analyticsService.getSentimentTrend();
    }

    @GetMapping("/api/analytics/ticket-trend")
    @ResponseBody
    public List<Map<String, Object>> getTicketTrend() {
        return analyticsService.getTicketTrend();
    }
}