package io.github.muaiso.kreditrisiko.engine;

import java.util.List;

/**
 * Min-Max-Normalisierung einer numerischen Merkmalsspalte.
 *
 * <p>Abbildung auf [0, 1] ueber {@code (x - min) / (max - min)}. Bei
 * konstanter Spalte (max == min) wird 0 zurueckgegeben, um Division
 * durch Null zu vermeiden.</p>
 */
public final class FeatureNormalizer {

    private final double min;
    private final double max;

    /**
     * Lernt Min/Max aus den Trainingswerten.
     *
     * @param values die Spaltenwerte
     */
    public FeatureNormalizer(List<Double> values) {
        if (values == null || values.isEmpty()) {
            throw new IllegalArgumentException("Werte duerfen nicht leer sein");
        }
        double lo = Double.POSITIVE_INFINITY;
        double hi = Double.NEGATIVE_INFINITY;
        for (double v : values) {
            lo = Math.min(lo, v);
            hi = Math.max(hi, v);
        }
        this.min = lo;
        this.max = hi;
    }

    /**
     * @param value zu normalisierender Wert
     * @return Wert in [0, 1]
     */
    public double normalize(double value) {
        if (max == min) {
            return 0.0;
        }
        return (value - min) / (max - min);
    }

    public double getMin() {
        return min;
    }

    public double getMax() {
        return max;
    }
}
