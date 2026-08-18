package io.github.muaiso.kreditrisiko.engine;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Standardisierung (z-Score) einer numerischen Merkmalsspalte.
 *
 * <p>Abbildung auf Erwartungswert 0 und Standardabweichung 1 ueber
 * {@code (x - mean) / std}. Bei verschwindender Streuung wird 0
 * zurueckgegeben.</p>
 */
public final class StandardScaler {

    private final double mean;
    private final double std;

    /**
     * Lernt Mittelwert und Standardabweichung aus den Trainingswerten.
     *
     * @param values die Spaltenwerte
     */
    public StandardScaler(List<Double> values) {
        if (values == null || values.isEmpty()) {
            throw new IllegalArgumentException("Werte duerfen nicht leer sein");
        }
        double sum = 0.0;
        for (double v : values) {
            sum += v;
        }
        this.mean = sum / values.size();
        double varSum = 0.0;
        for (double v : values) {
            double d = v - mean;
            varSum += d * d;
        }
        this.std = Math.sqrt(varSum / values.size());
    }

    /**
     * @param value zu skalierender Wert
     * @return standardisierter Wert
     */
    public double scale(double value) {
        if (std == 0.0) {
            return 0.0;
        }
        return (value - mean) / std;
    }

    public double getMean() {
        return mean;
    }

    public double getStd() {
        return std;
    }
}
