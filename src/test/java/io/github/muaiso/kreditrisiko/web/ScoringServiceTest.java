package io.github.muaiso.kreditrisiko.web;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import io.github.muaiso.kreditrisiko.domain.CreditFeatures;
import io.github.muaiso.kreditrisiko.domain.LoanApplication;
import io.github.muaiso.kreditrisiko.engine.models.CreditModel;
import io.github.muaiso.kreditrisiko.service.ScoringService;

import java.util.List;

/**
 * Testet den ScoringService (Train/Score/Evaluate) ohne Spring-Context.
 */
class ScoringServiceTest {

    private final ScoringService service = new ScoringService();

    @Test
    void trainsAndScoresLogisticRegression() {
        List<LoanApplication> apps = List.of(
                new LoanApplication(new CreditFeatures(30, 80000, 1000, 5, "CAR"), false),
                new LoanApplication(new CreditFeatures(55, 20000, 40000, 1, "OTHER"), true),
                new LoanApplication(new CreditFeatures(28, 62000, 8000, 7, "HOUSE"), false),
                new LoanApplication(new CreditFeatures(52, 28000, 30000, 1, "OTHER"), true));
        CreditModel model = service.train("LOGISTIC_REGRESSION", apps, 1L);
        ScoreResponse resp = service.score(model, apps.get(0));
        assertNotNull(resp);
        assertTrue(resp.probability() >= 0.0 && resp.probability() <= 1.0);
        assertEquals(false, resp.declined());
    }

    @Test
    void evaluatesRandomForest() {
        List<LoanApplication> apps = new io.github.muaiso.kreditrisiko.data.SyntheticDataGenerator(5L).generate(100);
        CreditModel model = service.train("RANDOM_FOREST", apps, 5L);
        EvaluationResult result = service.evaluate(model, apps);
        assertTrue(result.auc() >= 0.0 && result.auc() <= 1.0);
        assertTrue(result.ks() >= 0.0);
        assertEquals(100, result.confusion().values().stream().mapToInt(Integer::intValue).sum());
    }

    @Test
    void unknownAlgorithmThrows() {
        List<LoanApplication> apps = List.of(
                new LoanApplication(new CreditFeatures(30, 50000, 1000, 4, "CAR"), false));
        try {
            service.train("NICHT_VORHANDEN", apps, 1L);
            assertEquals(true, false, "sollte Exception werfen");
        } catch (IllegalArgumentException expected) {
            assertEquals(true, true);
        }
    }
}
