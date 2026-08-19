package io.github.muaiso.kreditrisiko.engine;

import java.util.List;

/**
 * Kostenoptimale Entscheidungsschwelle fuer Kredit-Zusagen.
 *
 * <p>Die Standard-Schwelle 0.5 ist selten wirtschaftlich optimal. Statt-
 * dessen minimiert dieses Modul die erwarteten Kosten je Antrag ueber alle
 * Schwellen: ein <em>False Positive</em> (Kredit vergeben, der ausfaellt)
 * kostet {@code costFp} (typ. die erwartete Verlusthoehe), ein
 * <em>False Negative</em> (guten Kredit abgelehnt) kostet {@code costFn}
 * (entgangener Zinsgewinn).</p>
 *
 * <pre>
 *   erwartete Kosten(t) = FP(t)*costFp + FN(t)*costFn   ->  min
 * </pre>
 */
public final class CostSensitiveThreshold {

    private final double costFp;
    private final double costFn;
    private double optimalThreshold;
    private double minimalCost;

    /**
     * @param costFp Kosten eines erteilten, ausgefallenen Kredits (>= 0)
     * @param costFn Kosten eines abgelehnten, guten Kredits (>= 0)
     */
    public CostSensitiveThreshold(double costFp, double costFn) {
        if (costFp < 0 || costFn < 0) {
            throw new IllegalArgumentException("Kosten muessen >= 0 sein");
        }
        this.costFp = costFp;
        this.costFn = costFn;
    }

    /**
     * Bestimmt die kostenoptimale Schwelle aus Scores und Ist-Labels.
     *
     * @param scores Modell-Score/Wahrscheinlichkeit je Instance
     * @param actual tatsaechliches Label (0/1)
     * @param steps  Anzahl der zu pruefenden Schwellen (>= 2)
     */
    public void fit(List<Double> scores, List<Integer> actual, int steps) {
        if (scores.size() != actual.size()) {
            throw new IllegalArgumentException("Listen muessen gleich lang sein");
        }
        if (steps < 2) {
            throw new IllegalArgumentException("steps muss >= 2 sein");
        }
        int n = scores.size();
        double bestT = 0.5;
        double bestCost = Double.POSITIVE_INFINITY;
        for (int s = 0; s <= steps; s++) {
            double t = (double) s / steps;
            int fp = 0;
            int fn = 0;
            for (int i = 0; i < n; i++) {
                boolean predictedDefault = scores.get(i) >= t;
                boolean actualDefault = actual.get(i) == 1;
                if (predictedDefault && !actualDefault) {
                    fp++;
                } else if (!predictedDefault && actualDefault) {
                    fn++;
                }
            }
            double cost = fp * costFp + fn * costFn;
            if (cost < bestCost) {
                bestCost = cost;
                bestT = t;
            }
        }
        this.optimalThreshold = bestT;
        this.minimalCost = bestCost;
    }

    /** @return die kostenoptimale Schwelle in [0, 1] */
    public double optimalThreshold() {
        return optimalThreshold;
    }

    /** @return die minimalen erwarteten Kosten bei dieser Schwelle */
    public double minimalCost() {
        return minimalCost;
    }
}
