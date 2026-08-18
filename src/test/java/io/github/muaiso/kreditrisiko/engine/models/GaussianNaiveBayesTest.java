package io.github.muaiso.kreditrisiko.engine.models;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import io.github.muaiso.kreditrisiko.domain.CreditFeatures;
import io.github.muaiso.kreditrisiko.domain.LoanApplication;

import java.util.List;

/**
 * Testet Gaussian Naive Bayes.
 */
class GaussianNaiveBayesTest {

    private List<LoanApplication> data() {
        return List.of(
                new LoanApplication(new CreditFeatures(30, 50000, 1000, 4, "CAR"), false),
                new LoanApplication(new CreditFeatures(45, 30000, 25000, 1, "OTHER"), true),
                new LoanApplication(new CreditFeatures(28, 62000, 8000, 7, "HOUSE"), false),
                new LoanApplication(new CreditFeatures(52, 28000, 30000, 1, "OTHER"), true));
    }

    @Test
    void predictsInUnitInterval() {
        var nb = new GaussianNaiveBayes();
        nb.train(data());
        double p = nb.predictProbability(data().get(0));
        assertTrue(p >= 0.0 && p <= 1.0);
    }

    @Test
    void highDebtYieldsHigherPdThanLowDebt() {
        var nb = new GaussianNaiveBayes();
        nb.train(data());
        double pLow = nb.predictProbability(
                new LoanApplication(new CreditFeatures(30, 80000, 1000, 4, "CAR"), false));
        double pHigh = nb.predictProbability(
                new LoanApplication(new CreditFeatures(55, 20000, 40000, 1, "OTHER"), true));
        assertTrue(pHigh >= pLow, "hohes DTI sollte hoehere PD liefern");
    }
}
