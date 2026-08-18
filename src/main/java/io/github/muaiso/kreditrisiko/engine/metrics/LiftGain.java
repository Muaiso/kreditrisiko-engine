package io.github.muaiso.kreditrisiko.engine.metrics;

import java.util.ArrayList;
import java.util.List;

/**
 * Lift- und Gain-Kurve fuer kumulative Decile-Analysen.
 *
 * <p>Sortiert die Population nach absteigendem Risiko-Score und zeigt,
 * welchen Anteil der tatsaechlichen Ausfaelle die obersten x % der
 * Risiko-Score-Rangliste enthalten (Gain) bzw. wie viel besser das Modell
 * gegenueber Zufall trennt (Lift).</p>
 */
public final class LiftGain {

    private final List<double[]> gainPoints; // [fraction, gain]
    private final List<double[]> liftPoints;  // [fraction, lift]

    /**
     * Berechnet Gain/Lift aus Scores und Ist-Labels.
     *
     * @param scores Modell-Score/Wahrscheinlichkeit je Instance
     * @param actual tatsaechliches Label (0/1)
     */
    public LiftGain(List<Double> scores, List<Integer> actual) {
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
        double baseRate = pos / (double) n;

        this.gainPoints = new ArrayList<>();
        this.liftPoints = new ArrayList<>();
        int cumPos = 0;
        for (int i = 0; i < n; i++) {
            int k = idx.get(i);
            if (actual.get(k) == 1) {
                cumPos++;
            }
            double fraction = (i + 1.0) / n;
            double gain = pos == 0 ? 0.0 : cumPos / (double) pos;
            double lift = baseRate == 0 ? 0.0 : gain / baseRate;
            gainPoints.add(new double[]{fraction, gain});
            liftPoints.add(new double[]{fraction, lift});
        }
    }

    /** @return Gain-Punkte als [Anteil, kumulativer Gain] */
    public List<double[]> gainPoints() {
        return List.copyOf(gainPoints);
    }

    /** @return Lift-Punkte als [Anteil, Lift] */
    public List<double[]> liftPoints() {
        return List.copyOf(liftPoints);
    }

    /**
     * Lift beim obersten Decile (oberste 10 % der Rangliste).
     *
     * @return Lift im ersten Zehntel
     */
    public double liftAtTopDecile() {
        if (liftPoints.isEmpty()) {
            return 0.0;
        }
        // erster Punkt, der >= 10 % Abdeckung erreicht
        for (double[] p : liftPoints) {
            if (p[0] >= 0.1) {
                return p[1];
            }
        }
        return liftPoints.get(liftPoints.size() - 1)[1];
    }
}
