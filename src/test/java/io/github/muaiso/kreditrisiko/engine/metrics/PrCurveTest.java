package io.github.muaiso.kreditrisiko.engine.metrics;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import java.util.List;

/**
 * Testet PR-Kurve und PR-AUC.
 */
class PrCurveTest {

    @Test
    void perfectSeparatorHasPrAucOne() {
        var scores = List.of(0.9, 0.8, 0.2, 0.1);
        var actual = List.of(1, 1, 0, 0);
        var pr = new PrCurve(scores, actual);
        assertEquals(1.0, pr.prAuc(), 1e-9);
    }

    @Test
    void prAucBelowOneForImperfect() {
        var scores = List.of(0.5, 0.4, 0.6, 0.3);
        var actual = List.of(1, 0, 1, 0);
        var pr = new PrCurve(scores, actual);
        assertTrue(pr.prAuc() > 0.0 && pr.prAuc() < 1.0);
    }
}
