package com.SwiftSort.MLTicketClassifier.service;

import com.SwiftSort.MLTicketClassifier.dto.AnalyticsOverview;
import com.SwiftSort.MLTicketClassifier.repository.TicketRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@Transactional(readOnly = true)
public class AnalyticsServiceImpl implements AnalyticsService {

    private final TicketRepository ticketRepo;

    public AnalyticsServiceImpl(TicketRepository ticketRepo) {
        this.ticketRepo = ticketRepo;
    }

    @Override
    public AnalyticsOverview getOverview() {
        long total = ticketRepo.count();
        Double avgHours = ticketRepo.avgResolutionHours();
        long reopened = ticketRepo.countReopened();
        long negative = ticketRepo.countNegativeSentiment();
        long slaBreaches = ticketRepo.findOpenTickets().stream()
                .filter(t -> t.getSlaDeadline() != null && LocalDate.now().atStartOfDay().isAfter(t.getSlaDeadline().toLocalDate().atStartOfDay())
                        && t.getFirstResponseAt() == null)
                .count();

        double reopenRate = total > 0 ? (double) reopened / total : 0.0;
        double negPct = total > 0 ? (double) negative / total : 0.0;

        return new AnalyticsOverview(
                avgHours != null ? avgHours : 0.0,
                reopenRate,
                negPct,
                (int) slaBreaches
        );
    }

    @Override
    public Map<String, Long> getCategoryDistribution() {
        return ticketRepo.countByCategory().stream()
                .collect(java.util.stream.Collectors.toMap(
                        arr -> arr[0] == null ? "UNKNOWN" : arr[0].toString(),
                        arr -> (Long) arr[1]
                ));
    }

    @Override
    public Map<String, Long> getPriorityDistribution() {
        return ticketRepo.countByPriorityGrouped().stream()
                .collect(java.util.stream.Collectors.toMap(
                        arr -> arr[0].toString(),
                        arr -> (Long) arr[1]
                ));
    }

    @Override
    public List<Map<String, Object>> getSentimentTrend() {
        List<Object[]> raw = ticketRepo.sentimentTrendRaw();
        Map<String, Map<String, Long>> weekMap = new LinkedHashMap<>();
        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE;
        for (Object[] row : raw) {
            LocalDate week = toLocalDate(row[0]);
            if (week == null) continue;
            String sentiment = row[1] != null ? row[1].toString() : "NEUTRAL";
            Long count = row[2] instanceof Long l ? l : Long.parseLong(row[2].toString());
            String key = week.format(formatter);
            weekMap.computeIfAbsent(key, k -> new HashMap<>()).put(sentiment, count);
        }
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map.Entry<String, Map<String, Long>> entry : weekMap.entrySet()) {
            Map<String, Object> point = new LinkedHashMap<>();
            point.put("week", entry.getKey());
            Map<String, Long> counts = entry.getValue();
            point.put("POSITIVE", counts.getOrDefault("POSITIVE", 0L));
            point.put("NEGATIVE", counts.getOrDefault("NEGATIVE", 0L));
            point.put("NEUTRAL", counts.getOrDefault("NEUTRAL", 0L));
            result.add(point);
        }
        return result;
    }

    @Override
    public List<Map<String, Object>> getTicketTrend() {
        List<Object[]> raw = ticketRepo.countByDay();
        List<Map<String, Object>> result = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE;
        for (Object[] row : raw) {
            LocalDate date = toLocalDate(row[0]);
            if (date == null) continue;
            Map<String, Object> point = new LinkedHashMap<>();
            point.put("date", date.format(formatter));
            point.put("count", row[1] instanceof Long l ? l : Long.parseLong(row[1].toString()));
            result.add(point);
        }
        return result;
    }

    private LocalDate toLocalDate(Object value) {
        if (value == null) return null;
        if (value instanceof java.sql.Date d) return d.toLocalDate();
        if (value instanceof Timestamp t) return t.toLocalDateTime().toLocalDate();
        if (value instanceof LocalDate ld) return ld;
        return LocalDate.parse(value.toString().substring(0, 10));
    }
}
