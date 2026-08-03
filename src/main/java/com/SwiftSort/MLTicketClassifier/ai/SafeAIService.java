package com.SwiftSort.MLTicketClassifier.ai;

import com.SwiftSort.MLTicketClassifier.model.AIAnalysisLog;
import com.SwiftSort.MLTicketClassifier.model.Ticket;
import com.SwiftSort.MLTicketClassifier.repository.AIAnalysisLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.*;

@Service
@Primary
public class SafeAIService implements AIService {

    private static final Logger log = LoggerFactory.getLogger(SafeAIService.class);

    private final RuleBasedAIService delegate;
    private final SensitiveDataMasker masker;
    private final PromptInjectionDetector injectionDetector;
    private final EmbeddingService embeddingService;
    private final AIAnalysisLogRepository analysisLogRepo;

    @Value("${app.ai.enabled:true}")
    private boolean aiEnabled;

    @Value("${app.ai.max-input-length:10000}")
    private int maxInputLength;

    @Value("${app.ai.timeout-ms:5000}")
    private long timeoutMs;

    public SafeAIService(RuleBasedAIService delegate, SensitiveDataMasker masker,
                           PromptInjectionDetector injectionDetector, EmbeddingService embeddingService,
                           AIAnalysisLogRepository analysisLogRepo) {
        this.delegate = delegate;
        this.masker = masker;
        this.injectionDetector = injectionDetector;
        this.embeddingService = embeddingService;
        this.analysisLogRepo = analysisLogRepo;
    }

    @Override
    public AIClassificationResult classifyTicket(Ticket ticket) {
        AIClassificationResult fallback = createFallback();
        if (!aiEnabled) {
            applyToTicket(ticket, fallback);
            return fallback;
        }
        try {
            prepareTicketInput(ticket);
            AIClassificationResult result = executeWithTimeout(() -> delegate.classifyTicket(ticket));
            if (result == null || !result.isValidationPassed()) {
                result = fallback;
            }
            applyToTicket(ticket, result);
            ticket.setEmbedding(embeddingService.toStorageString(
                    embeddingService.embed(ticket.getSubject() + " " + ticket.getMaskedMessage())));
            logAnalysis(ticket.getId(), result, true, null);
            return result;
        } catch (Exception ex) {
            log.error("AI classification failed for ticket {}: {}", ticket.getId(), ex.getMessage());
            applyToTicket(ticket, fallback);
            logAnalysis(ticket.getId(), fallback, false, ex.getMessage());
            return fallback;
        }
    }

    @Override
    public String summarize(String text) {
        try {
            String sanitized = sanitizeInput(text);
            return executeWithTimeout(() -> delegate.summarize(sanitized));
        } catch (Exception ex) {
            log.error("AI summarization failed: {}", ex.getMessage());
            return text != null && text.length() > 120 ? text.substring(0, 120) + "..." : text;
        }
    }

    @Override
    public String generateSuggestedResponse(Ticket ticket, Ticket.ResponseTone tone) {
        try {
            prepareTicketInput(ticket);
            return executeWithTimeout(() -> delegate.generateSuggestedResponse(ticket, tone));
        } catch (Exception ex) {
            log.error("AI response generation failed: {}", ex.getMessage());
            return "Thank you for contacting us. A support agent will review your ticket and respond shortly.";
        }
    }

    @Override
    public String generateWeeklyReport(String dataSummary) {
        try {
            return executeWithTimeout(() -> delegate.generateWeeklyReport(dataSummary));
        } catch (Exception ex) {
            log.error("AI weekly report failed: {}", ex.getMessage());
            return "Weekly report could not be generated. Please review analytics dashboard.";
        }
    }

    private void prepareTicketInput(Ticket ticket) {
        if (ticket.getMessage() != null && ticket.getMessage().length() > maxInputLength) {
            ticket.setMessage(ticket.getMessage().substring(0, maxInputLength));
        }
        if (injectionDetector.isSuspicious(ticket.getMessage())) {
            ticket.setMessage(injectionDetector.sanitize(ticket.getMessage()));
        }
        if (injectionDetector.isSuspicious(ticket.getSubject())) {
            ticket.setSubject(injectionDetector.sanitize(ticket.getSubject()));
        }
        ticket.setMaskedMessage(masker.mask(ticket.getMessage()));
    }

    private void applyToTicket(Ticket ticket, AIClassificationResult result) {
        ticket.setCategory(result.getCategory());
        ticket.setPriority(result.getPriority());
        ticket.setSentiment(result.getSentiment());
        ticket.setUrgency(result.getUrgency());
        ticket.setRecommendedTeam(result.getRecommendedTeam());
        ticket.setAssignedTeam(result.getRecommendedTeam());
        ticket.setTags(result.getSuggestedTags() != null ? result.getSuggestedTags() : List.of());
        ticket.setSummary(result.getSummary());
        ticket.setSuggestedResponse(result.getSuggestedResponse());
        ticket.setConfidence(result.getConfidence());
        ticket.setAiModelName(result.getModelName());
        ticket.setAiPromptVersion(result.getPromptVersion());
    }

    private AIClassificationResult createFallback() {
        AIClassificationResult r = new AIClassificationResult();
        r.setCategory(Ticket.Category.GENERAL_ENQUIRY);
        r.setPriority(Ticket.Priority.MEDIUM);
        r.setSentiment(Ticket.Sentiment.NEUTRAL);
        r.setUrgency(Ticket.Urgency.MEDIUM);
        r.setRecommendedTeam("SUPPORT");
        r.setSuggestedTags(List.of("general"));
        r.setSummary("");
        r.setSuggestedResponse("Thank you for contacting us. A support agent will review your ticket and respond shortly.");
        r.setConfidence(0.5);
        r.setModelName("fallback");
        r.setPromptVersion("1.0.0");
        r.setValidationPassed(true);
        r.setResponseTimeMs(0);
        return r;
    }

    private void logAnalysis(Long ticketId, AIClassificationResult result, boolean success, String error) {
        try {
            AIAnalysisLog logEntry = new AIAnalysisLog();
            logEntry.setTicketId(ticketId);
            logEntry.setModelName(result.getModelName());
            logEntry.setPromptVersion(result.getPromptVersion());
            logEntry.setConfidence(result.getConfidence());
            logEntry.setResponseTimeMs(result.getResponseTimeMs());
            logEntry.setTokenUsage(0);
            logEntry.setValidationPassed(result.isValidationPassed());
            logEntry.setSuccess(success);
            logEntry.setErrorMessage(error);
            analysisLogRepo.save(logEntry);
        } catch (Exception ex) {
            log.warn("Failed to log AI analysis: {}", ex.getMessage());
        }
    }

    private String sanitizeInput(String text) {
        if (text == null) {
            return "";
        }
        if (text.length() > maxInputLength) {
            text = text.substring(0, maxInputLength);
        }
        if (injectionDetector.isSuspicious(text)) {
            text = injectionDetector.sanitize(text);
        }
        return text;
    }

    private <T> T executeWithTimeout(Callable<T> task) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            Future<T> future = executor.submit(task);
            return future.get(timeoutMs, TimeUnit.MILLISECONDS);
        } finally {
            executor.shutdownNow();
        }
    }
}
