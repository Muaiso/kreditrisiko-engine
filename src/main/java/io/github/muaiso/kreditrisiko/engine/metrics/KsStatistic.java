package io.github.muaiso.kreditrisiko.engine.metrics;

import java.util.ArrayList;
import java.util.List;

/**
 * KS-Statistik (Kolmogorov-Smirnov) fuer Kredit-Scoring.
 *
 * <p>Misst den maximalen kumulativen Abstand der Score-Verteilungen von
 * Ausfall- vs. Nicht-Ausfall-Populationen. Ein hohes KS (typisch > 0.3)
 * deutet auf gute Trennschaerfe hin.</p>
 */
public final class KsStatistic {

    private final double ks;

    /**
     * Berechnet die KS-Statistik aus Scores und Ist-Labels.
     *
     * @param scores Modell-Score/Wahrscheinlichkeit je Instance
     * @param actual tatsaechliches Label (0/1)
     */
    public KsStatistic(List<Double> scores, List<Integer> actual) {
        int n = scores.size();
        List<Integer> idx = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            idx.add(i);
        }
        idx.sort((a, b) -> Double.compare(scores.get(a), scores.get(b)));

        int pos = 0;
        int neg = 0;
        for (int a : actual) {
            if (a == 1) {
                pos++;
            } else {
                neg++;
            }
        }
        if (pos == 0 || neg == 0) {
            this.ks = 0.0;
            return;
        }

        double tp = 0.0;
        double fp = 0.0;
        double maxDiff = 0.0;
        for (int i = 0; i < n; i++) {
            int k = idx.get(i);
            if (actual.get(k) == 1) {
                tp += 1.0;
            } else {
                fp += 1.0;
            }
            double cumPos = tp / pos;
            double cumNeg = fp / neg;
            maxDiff = Math.max(maxDiff, Math.abs(cumPos - cumNeg));
        }
        this.ks = maxDiff;
    }

    /** @return KS-Wert in [0, 1] */
    public double value() {
        return ks;
    }
}
