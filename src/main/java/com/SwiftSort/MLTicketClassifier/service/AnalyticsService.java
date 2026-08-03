package com.SwiftSort.MLTicketClassifier.service;

import com.SwiftSort.MLTicketClassifier.dto.AnalyticsOverview;

import java.util.List;
import java.util.Map;

public interface AnalyticsService {
    AnalyticsOverview getOverview();
    Map<String, Long> getCategoryDistribution();
    Map<String, Long> getPriorityDistribution();
    List<Map<String, Object>> getSentimentTrend();
    List<Map<String, Object>> getTicketTrend();
}