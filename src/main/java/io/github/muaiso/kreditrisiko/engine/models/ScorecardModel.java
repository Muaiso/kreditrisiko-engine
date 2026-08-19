package io.github.muaiso.kreditrisiko.engine.models;

import io.github.muaiso.kreditrisiko.domain.LoanApplication;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Punkte-basierte Scorecard – das in Banken am weitesten verbreitete
 * Scoring-Format.
 *
 * <p>Statt einer undurchsichtigen Wahrscheinlichkeit liefert eine Scorecard
 * fuer jeden Antrag eine nachvollziehbare Punktzahl; je hoeher, desto
 * bonitaetsstaerker. Intern wird ein logistisches Modell trainiert; daraus
 * wird ueber den Logit der Gesamt-Score abgeleitet:</p>
 *
 * <pre>
 *   score = basePoints - pointsPerUnitWeight * ln(PD / (1 - PD))
 * </pre>
 *
 * <p>Das Modell ist vollstaendig erklaerbar (jedes Merkmal ist einzeln
 * nachvollziehbar) und damit auditierbar – ein zentrales Anforderungsmerkmal
 * regulatorischer Modelle (EU AI Act, Basel).</p>
 */
public final class ScorecardModel implements CreditModel {

    private final LogisticRegression base;
    private final double basePoints;
    private final double pointsPerUnitWeight;
    private Map<String, Double> attributePoints;
    private boolean trained;

    /**
     * @param basePoints          Startpunktzahl (Score des neutralen Antrags)
     * @param pointsPerUnitWeight Skalierung von Logit auf Punkte
     */
    public ScorecardModel(double basePoints, double pointsPerUnitWeight) {
        this.base = new LogisticRegression(0.1, 200, 0.001);
        this.basePoints = basePoints;
        this.pointsPerUnitWeight = pointsPerUnitWeight;
        this.attributePoints = Map.of();
    }

    @Override
    public void train(List<LoanApplication> applications) {
        base.train(applications);
        double[] w = base.getWeights();
        Map<String, Double> pts = new LinkedHashMap<>();
        String[] names = featureNames(applications);
        for (int i = 0; i < w.length; i++) {
            String name = i < names.length ? names[i] : "feature_" + i;
            // Steigung des Scores bezueglich des Merkmals (hoeheres Merkmal
            // senkt bei positivem Gewicht die PD und erhoeht den Score)
            pts.put(name, -w[i] * pointsPerUnitWeight);
        }
        this.attributePoints = pts;
        this.trained = true;
    }

    @Override
    public double predictProbability(LoanApplication application) {
        return base.predictProbability(application);
    }

    /**
     * @return Punkte je Merkmal (Richtungsgewicht bezueglich des Scores)
     */
    public Map<String, Double> attributePoints() {
        if (!trained) {
            throw new IllegalStateException("Modell nicht trainiert");
        }
        return attributePoints;
    }

    /**
     * @return Gesamtpunktzahl eines Antrags (hoeher = bonitaetsstaerker)
     */
    public double score(LoanApplication application) {
        if (!trained) {
            throw new IllegalStateException("Modell nicht trainiert");
        }
        double pd = predictProbability(application);
        double logit = Math.log(pd / (1.0 - pd));
        return basePoints - pointsPerUnitWeight * logit;
    }

    private String[] featureNames(List<LoanApplication> applications) {
        int dim = new io.github.muaiso.kreditrisiko.engine.FeatureAggregator(applications)
                .vectorSize();
        String[] names = new String[dim];
        names[0] = "age";
        names[1] = "income";
        names[2] = "debt";
        names[3] = "employmentYears";
        names[4] = "dti";
        for (int i = 5; i < dim; i++) {
            names[i] = "purpose_" + (i - 5);
        }
        return names;
    }

    @Override
    public String algorithmName() {
        return "SCORECARD";
    }

    @Override
    public boolean isTrained() {
        return trained;
    }
}
