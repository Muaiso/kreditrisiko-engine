package io.github.muaiso.kreditrisiko.service;

import io.github.muaiso.kreditrisiko.domain.LoanApplication;
import io.github.muaiso.kreditrisiko.domain.Rating;
import io.github.muaiso.kreditrisiko.engine.metrics.ConfusionMatrix;
import io.github.muaiso.kreditrisiko.engine.metrics.ConfusionMatrixBuilder;
import io.github.muaiso.kreditrisiko.engine.metrics.KsStatistic;
import io.github.muaiso.kreditrisiko.engine.metrics.PrCurve;
import io.github.muaiso.kreditrisiko.engine.metrics.RocCurve;
import io.github.muaiso.kreditrisiko.engine.models.BaselineModel;
import io.github.muaiso.kreditrisiko.engine.models.CreditModel;
import io.github.muaiso.kreditrisiko.engine.models.DecisionTree;
import io.github.muaiso.kreditrisiko.engine.models.EnsembleModel;
import io.github.muaiso.kreditrisiko.engine.models.GaussianNaiveBayes;
import io.github.muaiso.kreditrisiko.engine.models.LogisticRegression;
import io.github.muaiso.kreditrisiko.engine.models.RandomForest;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Fassade für das Training, Scoring und die Evaluierung von Kreditmodellen.
 *
 * <p>Erzeugt das angeforderte Modell, trainiert es und liefert Scores
 * sowie Kennzahlen. Die Entscheidungsschwelle für "Ablehnung" ist 0.5.</p>
 */
public final class ScoringService {

    private static final double DECLINE_THRESHOLD = 0.5;

    /**
     * Erzeugt und trainiert ein Modell anhand des Algorithmus-Namens.
     *
     * @param algorithm Name des Verfahrens
     * @param applications Trainingsdaten
     * @param seed Startwert für Reproduzierbarkeit
     * @return das trainierte Modell
     */
    public CreditModel train(String algorithm, List<LoanApplication> applications, long seed) {
        CreditModel model = createModel(algorithm, seed);
        model.train(applications);
        return model;
    }

    /**
     * Bewertet eine Anfrage und leitet Rating + Ablehnung ab.
     *
     * @param model das trainierte Modell
     * @param application zu bewertende Anfrage
     * @return Score-Antwort
     */
    public io.github.muaiso.kreditrisiko.web.ScoreResponse score(
            CreditModel model, LoanApplication application) {
        double pd = model.predictProbability(application);
        Rating rating = Rating.fromPd(pd);
        return new io.github.muaiso.kreditrisiko.web.ScoreResponse(
                pd, rating.name(), pd >= DECLINE_THRESHOLD);
    }

    /**
     * Evaluiert ein Modell auf einem Testdatensatz.
     *
     * @param model das trainierte Modell
     * @param test der Testdatensatz
     * @return Kennzahlen der Evaluierung
     */
    public io.github.muaiso.kreditrisiko.web.EvaluationResult evaluate(
            CreditModel model, List<LoanApplication> test) {
        List<Double> scores = new ArrayList<>();
        List<Integer> actual = new ArrayList<>();
        List<Integer> predicted = new ArrayList<>();
        for (LoanApplication a : test) {
            double p = model.predictProbability(a);
            scores.add(p);
            actual.add(a.label());
            predicted.add(p >= DECLINE_THRESHOLD ? 1 : 0);
        }

        RocCurve roc = new RocCurve(scores, actual);
        PrCurve pr = new PrCurve(scores, actual);
        KsStatistic ks = new KsStatistic(scores, actual);
        ConfusionMatrix cm = ConfusionMatrixBuilder.fromLabels(actual, predicted);

        double gini = 2.0 * roc.auc() - 1.0;
        return new io.github.muaiso.kreditrisiko.web.EvaluationResult(
                model.algorithmName(),
                roc.auc(),
                pr.prAuc(),
                ks.value(),
                gini,
                cm.accuracy(),
                cm.precision(),
                cm.recall(),
                cm.f1(),
                confusionMap(cm));
    }

    private Map<String, Integer> confusionMap(ConfusionMatrix cm) {
        Map<String, Integer> m = new LinkedHashMap<>();
        m.put("tp", (int) cm.truePositive());
        m.put("fp", (int) cm.falsePositive());
        m.put("tn", (int) cm.trueNegative());
        m.put("fn", (int) cm.falseNegative());
        return m;
    }

    private CreditModel createModel(String algorithm, long seed) {
        return switch (algorithm.toUpperCase()) {
            case "BASELINE_DTI" -> new BaselineModel(0.3);
            case "LOGISTIC_REGRESSION" -> new LogisticRegression(0.1, 100, 0.01);
            case "DECISION_TREE" -> new DecisionTree(6, 2);
            case "RANDOM_FOREST" -> new RandomForest(10, 5, 2, seed);
            case "GAUSSIAN_NAIVE_BAYES" -> new GaussianNaiveBayes();
            case "ENSEMBLE_VOTING" -> new EnsembleModel(List.of(
                    new LogisticRegression(0.1, 100, 0.01),
                    new GaussianNaiveBayes(),
                    new DecisionTree(6, 2)));
            default -> throw new IllegalArgumentException("Unbekannter Algorithmus: " + algorithm);
        };
    }
}
