package io.github.muaiso.kreditrisiko.engine.validation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Testet K-Fold- und Holdout-Validierung (Aufteilung, Reproduzierbarkeit).
 */
class KFoldHoldoutValidatorTest {

    @Test
    void kFoldProducesKDisjointTestFolds() {
        var v = new KFoldCrossValidator(5, 42L);
        int total = 100;
        Set<Integer> allTest = new HashSet<>();
        int[] foldCount = {0};
        v.validate(fold -> {
            // Train + Test = alle Indizes, disjunkt
            assertEquals(total, fold.trainIndices().size() + fold.testIndices().size());
            allTest.addAll(fold.testIndices());
            foldCount[0]++;
        }, total);
        // 5 Folds a 20 Test -> 100 unterschiedliche Test-Indizes
        assertEquals(100, allTest.size());
        assertEquals(5, foldCount[0]);
    }

    @Test
    void kFoldReproducibleWithSameSeed() {
        var v1 = new KFoldCrossValidator(4, 7L);
        var v2 = new KFoldCrossValidator(4, 7L);
        List<Integer> first1 = new java.util.ArrayList<>();
        List<Integer> first2 = new java.util.ArrayList<>();
        v1.validate(f -> first1.addAll(f.testIndices()), 40);
        v2.validate(f -> first2.addAll(f.testIndices()), 40);
        assertEquals(first1, first2);
    }

    @Test
    void holdoutSplitsByFraction() {
        var v = new HoldoutValidator(0.25, 1L);
        int total = 40;
        v.validate(fold -> {
            assertEquals(10, fold.testIndices().size());
            assertEquals(30, fold.trainIndices().size());
        }, total);
    }

    @Test
    void holdoutRejectsBadFraction() {
        assertThrows(IllegalArgumentException.class, () -> new HoldoutValidator(0.0, 1L));
        assertThrows(IllegalArgumentException.class, () -> new HoldoutValidator(1.0, 1L));
    }

    @Test
    void kFoldRejectsKLessThanTwo() {
        assertThrows(IllegalArgumentException.class, () -> new KFoldCrossValidator(1, 1L));
    }
}
