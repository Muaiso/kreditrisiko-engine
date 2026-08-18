package io.github.muaiso.kreditrisiko.engine.metrics;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import java.util.List;

/**
 * Testet Confusion Matrix und abgeleitete Kennzahlen an einem bekannten Fall.
 */
class ConfusionMatrixTest {

    @Test
    void derivesAllRates() {
        // 2 Ausfaelle korrekt, 1 gesund falsch als Ausfall,
        // 3 gesund korrekt, 0 Ausfaelle uebersehen
        var m = new ConfusionMatrix(2, 1, 3, 0);
        assertEquals(6.0, m.total(), 1e-9);
        assertEquals((2.0 + 3.0) / 6.0, m.accuracy(), 1e-9);
        assertEquals(1.0, m.recall(), 1e-9);
        assertEquals(2.0 / 3.0, m.precision(), 1e-9);
        // F1 = 2*(2/3)*1 / (2/3 + 1) = 4/5
        assertEquals(0.8, m.f1(), 1e-9);
        assertEquals(3.0 / 4.0, m.specificity(), 1e-9);
    }

    @Test
    void perfectClassifier() {
        var m = new ConfusionMatrix(5, 0, 5, 0);
        assertEquals(1.0, m.accuracy(), 1e-9);
        assertEquals(1.0, m.f1(), 1e-9);
    }

    @Test
    void builderCountsCorrectly() {
        var m = ConfusionMatrixBuilder.fromLabels(
                List.of(1, 0, 1, 0, 1),
                List.of(1, 1, 1, 0, 0));
        assertEquals(2, m.truePositive());
        assertEquals(1, m.falsePositive());
        assertEquals(1, m.trueNegative());
        assertEquals(1, m.falseNegative());
    }

    @Test
    void builderRejectsUnequalLengths() {
        try {
            ConfusionMatrixBuilder.fromLabels(List.of(1, 0), List.of(1));
            assertEquals(true, false);
        } catch (IllegalArgumentException expected) {
            assertEquals(true, true);
        }
    }
}
