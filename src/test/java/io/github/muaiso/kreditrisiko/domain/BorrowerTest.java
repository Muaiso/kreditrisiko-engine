package io.github.muaiso.kreditrisiko.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

/**
 * Testet Borrower und die Ableitung der CreditFeatures.
 */
class BorrowerTest {

    @Test
    void rejectsUnderage() {
        assertThrows(IllegalArgumentException.class,
                () -> new Borrower(16, 30000, 0, 1, "CAR"));
    }

    @Test
    void toFeaturesDerivesCorrectly() {
        var b = new Borrower(40, 50000, 10000, 3, "HOUSE");
        var f = b.toFeatures();
        assertEquals(40, f.age());
        assertEquals("HOUSE", f.purpose());
        assertEquals(10000.0 / 50000.0, f.debtToIncome(), 1e-9);
    }
}
