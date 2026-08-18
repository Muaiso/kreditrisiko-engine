package io.github.muaiso.kreditrisiko.engine.models;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import io.github.muaiso.kreditrisiko.domain.CreditFeatures;
import io.github.muaiso.kreditrisiko.domain.LoanApplication;

import java.util.List;

/**
 * Testet DecisionTree und RandomForest.
 */
class TreeModelsTest {

    private List<LoanApplication> data() {
        return List.of(
                new LoanApplication(new CreditFeatures(30, 50000, 1000, 4, "CAR"), false),
                new LoanApplication(new CreditFeatures(45, 30000, 25000, 1, "OTHER"), true),
                new LoanApplication(new CreditFeatures(28, 62000, 8000, 7, "HOUSE"), false),
                new LoanApplication(new CreditFeatures(52, 28000, 30000, 1, "OTHER"), true));
    }

    @Test
    void decisionTreeTrainsAndPredicts() {
        var tree = new DecisionTree(5, 2);
        tree.train(data());
        double p = tree.predictProbability(data().get(0));
        assertTrue(p >= 0.0 && p <= 1.0);
    }

    @Test
    void randomForestBuildsMultipleTrees() {
        var rf = new RandomForest(7, 4, 2, 42L);
        rf.train(data());
        assertEquals(7, rf.treeCount());
        double p = rf.predictProbability(data().get(1));
        assertTrue(p >= 0.0 && p <= 1.0);
    }

    @Test
    void randomForestRejectsZeroTrees() {
        try {
            new RandomForest(0, 4, 2, 1L);
            assertEquals(true, false);
        } catch (IllegalArgumentException expected) {
            assertEquals(true, true);
        }
    }
}
