package io.github.muaiso.kreditrisiko.service;

import io.github.muaiso.kreditrisiko.domain.Exposure;
import io.github.muaiso.kreditrisiko.domain.LoanApplication;
import io.github.muaiso.kreditrisiko.domain.Rating;
import io.github.muaiso.kreditrisiko.engine.CostSensitiveThreshold;
import io.github.muaiso.kreditrisiko.engine.ModelSerializer;
import io.github.muaiso.kreditrisiko.engine.ShapExplainer;
import io.github.muaiso.kreditrisiko.engine.metrics.ConfusionMatrix;
import io.github.muaiso.kreditrisiko.engine.metrics.ConfusionMatrixBuilder;
import io.github.muaiso.kreditrisiko.engine.metrics.ExpectedLoss;
import io.github.muaiso.kreditrisiko.engine.metrics.KsStatistic;
import io.github.muaiso.kreditrisiko.engine.metrics.PopulationStabilityIndex;
import io.github.muaiso.kreditrisiko.engine.metrics.PrCurve;
import io.github.muaiso.kreditrisiko.engine.metrics.RocCurve;
import io.github.muaiso.kreditrisiko.engine.models.BaselineModel;
import io.github.muaiso.kreditrisiko.engine.models.CreditModel;
import io.github.muaiso.kreditrisiko.engine.models.DecisionTree;
import io.github.muaiso.kreditrisiko.engine.models.EnsembleModel;
import io.github.muaiso.kreditrisiko.engine.models.GaussianNaiveBayes;
import io.github.muaiso.kreditrisiko.engine.models.LogisticRegression;
import io.github.muaiso.kreditrisiko.engine.models.RandomForest;
import io.github.muaiso.kreditrisiko.engine.models.ScorecardModel;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Fassade fuer das Training, Scoring und die Evaluierung von Kreditmodellen.
 *
 * <p>Erzeugt das angeforderte Modell, trainiert es und liefert Scores
 * sowie Kennzahlen. Die Entscheidungsschwelle fuer "Ablehnung" ist 0.5.</p>
 */
public final class ScoringService {

    private static final double DECLINE_THRESHOLD = 0.5;

    /**
     * Erzeugt und trainiert ein Modell anhand des Algorithmus-Namens.
     *
     * @param algorithm Name des Verfahrens
     * @param applications Trainingsdaten
     * @param seed Startwert fuer Reproduzierbarkeit
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

    /**
     * Erklaert eine Vorhersage ueber Merkmalsbeitraege (SHAP-aehnlich).
     *
     * @param model        das trainierte Modell
     * @param application  zu erklaerender Antrag
     * @param background   Referenzpopulation fuer den Basiswert
     * @return Merkmalsname -> Beitrag zur PD
     */
    public Map<String, Double> explain(
            CreditModel model, LoanApplication application, List<LoanApplication> background) {
        return new ShapExplainer(model, background).explain(application);
    }

    /**
     * Bestimmt die kostenoptimale Entscheidungsschwelle.
     *
     * @param scores  Modell-Scores der Testdaten
     * @param actual  Ist-Labels (0/1)
     * @param costFp  Kosten eines Ausfalls (FP)
     * @param costFn  Kosten einer entgangenen Vergabe (FN)
     * @return die optimale Schwelle in [0, 1]
     */
    public double optimalThreshold(List<Double> scores, List<Integer> actual,
                                   double costFp, double costFn) {
        CostSensitiveThreshold t = new CostSensitiveThreshold(costFp, costFn);
        t.fit(scores, actual, 200);
        return t.optimalThreshold();
    }

    /**
     * Misst die Datendrift zwischen zwei Score-Populationen (PSI).
     *
     * @param expected Referenz-Scores
     * @param actual   aktuelle Scores
     * @return PSI-Wert (>= 0)
     */
    public double populationStabilityIndex(List<Double> expected, List<Double> actual) {
        return new PopulationStabilityIndex(expected, actual, 10).value();
    }

    /**
     * Serialisiert ein trainiertes Modell als JSON.
     *
     * @param model das trainierte Modell
     * @return JSON-Repraesentation
     */
    public String serializeModel(CreditModel model) {
        return new ModelSerializer().serialize(model);
    }

    /**
     * Aggregiert die Expected Loss eines Portfolios.
     *
     * @param exposures die Einzelengagements
     * @return Gesamt-Expected-Loss
     */
    public double portfolioExpectedLoss(List<Exposure> exposures) {
        return new ExpectedLoss(exposures).totalExpectedLoss();
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
            case "SCORECARD" -> new ScorecardModel(600.0, 50.0);
            case "ENSEMBLE_VOTING" -> new EnsembleModel(List.of(
                    new LogisticRegression(0.1, 100, 0.01),
                    new GaussianNaiveBayes(),
                    new DecisionTree(6, 2)));
            default -> throw new IllegalArgumentException("Unbekannter Algorithmus: " + algorithm);
        };
    }
}
