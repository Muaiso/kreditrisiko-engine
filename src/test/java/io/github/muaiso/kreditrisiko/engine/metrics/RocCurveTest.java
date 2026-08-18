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
        var scores = List.of(0.5, 0.5, 0.5, 0.5);
        var actual = List.of(1, 0, 1, 0);
        var roc = new RocCurve(scores, actual);
        assertEquals(0.5, roc.auc(), 0.05);
    }

    @Test
    void aucInUnitInterval() {
        var scores = List.of(0.7, 0.3, 0.6, 0.2);
        var actual = List.of(1, 0, 1, 0);
        var roc = new RocCurve(scores, actual);
        assertTrue(roc.auc() > 0.5 && roc.auc() < 1.0);
        assertTrue(roc.gini() > 0.0 && roc.gini() < 1.0);
    }
}
