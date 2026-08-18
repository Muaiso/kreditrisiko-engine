package io.github.muaiso.kreditrisiko.engine;

import io.github.muaiso.kreditrisiko.domain.LoanApplication;
import io.github.muaiso.kreditrisiko.engine.metrics.ConfusionMatrix;
import io.github.muaiso.kreditrisiko.engine.metrics.ConfusionMatrixBuilder;
import io.github.muaiso.kreditrisiko.engine.models.CreditModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Optimierung der Klassifikationsschwelle (Cut-off) für ein Modell.
 *
 * <p>Sucht die Schwelle, die den Youden-Index
 * ({@code Sensitivity + Specificity - 1}) maximiert – ein Standard im
 * Kredit-Scoring, um TPR und TNR auszubalancieren.</p>
 */
public final class ThresholdOptimizer {

    /**
     * Findet die optimale Schwelle im Bereich [0.01, 0.99].
     *
     * @param model das trainierte Modell
     * @param test der Validierungsdatensatz
     * @return optimale Schwelle
     */
    public double optimize(CreditModel model, List<LoanApplication> test) {
        List<Double> scores = new ArrayList<>();
        List<Integer> actual = new ArrayList<>();
        for (LoanApplication a : test) {
            scores.add(model.predictProbability(a));
            actual.add(a.label());
        }

        double bestThreshold = 0.5;
        double bestYouden = -1.0;
        for (int t = 1; t < 100; t++) {
            double threshold = t / 100.0;
            List<Integer> predicted = new ArrayList<>();
            for (double s : scores) {
                predicted.add(s >= threshold ? 1 : 0);
            }
            ConfusionMatrix cm = ConfusionMatrixBuilder.fromLabels(actual, predicted);
            double youden = cm.recall() + cm.specificity() - 1.0;
            if (youden > bestYouden) {
                bestYouden = youden;
                bestThreshold = threshold;
            }
        }
        return bestThreshold;
    }
}
