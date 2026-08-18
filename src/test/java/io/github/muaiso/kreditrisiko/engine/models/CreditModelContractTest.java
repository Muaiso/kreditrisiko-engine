package io.github.muaiso.kreditrisiko.engine.models;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import io.github.muaiso.kreditrisiko.domain.CreditFeatures;
import io.github.muaiso.kreditrisiko.domain.LoanApplication;

import java.util.List;

/**
 * Testet die gemeinsame CreditModel-Contract ueber alle Implementierungen.
 */
class CreditModelContractTest {

    private List<LoanApplication> data() {
        return List.of(
                new LoanApplication(new CreditFeatures(30, 50000, 1000, 4, "CAR"), false),
                new LoanApplication(new CreditFeatures(45, 30000, 25000, 1, "OTHER"), true),
                new LoanApplication(new CreditFeatures(28, 62000, 8000, 7, "HOUSE"), false),
                new LoanApplication(new CreditFeatures(52, 28000, 30000, 1, "OTHER"), true));
    }

    @Test
    void probabilitiesInUnitInterval() {
        CreditModel[] models = {
                new BaselineModel(0.3),
                new LogisticRegression(0.1, 100, 0.01),
                new DecisionTree(5, 2),
                new RandomForest(5, 4, 2, 1L),
                new GaussianNaiveBayes()
        };
        for (CreditModel m : models) {
            m.train(data());
            double p = m.predictProbability(data().get(0));
            assertTrue(p >= 0.0 && p <= 1.0, m.algorithmName() + " PD ausserhalb [0,1]");
            assertEquals(true, m.isTrained());
        }
    }

    @Test
    void rejectsEmptyTraining() {
        CreditModel m = new LogisticRegression(0.1, 10, 0.01);
        assertThrows(IllegalArgumentException.class, () -> m.train(List.of()));
    }

    @Test
    void predictsBeforeTrainThrows() {
        CreditModel m = new DecisionTree(3, 2);
        assertThrows(IllegalStateException.class,
                () -> m.predictProbability(data().get(0)));
    }
}
