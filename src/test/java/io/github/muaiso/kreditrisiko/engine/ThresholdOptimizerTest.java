package io.github.muaiso.kreditrisiko.engine;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import io.github.muaiso.kreditrisiko.domain.CreditFeatures;
import io.github.muaiso.kreditrisiko.domain.LoanApplication;
import io.github.muaiso.kreditrisiko.engine.models.LogisticRegression;

import java.util.List;

/**
 * Testet ThresholdOptimizer.
 */
class ThresholdOptimizerTest {

    @Test
    void findsThresholdInRange() {
        var data = List.of(
                new LoanApplication(new CreditFeatures(30, 80000, 1000, 5, "CAR"), false),
                new LoanApplication(new CreditFeatures(55, 20000, 40000, 1, "OTHER"), true),
                new LoanApplication(new CreditFeatures(28, 62000, 8000, 7, "HOUSE"), false),
                new LoanApplication(new CreditFeatures(52, 28000, 30000, 1, "OTHER"), true));
        var model = new LogisticRegression(0.5, 500, 0.0);
        model.train(data);
        double t = new ThresholdOptimizer().optimize(model, data);
        assertTrue(t >= 0.01 && t <= 0.99, "Schwelle ausserhalb [0.01, 0.99]");
    }
}
