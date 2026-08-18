package io.github.muaiso.kreditrisiko.engine.metrics;

import java.util.ArrayList;
import java.util.List;

/**
 * ROC-Kurve und AUC fuer eine binäre Klassifikation mit Scores/Probability.
 *
 * <p>Die ROC (Receiver Operating Characteristic) zeichnet die True Positive
 * Rate gegen die False Positive Rate ueber alle Schwellwerte. Die AUC ist
 * die trapezfoermig integrierte Flaeche und misst die Trennschaerfe
 * (0.5 = Zufall, 1.0 = perfekt).</p>
 */
public final class RocCurve {

    private final List<double[]> points; // [fpr, tpr]
    private final double auc;

    /**
     * Berechnet ROC-Punkte und AUC aus Scores und Ist-Labels.
     *
     * @param scores  Modell-Score/Wahrscheinlichkeit je Instance
     * @param actual  tatsaechliches Label (0/1)
     */
    public RocCurve(List<Double> scores, List<Integer> actual) {
        if (scores.size() != actual.size()) {
            throw new IllegalArgumentException("Listen muessen gleich lang sein");
        }
        int n = scores.size();
        // Indizes nach fallendem Score sortieren (Schwellwert-Wanderung)
        List<Integer> idx = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            idx.add(i);
        }
        idx.sort((a, b) -> Double.compare(scores.get(b), scores.get(a)));

        int pos = 0;
        int neg = 0;
        for (int i = 0; i < n; i++) {
            if (actual.get(i) == 1) {
                pos++;
            } else {
                neg++;
            }
        }
        if (pos == 0 || neg == 0) {
            this.points = List.of(new double[]{0.0, 0.0}, new double[]{1.0, 1.0});
            this.auc = 1.0;
            return;
        }

        this.points = new ArrayList<>();
        double tprPrev = 0.0;
        double fprPrev = 0.0;
        double tp = 0.0;
        double fp = 0.0;
        double area = 0.0;
        this.points.add(new double[]{0.0, 0.0});

        for (int i = 0; i < n; i++) {
            int k = idx.get(i);
            if (actual.get(k) == 1) {
                tp += 1.0;
            } else {
                fp += 1.0;
            }
            double tpr = tp / pos;
            double fpr = fp / neg;
            area += (fpr - fprPrev) * (tpr + tprPrev) / 2.0;
            this.points.add(new double[]{fpr, tpr});
            tprPrev = tpr;
            fprPrev = fpr;
        }
        this.auc = area;
    }

    /** @return die ROC-Punkte als [fpr, tpr]-Paare */
    public List<double[]> points() {
        return List.copyOf(points);
    }

    /** @return Flaeche unter der ROC-Kurve in [0, 1] */
    public double auc() {
        return auc;
    }

    /**
     * Gini-Koeffizient = 2 * AUC - 1.
     *
     * @return Gini in [0, 1] (0 = kein Diskriminierungsvermoegen)
     */
    public double gini() {
        return 2.0 * auc - 1.0;
    }
}
