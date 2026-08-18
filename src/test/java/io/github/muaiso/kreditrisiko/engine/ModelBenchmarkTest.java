package io.github.muaiso.kreditrisiko.engine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import io.github.muaiso.kreditrisiko.domain.CreditFeatures;
import io.github.muaiso.kreditrisiko.domain.LoanApplication;
import io.github.muaiso.kreditrisiko.engine.models.LogisticRegression;
import io.github.muaiso.kreditrisiko.engine.validation.KFoldCrossValidator;

import java.util.List;
import java.util.Map;

/**
 * Testet ModelBenchmark.
 */
class ModelBenchmarkTest {

    private List<LoanApplication> data() {
        return new io.github.muaiso.kreditrisiko.data.SyntheticDataGenerator(5L).generate(100);
    }

    @Test
    void comparesAllModels() {
        var benchmark = new ModelBenchmark(new KFoldCrossValidator(3, 1L), 1L);
        Map<String, Double> result = benchmark.compare(data());
        assertEquals(5, result.size());
        for (double auc : result.values()) {
            assertTrue(auc >= 0.0 && auc <= 1.0, "AUC ausserhalb [0,1]");
        }
    }

    @Test
    void logisticRegressionTrained() {
        var data = List.of(
                new LoanApplication(new CreditFeatures(30, 80000, 1000, 5, "CAR"), false),
                new LoanApplication(new CreditFeatures(55, 20000, 40000, 1, "OTHER"), true));
        var model = new LogisticRegression(0.5, 50, 0.01);
        model.train(data);
        // nur Konsistenz-Check, kein Benchmark-Lauf
        assertTrue(model.isTrained());
    }
}
