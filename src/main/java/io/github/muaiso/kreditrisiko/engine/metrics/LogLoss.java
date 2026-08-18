package io.github.muaiso.kreditrisiko.engine.metrics;

import java.util.List;

/**
 * Logarithmische Verlustfunktion (Log Loss / Kreuzentropie).
 *
 * <p>Misst die Qualitaet von Wahrscheinlichkeitsvorhersagen: je naeher
 * die vorhergesagte PD an der tatsaechlichen Klasse liegt, desto kleiner
 * der Verlust. Belohnt gut kalibrierte, zuverlaessige Wahrscheinlichkeiten.</p>
 */
public final class LogLoss {

    private final double loss;

    /**
     * Berechnet den Log Loss.
     *
     * @param probabilities vorhergesagte PD je Instance (in (0,1) geklemmt)
     * @param actual         tatsaechliches Label (0/1)
     */
    public LogLoss(List<Double> probabilities, List<Integer> actual) {
        if (probabilities.size() != actual.size()) {
            throw new IllegalArgumentException("Listen muessen gleich lang sein");
        }
        int n = probabilities.size();
        double sum = 0.0;
        for (int i = 0; i < n; i++) {
            // klemmen, um log(0) zu vermeiden
            double p = Math.min(1.0 - 1e-15, Math.max(1e-15, probabilities.get(i)));
            int y = actual.get(i);
            sum += y * Math.log(p) + (1.0 - y) * Math.log(1.0 - p);
        }
        this.loss = n == 0 ? 0.0 : -sum / n;
    }

    /** @return Log Loss (niedriger = besser) */
    public double value() {
        return loss;
    }
}
