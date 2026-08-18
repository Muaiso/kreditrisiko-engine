package io.github.muaiso.kreditrisiko.engine.models;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import io.github.muaiso.kreditrisiko.domain.CreditFeatures;
import io.github.muaiso.kreditrisiko.domain.LoanApplication;

import java.util.List;

/**
 * Testet die Logistic Regression auf Konvergenz und Trennschaerfe.
 */
class LogisticRegressionTest {

    // linear trennbare Daten: hohes DTI -> Ausfall
    private List<LoanApplication> separable() {
        return List.of(
                new LoanApplication(new CreditFeatures(30, 80000, 1000, 4, "CAR"), false),
                new LoanApplication(new CreditFeatures(31, 79000, 2000, 4, "CAR"), false),
                new LoanApplication(new CreditFeatures(55, 20000, 40000, 1, "OTHER"), true),
                new LoanApplication(new CreditFeatures(58, 18000, 45000, 1, "OTHER"), true));
    }

    @Test
    void learnsSeparableData() {
        var m = new LogisticRegression(0.5, 500, 0.0);
        m.train(separable());
        // gesunde Anfrage -> niedrige PD
        double pGood = m.predictProbability(
                new LoanApplication(new CreditFeatures(30, 80000, 1000, 4, "CAR"), false));
        // Ausfall-Anfrage -> hohe PD
        double pBad = m.predictProbability(
                new LoanApplication(new CreditFeatures(58, 18000, 45000, 1, "OTHER"), true));
        assertTrue(pBad > pGood, "PD(Ausfall) sollte > PD(gesund) sein");
        assertTrue(pGood < 0.3, "gesunde PD niedrig erwartet");
        assertTrue(pBad > 0.7, "Ausfall-PD hoch erwartet");
    }

    @Test
    void weightsInitialized() {
        var m = new LogisticRegression(0.1, 10, 0.01);
        m.train(separable());
        assertEquals(true, m.getWeights().length > 0);
    }
}
