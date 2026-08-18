package io.github.muaiso.kreditrisiko.engine.validation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import io.github.muaiso.kreditrisiko.domain.CreditFeatures;
import io.github.muaiso.kreditrisiko.domain.LoanApplication;

import java.util.ArrayList;
import java.util.List;

/**
 * Testet die stratifizierte K-Fold-Validierung auf Klassenerhalt.
 */
class StratifiedKFoldValidatorTest {

    private List<LoanApplication> balancedData() {
        List<LoanApplication> apps = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            apps.add(new LoanApplication(
                    new CreditFeatures(30, 50000, 1000, 4, "CAR"), false));
        }
        for (int i = 0; i < 50; i++) {
            apps.add(new LoanApplication(
                    new CreditFeatures(45, 30000, 25000, 1, "OTHER"), true));
        }
        return apps;
    }

    @Test
    void preservesClassRatioInEachFold() {
        var v = new StratifiedKFoldValidator(5, 99L);
        var data = balancedData();
        v.validate(data, fold -> {
            long testPos = fold.testIndices().stream()
                    .filter(i -> data.get(i).defaulted()).count();
            long testNeg = fold.testIndices().size() - testPos;
            // 50/50 Daten, k=5 -> pro Fold 10 pos + 10 neg = 20 Test
            assertEquals(20, testPos + testNeg);
            assertTrue(testPos > 0 && testNeg > 0, "beide Klassen im Test-Fold");
        });
    }

    @Test
    void trainAndTestDisjoint() {
        var v = new StratifiedKFoldValidator(4, 5L);
        var data = balancedData();
        v.validate(data, fold -> {
            var overlap = new java.util.HashSet<>(fold.trainIndices());
            overlap.retainAll(fold.testIndices());
            assertTrue(overlap.isEmpty());
        });
    }
}
