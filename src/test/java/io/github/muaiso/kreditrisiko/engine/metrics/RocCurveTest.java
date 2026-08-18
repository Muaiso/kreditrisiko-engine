package io.github.muaiso.kreditrisiko.engine.metrics;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import java.util.List;

/**
 * Testet ROC-Kurve und AUC an perfekten/trennbaren Daten.
 */
class RocCurveTest {

    @Test
    void perfectSeparatorHasAucOne() {
        var scores = List.of(0.9, 0.8, 0.2, 0.1);
        var actual = List.of(1, 1, 0, 0);
        var roc = new RocCurve(scores, actual);
        assertEquals(1.0, roc.auc(), 1e-9);
        assertEquals(1.0, roc.gini(), 1e-9);
    }

    @Test
    void randomClassifierNearHalf() {
        // gleiche Scores -> Schwellwert wandert durch alle Indizes,
        // erwartete AUC bei diesem 2/2-Datensatz ist 0.75
        var scores = List.of(0.5, 0.5, 0.5, 0.5);
        var actual = List.of(1, 0, 1, 0);
        var roc = new RocCurve(scores, actual);
        assertEquals(0.75, roc.auc(), 1e-9);
    }

    @Test
    void separableScoresHaveAucOne() {
        // 0.7, 0.6 (pos) und 0.3, 0.2 (neg) -> perfekt trennbar
        var scores = List.of(0.7, 0.3, 0.6, 0.2);
        var actual = List.of(1, 0, 1, 0);
        var roc = new RocCurve(scores, actual);
        assertEquals(1.0, roc.auc(), 1e-9);
        assertEquals(1.0, roc.gini(), 1e-9);
    }
}
