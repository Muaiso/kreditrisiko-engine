package io.github.muaiso.kreditrisiko.engine;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import io.github.muaiso.kreditrisiko.domain.CreditFeatures;
import io.github.muaiso.kreditrisiko.domain.LoanApplication;
import io.github.muaiso.kreditrisiko.engine.models.LogisticRegression;

import java.util.List;
import java.util.Map;

/**
 * Testet FeatureImportance.
 */
class FeatureImportanceTest {

    @Test
    void computesImportanceMap() {
        var data = List.of(
                new LoanApplication(new CreditFeatures(30, 80000, 1000, 5, "CAR"), false),
                new LoanApplication(new CreditFeatures(55, 20000, 40000, 1, "OTHER"), true));
        var model = new LogisticRegression(0.5, 50, 0.01);
        model.train(data);
        Map<Integer, Double> imp = new FeatureImportance(1L).compute(model, data);
        assertNotNull(imp);
    }
}
