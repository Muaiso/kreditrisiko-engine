package io.github.muaiso.kreditrisiko.engine.metrics;

import java.util.ArrayList;
import java.util.List;

/**
 * Precision-Recall-Kurve und PR-AUC.
 *
 * <p>Im Kreditrisiko mit seltenen Ausfaellen (unausgeglichenen Klassen)
 * ist die PR-Kurve aussagekraeftiger als die ROC. Die PR-AUC ist die
 * ueber das Recall integrierte Precision.</p>
 */
public final class PrCurve {

    private final List<double[]> points; // [recall, precision]
    private final double prAuc;

    /**
     * Berechnet PR-Kurve und PR-AUC.
     *
     * @param scores Modell-Score/Wahrscheinlichkeit je Instance
     * @param actual tatsaechliches Label (0/1)
     */
    public PrCurve(List<Double> scores, List<Integer> actual) {
        int n = scores.size();
        List<Integer> idx = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            idx.add(i);
        }
        idx.sort((a, b) -> Double.compare(scores.get(b), scores.get(a)));

        int pos = 0;
        for (int a : actual) {
            if (a == 1) {
                pos++;
            }
        }
        if (pos == 0) {
            this.points = List.of(new double[]{0.0, 1.0});
            this.prAuc = 1.0;
            return;
        }

        this.points = new ArrayList<>();
        double tp = 0.0;
        double fp = 0.0;
        double area = 0.0;
        double recallPrev = 0.0;
        this.points.add(new double[]{0.0, 1.0});

        for (int i = 0; i < n; i++) {
            int k = idx.get(i);
            if (actual.get(k) == 1) {
                tp += 1.0;
            } else {
                fp += 1.0;
            }
            double recall = tp / pos;
            double precision = tp / (tp + fp);
            area += (recall - recallPrev) * precision;
            this.points.add(new double[]{recall, precision});
            recallPrev = recall;
        }
        this.prAuc = area;
    }

    /** @return PR-Punkte als [recall, precision]-Paare */
    public List<double[]> points() {
        return List.copyOf(points);
    }

    /** @return Flaeche unter der PR-Kurve */
    public double prAuc() {
        return prAuc;
    }
}
