package io.github.muaiso.kreditrisiko.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Testet die CreditFeatures: Invarianten, DTI und Vektor-Aufbereitung.
 */
class CreditFeaturesTest {

    private CreditFeatures f() {
        return new CreditFeatures(34, 48000, 12000, 5, "CAR");
    }

    @Test
    void rejectsInvalidAge() {
        assertThrows(IllegalArgumentException.class, () -> new CreditFeatures(10, 1, 0, 1, "CAR"));
        assertThrows(IllegalArgumentException.class, () -> new CreditFeatures(200, 1, 0, 1, "CAR"));
    }

    @Test
    void rejectsNegativeMoney() {
        assertThrows(IllegalArgumentException.class, () -> new CreditFeatures(30, -1, 0, 1, "CAR"));
        assertThrows(IllegalArgumentException.class, () -> new CreditFeatures(30, 1, -1, 1, "CAR"));
    }

    @Test
    void debtToIncomeComputed() {
        // 12000 / 48000 = 0.25
        assertEquals(0.25, f().debtToIncome(), 1e-9);
    }

    @Test
    void dtiProtectsAgainstZeroIncome() {
        var f = new CreditFeatures(30, 0, 1000, 1, "CAR");
        assertTrue(f.debtToIncome() >= 0.0);
    }

    @Test
    void numericVectorHasFiveComponents() {
        assertEquals(5, f().numericVector().size());
        assertEquals(34.0, f().numericVector().get(0), 1e-9);
    }

    @Test
    void categoricalMapContainsPurpose() {
        assertTrue(f().categoricalMap().containsKey("CAR"));
    }
}
