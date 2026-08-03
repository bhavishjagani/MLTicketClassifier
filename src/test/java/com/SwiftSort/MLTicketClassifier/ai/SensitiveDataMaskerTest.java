package com.SwiftSort.MLTicketClassifier.ai;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SensitiveDataMaskerTest {

    private final SensitiveDataMasker masker = new SensitiveDataMasker();

    @Test
    void masksEmail() {
        assertTrue(masker.mask("Contact test@example.com").contains("[EMAIL_REDACTED]"));
    }

    @Test
    void masksPhone() {
        assertTrue(masker.mask("My phone number is 9876543210").contains("[PHONE_REDACTED]"));
    }

    @Test
    void handlesNull() {
        assertNull(masker.mask(null));
    }
}
