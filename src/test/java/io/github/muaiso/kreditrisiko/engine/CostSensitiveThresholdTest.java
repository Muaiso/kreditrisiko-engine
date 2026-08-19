package io.github.muaiso.kreditrisiko.engine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import java.util.List;

class CostSensitiveThresholdTest {

    @Test
    void highFnCostLowersThresholdRelativeToHighFpCost() {
        // Realistisch: Ausfall = niedriger Score (hohe PD). Teure FN
        // (guten Kredit abgelehnt) soll mehr Kredite vergeben -> niedrigere
        // Schwelle; teure FP (Ausfall vergeben) soll strenger ablehnen ->
        // hoehere Schwelle.
        var scores = List.of(0.1, 0.2, 0.3, 0.4, 0.45, 0.55, 0.6, 0.7, 0.8, 0.9);
        var actual = List.of(1, 1, 1, 1, 1, 0, 0, 0, 0, 0);
        CostSensitiveThreshold cheapFn = new CostSensitiveThreshold(1.0, 1000.0);
        cheapFn.fit(scores, actual, 100);
        CostSensitiveThreshold cheapFp = new CostSensitiveThreshold(1000.0, 1.0);
        cheapFp.fit(scores, actual, 100);
        assertTrue(cheapFn.optimalThreshold() < cheapFp.optimalThreshold(),
                "teure FN -> niedrigere Schwelle als teure FP");
    }

    @Test
    void minimalCostIsNonNegative() {
        var scores = List.of(0.9, 0.1);
        var actual = List.of(1, 0);
        CostSensitiveThreshold t = new CostSensitiveThreshold(5.0, 5.0);
        t.fit(scores, actual, 10);
        assertTrue(t.minimalCost() >= 0);
    }

    @Test
    void rejectsNegativeCosts() {
        assertThrows(IllegalArgumentException.class,
                () -> new CostSensitiveThreshold(-1.0, 5.0));
    }

    @Test
    void rejectsMismatchedLists() {
        CostSensitiveThreshold t = new CostSensitiveThreshold(1.0, 1.0);
        assertThrows(IllegalArgumentException.class,
                () -> t.fit(List.of(0.5), List.of(1, 0), 5));
    }
}
