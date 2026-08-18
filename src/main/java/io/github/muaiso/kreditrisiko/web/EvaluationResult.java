package io.github.muaiso.kreditrisiko.web;

import java.util.List;
import java.util.Map;

/**
 * Ergebnis einer Modell-Evaluierung.
 *
 * @param algorithm Name des Algorithmus
 * @param auc AUC-ROC
 * @param prAuc PR-AUC
 * @param ks KS-Statistik
 * @param gini Gini-Koeffizient
 * @param accuracy Genauigkeit
 * @param precision Precision
 * @param recall Recall
 * @param f1 F1-Score
 * @param confusion Confusion-Matrix als Map (tp, fp, tn, fn)
 */
public record EvaluationResult(
        String algorithm,
        double auc,
        double prAuc,
        double ks,
        double gini,
        double accuracy,
        double precision,
        double recall,
        double f1,
        Map<String, Integer> confusion) {
}
