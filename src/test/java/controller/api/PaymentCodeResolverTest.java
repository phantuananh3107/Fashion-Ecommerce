package controller.api;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class PaymentCodeResolverTest {
    @Test
    void usesProvidedCode() {
        assertEquals("DH9", PaymentCodeResolver.resolve(" dh9 ", "DH12"));
    }

    @Test
    void resolvesCodeFromContent() {
        assertEquals("DH12", PaymentCodeResolver.resolve(null, "Thanh toan dh12"));
    }

    @Test
    void doesNotMatchDh1InsideDh12() {
        assertEquals("DH12", PaymentCodeResolver.resolve(null, "Thanh toan DH12"));
    }

    @Test
    void resolvesStandaloneDh1() {
        assertEquals("DH1", PaymentCodeResolver.resolve(null, "Thanh toan DH1"));
    }

    @Test
    void rejectsMultipleCodes() {
        assertNull(PaymentCodeResolver.resolve(null, "DH1 va DH12"));
    }

    @Test
    void matchesLowerCaseCode() {
        assertEquals("DH12", PaymentCodeResolver.resolve(null, "thanh toan dh12"));
    }

    @Test
    void returnsNullWhenContentHasNoCode() {
        assertNull(PaymentCodeResolver.resolve(null, "thanh toan don hang"));
    }
}
