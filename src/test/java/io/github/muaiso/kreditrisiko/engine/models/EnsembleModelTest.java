package io.github.muaiso.kreditrisiko.engine.models;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import io.github.muaiso.kreditrisiko.domain.CreditFeatures;
import io.github.muaiso.kreditrisiko.domain.LoanApplication;

import java.util.List;

/**
 * Testet das Ensemble (Soft Voting).
 */
class EnsembleModelTest {

    private List<LoanApplication> data() {
        return List.of(
                new LoanApplication(new CreditFeatures(30, 50000, 1000, 4, "CAR"), false),
                new LoanApplication(new CreditFeatures(45, 30000, 25000, 1, "OTHER"), true),
                new LoanApplication(new CreditFeatures(28, 62000, 8000, 7, "HOUSE"), false),
                new LoanApplication(new CreditFeatures(52, 28000, 30000, 1, "OTHER"), true));
    }

    @Test
    void combinesModels() {
        var ensemble = new EnsembleModel(List.of(
                new LogisticRegression(0.1, 50, 0.01),
                new GaussianNaiveBayes(),
                new DecisionTree(4, 2)));
        ensemble.train(data());
        assertEquals(3, ensemble.size());
        double p = ensemble.predictProbability(data().get(0));
        assertTrue(p >= 0.0 && p <= 1.0);
    }

    @Test
    void rejectsSingleModel() {
        assertThrows(IllegalArgumentException.class,
                () -> new EnsembleModel(List.of(new BaselineModel(0.3))));
    }
}
