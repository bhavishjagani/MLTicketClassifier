package com.SwiftSort.MLTicketClassifier.ai;

import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Component
public class SensitiveDataMasker {

    private static final Pattern EMAIL = Pattern.compile("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}");
    private static final Pattern PHONE = Pattern.compile("\\b(?:\\+?\\d{1,3}[-.\\s]?)?\\(?\\d{2,4}\\)?[-.\\s]?\\d{3,4}[-.\\s]?\\d{3,4}\\b");
    private static final Pattern PAYMENT_REF = Pattern.compile("\\b(?:TXN|REF|PAY|INV)[-_]?[A-Z0-9]{6,}\\b", Pattern.CASE_INSENSITIVE);
    private static final Pattern ACCOUNT_NUM = Pattern.compile("\\b\\d{10,16}\\b");
    private static final Pattern CARD = Pattern.compile("\\b\\d{4}[\\s-]?\\d{4}[\\s-]?\\d{4}[\\s-]?\\d{4}\\b");

    public String mask(String text) {
        if (text == null || text.isBlank()) {
            return text;
        }
        String masked = EMAIL.matcher(text).replaceAll("[EMAIL_REDACTED]");
        masked = PHONE.matcher(masked).replaceAll("[PHONE_REDACTED]");
        masked = PAYMENT_REF.matcher(masked).replaceAll("[PAYMENT_REF_REDACTED]");
        masked = CARD.matcher(masked).replaceAll("[CARD_REDACTED]");
        masked = ACCOUNT_NUM.matcher(masked).replaceAll("[ACCOUNT_REDACTED]");
        return masked;
    }
}
