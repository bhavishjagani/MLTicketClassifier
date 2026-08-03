package com.SwiftSort.MLTicketClassifier.ai;

import com.SwiftSort.MLTicketClassifier.model.Ticket;
import com.SwiftSort.MLTicketClassifier.repository.AIAnalysisLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SafeAIServiceTest {

    @Mock
    private RuleBasedAIService delegate;
    @Mock
    private SensitiveDataMasker masker;
    @Mock
    private PromptInjectionDetector injectionDetector;
    @Mock
    private EmbeddingService embeddingService;
    @Mock
    private AIAnalysisLogRepository analysisLogRepo;

    private SafeAIService safeAIService;

    @BeforeEach
    void setUp() {
        safeAIService = new SafeAIService(delegate, masker, injectionDetector, embeddingService, analysisLogRepo);
        ReflectionTestUtils.setField(safeAIService, "aiEnabled", true);
        ReflectionTestUtils.setField(safeAIService, "maxInputLength", 10000);
        ReflectionTestUtils.setField(safeAIService, "timeoutMs", 5000L);
    }

    @Test
    void classifyTicketSuccess() {
        Ticket ticket = new Ticket();
        ticket.setId(1L);
        ticket.setSubject("Test");
        ticket.setMessage("My phone is 9876543210 and payment failed");

        AIClassificationResult expected = new AIClassificationResult();
        expected.setCategory(Ticket.Category.PAYMENT_ISSUE);
        expected.setPriority(Ticket.Priority.HIGH);
        expected.setSentiment(Ticket.Sentiment.NEGATIVE);
        expected.setUrgency(Ticket.Urgency.HIGH);
        expected.setRecommendedTeam("BILLING");
        expected.setSuggestedTags(java.util.List.of("payment"));
        expected.setSummary("Payment failed");
        expected.setSuggestedResponse("We will help");
        expected.setConfidence(0.9);
        expected.setModelName("rule-based-v1");
        expected.setPromptVersion("1.0.0");
        expected.setValidationPassed(true);
        expected.setResponseTimeMs(10);

        when(injectionDetector.isSuspicious(anyString())).thenReturn(false);
        when(masker.mask(anyString())).thenReturn("My phone is [PHONE_REDACTED] and payment failed");
        when(delegate.classifyTicket(any())).thenReturn(expected);
        when(embeddingService.embed(anyString())).thenReturn(new float[128]);
        when(embeddingService.toStorageString(any())).thenReturn("0.1,0.2");

        AIClassificationResult result = safeAIService.classifyTicket(ticket);

        assertEquals(Ticket.Category.PAYMENT_ISSUE, result.getCategory());
        assertEquals(Ticket.Category.PAYMENT_ISSUE, ticket.getCategory());
        verify(analysisLogRepo).save(any());
    }

    @Test
    void classifyTicketFallbackOnError() {
        Ticket ticket = new Ticket();
        ticket.setId(2L);
        ticket.setSubject("Test");
        ticket.setMessage("Hello");

        when(injectionDetector.isSuspicious(anyString())).thenReturn(false);
        when(masker.mask(anyString())).thenReturn("Hello");
        when(delegate.classifyTicket(any())).thenThrow(new RuntimeException("AI failure"));
        when(embeddingService.embed(anyString())).thenReturn(new float[128]);
        when(embeddingService.toStorageString(any())).thenReturn("0.1");

        AIClassificationResult result = safeAIService.classifyTicket(ticket);

        assertNotNull(result);
        assertEquals(Ticket.Category.GENERAL_ENQUIRY, ticket.getCategory());
    }

    @Test
    void summarizeHandlesNull() {
        when(injectionDetector.isSuspicious(anyString())).thenReturn(false);
        when(delegate.summarize("")).thenReturn("");
        assertEquals("", safeAIService.summarize(""));
    }

    @Test
    void generateResponseFallback() {
        Ticket ticket = new Ticket();
        ticket.setMessage("test");
        when(injectionDetector.isSuspicious(anyString())).thenReturn(false);
        when(masker.mask(anyString())).thenReturn("test");
        when(delegate.generateSuggestedResponse(any(), any())).thenThrow(new RuntimeException("fail"));

        String response = safeAIService.generateSuggestedResponse(ticket, Ticket.ResponseTone.PROFESSIONAL);
        assertNotNull(response);
        assertFalse(response.isBlank());
    }

    @Test
    void masksSensitiveData() {
        Ticket ticket = new Ticket();
        ticket.setId(3L);
        ticket.setSubject("Contact");
        ticket.setMessage("Email me at test@example.com");

        AIClassificationResult expected = new AIClassificationResult();
        expected.setCategory(Ticket.Category.GENERAL_ENQUIRY);
        expected.setPriority(Ticket.Priority.MEDIUM);
        expected.setSentiment(Ticket.Sentiment.NEUTRAL);
        expected.setUrgency(Ticket.Urgency.MEDIUM);
        expected.setRecommendedTeam("SUPPORT");
        expected.setSuggestedTags(java.util.List.of());
        expected.setSummary("");
        expected.setSuggestedResponse("ok");
        expected.setConfidence(0.8);
        expected.setModelName("rule-based-v1");
        expected.setPromptVersion("1.0.0");
        expected.setValidationPassed(true);

        when(injectionDetector.isSuspicious(anyString())).thenReturn(false);
        when(masker.mask(anyString())).thenReturn("Email me at [EMAIL_REDACTED]");
        when(delegate.classifyTicket(any())).thenReturn(expected);
        when(embeddingService.embed(anyString())).thenReturn(new float[128]);
        when(embeddingService.toStorageString(any())).thenReturn("0.1");

        safeAIService.classifyTicket(ticket);
        assertEquals("Email me at [EMAIL_REDACTED]", ticket.getMaskedMessage());
    }
}
