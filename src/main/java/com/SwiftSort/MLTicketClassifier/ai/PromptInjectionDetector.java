package com.SwiftSort.MLTicketClassifier.ai;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.regex.Pattern;

@Component
public class PromptInjectionDetector {

    private static final List<Pattern> INJECTION_PATTERNS = List.of(
            Pattern.compile("ignore (all )?(previous|prior|above) instructions", Pattern.CASE_INSENSITIVE),
            Pattern.compile("you are now", Pattern.CASE_INSENSITIVE),
            Pattern.compile("system prompt", Pattern.CASE_INSENSITIVE),
            Pattern.compile("jailbreak", Pattern.CASE_INSENSITIVE),
            Pattern.compile("disregard (your )?instructions", Pattern.CASE_INSENSITIVE),
            Pattern.compile("```\\s*system", Pattern.CASE_INSENSITIVE)
    );

    public boolean isSuspicious(String text) {
        if (text == null || text.isBlank()) {
            return false;
        }
        return INJECTION_PATTERNS.stream().anyMatch(p -> p.matcher(text).find());
    }

    public String sanitize(String text) {
        if (text == null) {
            return "";
        }
        String sanitized = text.replaceAll("(?i)ignore (all )?(previous|prior|above) instructions", "[filtered]");
        sanitized = sanitized.replaceAll("(?i)disregard (your )?instructions", "[filtered]");
        return sanitized.trim();
    }
}
