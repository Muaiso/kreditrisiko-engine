package io.github.muaiso.kreditrisiko.engine.metrics;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import java.util.List;

/**
 * Testet die KS-Statistik.
 */
class KsStatisticTest {

    @Test
    void perfectSeparationHasKsOne() {
        var scores = List.of(0.9, 0.8, 0.1, 0.05);
        var actual = List.of(1, 1, 0, 0);
        assertEquals(1.0, new KsStatistic(scores, actual).value(), 1e-9);
    }

    @Test
    void overlappingDistributionsReduceKs() {
        var scores = List.of(0.6, 0.55, 0.5, 0.45);
        var actual = List.of(1, 0, 1, 0);
        double ks = new KsStatistic(scores, actual).value();
        assertTrue(ks > 0.0 && ks < 1.0);
    }
}
