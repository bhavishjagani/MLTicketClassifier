package com.SwiftSort.MLTicketClassifier.ai;

import com.SwiftSort.MLTicketClassifier.dto.KbSearchResult;
import com.SwiftSort.MLTicketClassifier.model.Ticket;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Pattern;

@Service
public class RuleBasedAIService implements AIService {

    private static final String MODEL_NAME = "rule-based-v1";
    private static final String PROMPT_VERSION = "1.0.0";

    private static final List<String> POSITIVE_WORDS = List.of("love", "great", "awesome", "excellent", "happy", "satisfied", "thank");
    private static final List<String> NEGATIVE_WORDS = List.of("bad", "terrible", "awful", "disappointed", "frustrated", "angry", "annoyed", "hate");
    private static final List<String> ANGRY_WORDS = List.of("furious", "outraged", "unacceptable", "ridiculous");
    private static final List<String> CONFUSED_WORDS = List.of("confused", "unclear", "don't understand", "how do i");
    private static final List<String> URGENT_WORDS = List.of("urgent", "asap", "immediately", "critical", "blocked", "cannot", "unable", "deducted", "cancel");

    private static final Map<String, Ticket.Category> CATEGORY_KEYWORDS = Map.ofEntries(
            Map.entry("payment|bill|invoice|charge|deducted", Ticket.Category.PAYMENT_ISSUE),
            Map.entry("login|sign in|access|password|account locked", Ticket.Category.LOGIN_ISSUE),
            Map.entry("account|profile|settings", Ticket.Category.ACCOUNT_ISSUE),
            Map.entry("bug|error|crash|freeze|not working|fault", Ticket.Category.TECHNICAL_BUG),
            Map.entry("feature|suggestion|improve|new idea|recorded class", Ticket.Category.FEATURE_REQUEST),
            Map.entry("subscription|plan|upgrade|downgrade|activate", Ticket.Category.SUBSCRIPTION_ISSUE),
            Map.entry("booking|tutor|class|schedule", Ticket.Category.TUTOR_BOOKING_ISSUE),
            Map.entry("course|lesson|module", Ticket.Category.COURSE_RELATED_QUESTION),
            Map.entry("tutor feedback|teacher|instructor", Ticket.Category.TUTOR_FEEDBACK),
            Map.entry("complaint|bad service|rude", Ticket.Category.COMPLAINT),
            Map.entry("security|hack|breach", Ticket.Category.SECURITY_ISSUE),
            Map.entry("refund|money back", Ticket.Category.REFUND_REQUEST)
    );

    private static final Map<String, Ticket.Priority> PRIORITY_KEYWORDS = Map.of(
            "urgent|critical|asap|blocked|emergency|deducted|security", Ticket.Priority.CRITICAL,
            "high|important|major|severe|frustrated", Ticket.Priority.HIGH,
            "medium|moderate|normal", Ticket.Priority.MEDIUM,
            "low|minor|small", Ticket.Priority.LOW
    );

    private static final Map<Ticket.Category, String> TEAM_MAP = Map.ofEntries(
            Map.entry(Ticket.Category.PAYMENT_ISSUE, "BILLING"),
            Map.entry(Ticket.Category.SUBSCRIPTION_ISSUE, "BILLING"),
            Map.entry(Ticket.Category.REFUND_REQUEST, "BILLING"),
            Map.entry(Ticket.Category.TECHNICAL_BUG, "TECHNICAL"),
            Map.entry(Ticket.Category.LOGIN_ISSUE, "TECHNICAL"),
            Map.entry(Ticket.Category.SECURITY_ISSUE, "SECURITY"),
            Map.entry(Ticket.Category.FEATURE_REQUEST, "PRODUCT"),
            Map.entry(Ticket.Category.TUTOR_BOOKING_ISSUE, "TUTOR_OPERATIONS"),
            Map.entry(Ticket.Category.CLASS_SCHEDULING_ISSUE, "TUTOR_OPERATIONS"),
            Map.entry(Ticket.Category.COMPLAINT, "ADMINISTRATION")
    );

    private final RAGService ragService;

    public RuleBasedAIService(RAGService ragService) {
        this.ragService = ragService;
    }

    @Override
    public AIClassificationResult classifyTicket(Ticket ticket) {
        long start = System.currentTimeMillis();
        AIClassificationResult result = new AIClassificationResult();
        result.setModelName(MODEL_NAME);
        result.setPromptVersion(PROMPT_VERSION);

        String subject = ticket.getSubject() != null ? ticket.getSubject() : "";
        String message = ticket.getMessage() != null ? ticket.getMessage() : "";
        String text = (subject + " " + message).toLowerCase(Locale.ROOT);

        Ticket.Category category = detectCategory(text);
        Ticket.Priority priority = detectPriority(text);
        Ticket.Sentiment sentiment = detectSentiment(text);
        Ticket.Urgency urgency = detectUrgency(text, priority);
        String team = TEAM_MAP.getOrDefault(category, "SUPPORT");
        List<String> tags = detectTags(text, category);

        result.setCategory(category);
        result.setPriority(priority);
        result.setSentiment(sentiment);
        result.setUrgency(urgency);
        result.setRecommendedTeam(team);
        result.setSuggestedTags(tags);
        result.setSummary(summarize(message));
        result.setSuggestedResponse(generateSuggestedResponse(ticket, Ticket.ResponseTone.PROFESSIONAL));
        result.setConfidence(0.85 + new Random().nextDouble() * 0.1);
        result.setValidationPassed(isValid(result));
        result.setResponseTimeMs(System.currentTimeMillis() - start);
        return result;
    }

    @Override
    public String summarize(String text) {
        if (text == null || text.isBlank()) {
            return "";
        }
        if (text.length() < 60) {
            return text;
        }
        String[] sentences = text.split("(?<=[.!?])\\s+");
        if (sentences.length <= 2) {
            return text;
        }
        return String.join(" ", Arrays.copyOf(sentences, 2)) + " ...";
    }

    @Override
    public String generateSuggestedResponse(Ticket ticket, Ticket.ResponseTone tone) {
        Ticket.Category category = ticket.getCategory() != null ? ticket.getCategory() : Ticket.Category.GENERAL_ENQUIRY;
        String base = switch (category) {
            case PAYMENT_ISSUE -> "We understand you're facing a payment issue. Our billing team will verify the transaction and get back to you shortly.";
            case LOGIN_ISSUE -> "We're sorry you're having trouble logging in. Please reset your password or contact support for assistance.";
            case TECHNICAL_BUG -> "Thank you for reporting this bug. Our engineering team has been notified and will investigate.";
            case FEATURE_REQUEST -> "We appreciate your suggestion. It has been added to our product backlog for review.";
            case SUBSCRIPTION_ISSUE -> "We'll check your subscription status and ensure everything is activated correctly.";
            case COMPLAINT -> "We apologise for the inconvenience. A senior agent will personally review your case.";
            case REFUND_REQUEST -> "We have received your refund request. Our finance team will process it within 5 business days.";
            case SECURITY_ISSUE -> "This is a security concern. We have escalated it to our security team for immediate action.";
            case TUTOR_BOOKING_ISSUE -> "We will review your booking details and confirm the tutor assignment as soon as possible.";
            default -> "Thank you for reaching out. We'll review your request and respond as soon as possible.";
        };

        KbSearchResult kb = ragService.findBestMatch(ticket.getSubject() + " " + ticket.getMessage());
        if (kb != null && kb.getSnippet() != null) {
            base += " Based on our knowledge base (" + kb.getDocumentRef() + "): " + kb.getSnippet();
        }

        String greeting = switch (tone != null ? tone : Ticket.ResponseTone.PROFESSIONAL) {
            case FRIENDLY -> "Hi there!\n\n";
            case APOLOGETIC -> "Dear customer,\n\nWe sincerely apologise for the trouble you've experienced.\n\n";
            case SIMPLE -> "Hello,\n\n";
            case CONCISE -> "";
            case REASSURING -> "Dear customer,\n\nPlease rest assured we are here to help.\n\n";
            case FORMAL -> "Dear valued customer,\n\n";
            default -> "Dear customer,\n\n";
        };

        String closing = tone == Ticket.ResponseTone.CONCISE ? "" : "\n\nBest regards,\nSupport Team";
        return greeting + base + closing;
    }

    @Override
    public String generateWeeklyReport(String dataSummary) {
        if (dataSummary == null || dataSummary.isBlank()) {
            return "No ticket data available for this week.";
        }
        return "Weekly Founder Report\n\n" + dataSummary;
    }

    private Ticket.Category detectCategory(String text) {
        for (Map.Entry<String, Ticket.Category> entry : CATEGORY_KEYWORDS.entrySet()) {
            if (Pattern.compile(entry.getKey()).matcher(text).find()) {
                return entry.getValue();
            }
        }
        return Ticket.Category.GENERAL_ENQUIRY;
    }

    private Ticket.Priority detectPriority(String text) {
        for (Map.Entry<String, Ticket.Priority> entry : PRIORITY_KEYWORDS.entrySet()) {
            if (Pattern.compile(entry.getKey()).matcher(text).find()) {
                return entry.getValue();
            }
        }
        return Ticket.Priority.MEDIUM;
    }

    private Ticket.Sentiment detectSentiment(String text) {
        if (ANGRY_WORDS.stream().anyMatch(text::contains)) {
            return Ticket.Sentiment.ANGRY;
        }
        if (CONFUSED_WORDS.stream().anyMatch(text::contains)) {
            return Ticket.Sentiment.CONFUSED;
        }
        long pos = POSITIVE_WORDS.stream().filter(text::contains).count();
        long neg = NEGATIVE_WORDS.stream().filter(text::contains).count();
        if (URGENT_WORDS.stream().anyMatch(text::contains) && neg > 0) {
            return Ticket.Sentiment.URGENT;
        }
        if (neg > pos && neg > 0) {
            return text.contains("frustrated") ? Ticket.Sentiment.FRUSTRATED : Ticket.Sentiment.NEGATIVE;
        }
        if (pos > neg) {
            return Ticket.Sentiment.POSITIVE;
        }
        return Ticket.Sentiment.NEUTRAL;
    }

    private Ticket.Urgency detectUrgency(String text, Ticket.Priority priority) {
        if (priority == Ticket.Priority.CRITICAL) {
            return Ticket.Urgency.CRITICAL;
        }
        if (URGENT_WORDS.stream().anyMatch(text::contains)) {
            return Ticket.Urgency.HIGH;
        }
        return Ticket.Urgency.MEDIUM;
    }

    private List<String> detectTags(String text, Ticket.Category category) {
        List<String> tags = new ArrayList<>();
        tags.add(category.name().toLowerCase(Locale.ROOT));
        if (text.contains("refund")) tags.add("refund");
        if (text.contains("payment")) tags.add("payment");
        if (text.contains("subscription")) tags.add("subscription");
        return tags;
    }

    private boolean isValid(AIClassificationResult result) {
        return result.getCategory() != null
                && result.getPriority() != null
                && result.getSentiment() != null
                && result.getConfidence() >= 0 && result.getConfidence() <= 1.0;
    }
}
