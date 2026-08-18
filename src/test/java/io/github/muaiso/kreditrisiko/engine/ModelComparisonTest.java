package io.github.muaiso.kreditrisiko.engine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import io.github.muaiso.kreditrisiko.domain.CreditFeatures;
import io.github.muaiso.kreditrisiko.domain.LoanApplication;
import io.github.muaiso.kreditrisiko.engine.models.CreditModel;
import io.github.muaiso.kreditrisiko.engine.models.LogisticRegression;
import io.github.muaiso.kreditrisiko.engine.models.RandomForest;

import java.util.List;

/**
 * Testet ModelComparison.
 */
class ModelComparisonTest {

    @Test
    void ranksModels() {
        var data = List.of(
                new LoanApplication(new CreditFeatures(30, 80000, 1000, 5, "CAR"), false),
                new LoanApplication(new CreditFeatures(55, 20000, 40000, 1, "OTHER"), true),
                new LoanApplication(new CreditFeatures(28, 62000, 8000, 7, "HOUSE"), false),
                new LoanApplication(new CreditFeatures(52, 28000, 30000, 1, "OTHER"), true));
        List<CreditModel> models = List.of(
                new LogisticRegression(0.1, 100, 0.01),
                new RandomForest(5, 4, 2, 1L));
        for (CreditModel m : models) {
            m.train(data);
        }
        var ranking = new ModelComparison().rank(models, data);
        assertEquals(2, ranking.size());
        assertTrue(ranking.get(0).getValue() >= ranking.get(1).getValue());
    }
}
