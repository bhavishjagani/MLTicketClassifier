package com.SwiftSort.MLTicketClassifier.service;

import com.SwiftSort.MLTicketClassifier.ai.AIService;
import com.SwiftSort.MLTicketClassifier.model.Ticket;
import com.SwiftSort.MLTicketClassifier.repository.TicketRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class ReportService {

    private final TicketRepository ticketRepo;
    private final AIService aiService;

    public ReportService(TicketRepository ticketRepo, AIService aiService) {
        this.ticketRepo = ticketRepo;
        this.aiService = aiService;
    }

    public List<Map<String, Object>> getFeatureRequestAnalytics() {
        List<Ticket> features = ticketRepo.findByCategory(Ticket.Category.FEATURE_REQUEST);
        Map<String, List<Ticket>> grouped = new HashMap<>();
        for (Ticket t : features) {
            String key = normalizeFeature(t.getSubject() + " " + t.getMessage());
            grouped.computeIfAbsent(key, k -> new ArrayList<>()).add(t);
        }
        return grouped.entrySet().stream()
                .map(e -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("feature", e.getKey());
                    m.put("count", e.getValue().size());
                    m.put("sentiment", dominantSentiment(e.getValue()));
                    return m;
                })
                .sorted((a, b) -> Integer.compare((int) b.get("count"), (int) a.get("count")))
                .limit(20)
                .collect(Collectors.toList());
    }

    public List<Map<String, Object>> getBugIntelligence() {
        List<Ticket> bugs = ticketRepo.findByCategory(Ticket.Category.TECHNICAL_BUG);
        Map<String, List<Ticket>> grouped = new HashMap<>();
        for (Ticket t : bugs) {
            String key = normalizeFeature(t.getSubject());
            grouped.computeIfAbsent(key, k -> new ArrayList<>()).add(t);
        }
        return grouped.entrySet().stream()
                .map(e -> {
                    List<Ticket> list = e.getValue();
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("bug", e.getKey());
                    m.put("affectedCustomers", list.size());
                    m.put("firstReported", list.stream().map(Ticket::getCreatedAt).min(java.util.Comparator.naturalOrder()).orElse(null));
                    m.put("latestOccurrence", list.stream().map(Ticket::getCreatedAt).max(java.util.Comparator.naturalOrder()).orElse(null));
                    m.put("product", list.stream().map(Ticket::getProduct).filter(Objects::nonNull).findFirst().orElse("Unknown"));
                    return m;
                })
                .sorted((a, b) -> Integer.compare((int) b.get("affectedCustomers"), (int) a.get("affectedCustomers")))
                .collect(Collectors.toList());
    }

    public String getWeeklyFounderReport() {
        long total = ticketRepo.count();
        Map<String, Long> categories = ticketRepo.countByCategory().stream()
                .collect(Collectors.toMap(arr -> arr[0] == null ? "UNKNOWN" : arr[0].toString(), arr -> (Long) arr[1]));
        String topCategory = categories.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("N/A");
        long featureRequests = ticketRepo.countByCategoryEnum(Ticket.Category.FEATURE_REQUEST);
        long paymentIssues = ticketRepo.countByCategoryEnum(Ticket.Category.PAYMENT_ISSUE);
        long negative = ticketRepo.countNegativeSentiment();
        double negPct = total > 0 ? (double) negative / total * 100 : 0;

        String summary = String.format(
                "This week, %d tickets were received. The most common issue was %s. %d users submitted feature requests, and %d users reported payment problems. Negative sentiment is at %.1f%%.",
                total, topCategory.replace('_', ' ').toLowerCase(), featureRequests, paymentIssues, negPct);
        return aiService.generateWeeklyReport(summary);
    }

    public List<Map<String, Object>> getProductComparison() {
        List<String> products = List.of("ECLTalk", "NexGSchool", "VCDoc");
        List<Map<String, Object>> result = new ArrayList<>();
        for (String product : products) {
            List<Ticket> tickets = ticketRepo.findByProduct(product);
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("product", product);
            m.put("ticketCount", tickets.size());
            m.put("criticalCount", tickets.stream().filter(t -> t.getPriority() == Ticket.Priority.CRITICAL).count());
            m.put("negativeSentiment", tickets.stream().filter(t ->
                    t.getSentiment() == Ticket.Sentiment.NEGATIVE
                            || t.getSentiment() == Ticket.Sentiment.ANGRY
                            || t.getSentiment() == Ticket.Sentiment.FRUSTRATED).count());
            m.put("featureRequests", tickets.stream().filter(t -> t.getCategory() == Ticket.Category.FEATURE_REQUEST).count());
            result.add(m);
        }
        return result;
    }

    private String normalizeFeature(String text) {
        if (text == null) return "unknown";
        return text.toLowerCase(Locale.ROOT).replaceAll("\\W+", " ").trim();
    }

    private String dominantSentiment(List<Ticket> tickets) {
        return tickets.stream()
                .map(t -> t.getSentiment() != null ? t.getSentiment().name() : "NEUTRAL")
                .collect(Collectors.groupingBy(s -> s, Collectors.counting()))
                .entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("NEUTRAL");
    }
}
