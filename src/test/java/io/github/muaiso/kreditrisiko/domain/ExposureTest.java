package io.github.muaiso.kreditrisiko.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class ExposureTest {

    @Test
    void expectedLossIsProductOfComponents() {
        Exposure e = new Exposure(100_000, 0.4, 0.05);
        assertEquals(100_000 * 0.4 * 0.05, e.expectedLoss(), 1e-9);
    }

    @Test
    void rejectsNegativeEad() {
        assertThrows(IllegalArgumentException.class, () -> new Exposure(-1, 0.5, 0.1));
    }

    @Test
    void rejectsLgdOutOfRange() {
        assertThrows(IllegalArgumentException.class, () -> new Exposure(100, 1.2, 0.1));
    }

    @Test
    void rejectsPdOutOfRange() {
        assertThrows(IllegalArgumentException.class, () -> new Exposure(100, 0.5, -0.1));
    }
}
