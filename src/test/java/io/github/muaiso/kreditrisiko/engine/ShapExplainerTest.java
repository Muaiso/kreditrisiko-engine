package io.github.muaiso.kreditrisiko.engine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.muaiso.kreditrisiko.domain.CreditFeatures;
import io.github.muaiso.kreditrisiko.domain.LoanApplication;
import io.github.muaiso.kreditrisiko.engine.models.CreditModel;
import io.github.muaiso.kreditrisiko.engine.models.LogisticRegression;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

class ShapExplainerTest {

    private CreditModel trainedModel() {
        LogisticRegression model = new LogisticRegression(0.1, 200, 0.001);
        model.train(new io.github.muaiso.kreditrisiko.data.SyntheticDataGenerator(7L).generate(120));
        return model;
    }

    @Test
    void explainsWithKnownFeatureKeys() {
        CreditModel model = trainedModel();
        ShapExplainer explainer = new ShapExplainer(model,
                new io.github.muaiso.kreditrisiko.data.SyntheticDataGenerator(7L).generate(120));
        LoanApplication app = new LoanApplication(
                new CreditFeatures(35, 50000, 5000, 6, "CAR"), false);
        Map<String, Double> contributions = explainer.explain(app);
        assertTrue(contributions.containsKey("age"));
        assertTrue(contributions.containsKey("income"));
        assertTrue(contributions.containsKey("debt"));
        assertTrue(contributions.containsKey("employmentYears"));
    }

    @Test
    void contributionsApproximatePrediction() {
        CreditModel model = trainedModel();
        var bg = new io.github.muaiso.kreditrisiko.data.SyntheticDataGenerator(7L).generate(120);
        ShapExplainer explainer = new ShapExplainer(model, bg);
        LoanApplication app = new LoanApplication(
                new CreditFeatures(35, 50000, 5000, 6, "CAR"), false);
        Map<String, Double> c = explainer.explain(app);
        double reconstructed = explainer.baseValue()
                + c.values().stream().mapToDouble(Double::doubleValue).sum();
        // Ablations-Naeherung (Logit ist nichtlinear) -> Toleranz
        assertEquals(model.predictProbability(app), reconstructed, 0.05,
                "Basiswert + Summe der Beitraege naehert PD an");
    }

    @Test
    void rejectsUntrainedModel() {
        CreditModel model = new LogisticRegression(0.1, 10, 0.01);
        assertThrows(IllegalStateException.class,
                () -> new ShapExplainer(model,
                        new io.github.muaiso.kreditrisiko.data.SyntheticDataGenerator(1L).generate(10)));
    }
}
