package io.github.muaiso.kreditrisiko.engine.metrics;

/**
 * Confusion Matrix einer binären Klassifikation.
 *
 * <p>Hält die vier Grunddichten (TP, FP, TN, FN) und leitet die
 * klassischen Klassifikationskennzahlen ab. Die positive Klasse ist
 * "Ausfall" (default = 1).</p>
 */
public final class ConfusionMatrix {

    private final long truePositive;
    private final long falsePositive;
    private final long trueNegative;
    private final long falseNegative;

    /**
     * @param truePositive  korrekt als Ausfall erkannt
     * @param falsePositive gesund als Ausfall geflaggt
     * @param trueNegative  korrekt als gesund erkannt
     * @param falseNegative Ausfall übersehen
     */
    public ConfusionMatrix(long truePositive, long falsePositive,
                           long trueNegative, long falseNegative) {
        this.truePositive = truePositive;
        this.falsePositive = falsePositive;
        this.trueNegative = trueNegative;
        this.falseNegative = falseNegative;
    }

    public long truePositive() {
        return truePositive;
    }

    public long falsePositive() {
        return falsePositive;
    }

    public long trueNegative() {
        return trueNegative;
    }

    public long falseNegative() {
        return falseNegative;
    }

    public long total() {
        return truePositive + falsePositive + trueNegative + falseNegative;
    }

    /** @return Genauigkeit (alle korrekten / alle) */
    public double accuracy() {
        long t = total();
        return t == 0 ? 0.0 : (double) (truePositive + trueNegative) / t;
    }

    /** @return Trefferquote der positiven Klasse (Recall/Sensitivity) */
    public double recall() {
        double denom = truePositive + falseNegative;
        return denom == 0 ? 0.0 : (double) truePositive / denom;
    }

    /** @return Praezision der positiven Klasse */
    public double precision() {
        double denom = truePositive + falsePositive;
        return denom == 0 ? 0.0 : (double) truePositive / denom;
    }

    /** @return F1-Score (Harmonisches Mittel aus Precision und Recall) */
    public double f1() {
        double p = precision();
        double r = recall();
        double sum = p + r;
        return sum == 0.0 ? 0.0 : 2.0 * p * r / sum;
    }

    /** @return Spezifitaet (True Negative Rate) */
    public double specificity() {
        double denom = trueNegative + falsePositive;
        return denom == 0 ? 0.0 : (double) trueNegative / denom;
    }

    /**
     * False Positive Rate = 1 - Spezifitaet.
     *
     * @return FPR
     */
    public double falsePositiveRate() {
        return 1.0 - specificity();
    }

    /**
     * True Positive Rate (entspricht {@link #recall()}).
     *
     * @return TPR
     */
    public double truePositiveRate() {
        return recall();
    }
}
