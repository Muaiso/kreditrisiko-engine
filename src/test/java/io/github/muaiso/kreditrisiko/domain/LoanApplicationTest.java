package io.github.muaiso.kreditrisiko.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

/**
 * Testet LoanApplication und die Label-Ableitung.
 */
class LoanApplicationTest {

    @Test
    void labelIsOneForDefault() {
        var app = new LoanApplication(
                new CreditFeatures(30, 50000, 1000, 4, "CAR"), true);
        assertEquals(1, app.label());
        assertEquals(true, app.defaulted());
    }

    @Test
    void labelIsZeroForNonDefault() {
        var app = new LoanApplication(
                new CreditFeatures(30, 50000, 1000, 4, "CAR"), false);
        assertEquals(0, app.label());
    }

    @Test
    void rejectsNullFeatures() {
        assertThrows(IllegalArgumentException.class,
                () -> new LoanApplication(null, false));
    }
}
