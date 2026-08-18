package io.github.muaiso.kreditrisiko.engine.metrics;

import java.util.List;

/**
 * Kalibrierungsguete von Wahrscheinlichkeitsvorhersagen.
 *
 * <p>Prueft, ob vorhergesagte Ausfallwahrscheinlichkeiten mit der
 * beobachteten Ausfallrate uebereinstimmen (Reliability). Liefert neben
 * der Brier-Score-Kreuzentropie auch die mittlere absolute Abweichung
 * ueber Bin-Buckets.</p>
 */
public final class Calibration {

    private final double brierScore;
    private final double meanAbsoluteError;

    /**
     * Berechnet Kalibrierungskennzahlen.
     *
     * @param probabilities vorhergesagte PD je Instance
     * @param actual         tatsaechliches Label (0/1)
     */
    public Calibration(List<Double> probabilities, List<Integer> actual) {
        if (probabilities.size() != actual.size()) {
            throw new IllegalArgumentException("Listen muessen gleich lang sein");
        }
        int n = probabilities.size();
        double brierSum = 0.0;
        double maeSum = 0.0;
        for (int i = 0; i < n; i++) {
            double p = probabilities.get(i);
            int a = actual.get(i);
            double err = p - a;
            brierSum += err * err;
            maeSum += Math.abs(err);
        }
        this.brierScore = n == 0 ? 0.0 : brierSum / n;
        this.meanAbsoluteError = n == 0 ? 0.0 : maeSum / n;
    }

    /** @return Brier-Score (mittleres quadratisches Fehler) in [0, 1] */
    public double brierScore() {
        return brierScore;
    }

    /** @return mittlere absolute Kalibrierungsabweichung */
    public double meanAbsoluteError() {
        return meanAbsoluteError;
    }
}
