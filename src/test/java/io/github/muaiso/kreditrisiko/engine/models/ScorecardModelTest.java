package io.github.muaiso.kreditrisiko.engine.models;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.muaiso.kreditrisiko.domain.CreditFeatures;
import io.github.muaiso.kreditrisiko.domain.LoanApplication;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

class ScorecardModelTest {

    private List<LoanApplication> sample() {
        var gen = new io.github.muaiso.kreditrisiko.data.SyntheticDataGenerator(7L);
        return gen.generate(120);
    }

    @Test
    void trainsAndScores() {
        ScorecardModel model = new ScorecardModel(600.0, 50.0);
        model.train(sample());
        LoanApplication app = new LoanApplication(
                new CreditFeatures(35, 50000, 5000, 6, "CAR"), false);
        double score = model.score(app);
        assertTrue(score > 0 && score < 3000, "Score plausibel, war: " + score);
        assertTrue(model.predictProbability(app) > 0 && model.predictProbability(app) < 1);
    }

    @Test
    void attributePointsAreExposed() {
        ScorecardModel model = new ScorecardModel(600.0, 50.0);
        model.train(sample());
        assertTrue(model.attributePoints().containsKey("age"));
        assertTrue(model.attributePoints().containsKey("income"));
    }

    @Test
    void scoreReflectsFeaturesMonotonically() {
        ScorecardModel model = new ScorecardModel(600.0, 50.0);
        model.train(sample());
        LoanApplication good = new LoanApplication(
                new CreditFeatures(40, 90000, 1000, 10, "HOUSE"), false);
        LoanApplication bad = new LoanApplication(
                new CreditFeatures(22, 22000, 20000, 0, "OTHER"), false);
        assertTrue(model.score(good) > model.score(bad),
                "besserer Antrag erhaelt mehr Punkte");
    }

    @Test
    void rejectsScoringBeforeTraining() {
        ScorecardModel model = new ScorecardModel(600.0, 50.0);
        assertThrows(IllegalStateException.class,
                () -> model.attributePoints());
    }
}
