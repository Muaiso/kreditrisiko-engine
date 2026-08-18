package io.github.muaiso.kreditrisiko.engine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import java.util.List;

/**
 * Testet die z-Score-Standardisierung.
 */
class StandardScalerTest {

    @Test
    void meanCenteredAtZero() {
        var s = new StandardScaler(List.of(2.0, 4.0, 6.0));
        // Mittel 4, std = sqrt(8/3) ~= 1.633
        assertEquals(0.0, s.scale(4.0), 1e-9);
        assertEquals((6.0 - 4.0) / Math.sqrt(8.0 / 3.0), s.scale(6.0), 1e-9);
    }

    @Test
    void zeroStdReturnsZero() {
        var s = new StandardScaler(List.of(3.0, 3.0));
        assertEquals(0.0, s.scale(3.0), 1e-9);
    }

    @Test
    void rejectsEmpty() {
        assertThrows(IllegalArgumentException.class, () -> new StandardScaler(List.of()));
    }
}
