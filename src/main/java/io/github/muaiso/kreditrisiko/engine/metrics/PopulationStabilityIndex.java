package io.github.muaiso.kreditrisiko.engine.metrics;

import java.util.List;

/**
 * Population Stability Index (PSI) zur Überwachung der Datendrift.
 *
 * <p>Vergleicht die Verteilung eines Scores (oder Merkmals) zwischen zwei
 * Populationen – typischerweise Entwicklungsstichprobe vs. aktuelle
 * Scorecard-Anwendung. Ein PSI &gt; 0.25 deutet auf signifikante Drift
 * hin, die eine Rekalibrierung der Scorecard erforderlich macht.</p>
 *
 * <p>Formel je Bucket: {@code (AnteilNeu - AnteilAlt) * ln(AnteilNeu / AnteilAlt)}.
 * Bucket mit Anteil 0 werden mit einem kleinen Epsilon belegt, damit der
 * Logarithmus definiert bleibt.</p>
 */
public final class PopulationStabilityIndex {

    private static final double EPSILON = 1e-6;

    private final double psi;

    /**
     * Berechnet den PSI zwischen zwei Score-Listen.
     *
     * @param expected Referenz-Scores (z. B. Entwicklungsstichprobe)
     * @param actual   zu pruefende Scores (z. B. aktuelle Population)
     * @param buckets  Anzahl der gleichmaessigen Buckets (>= 2)
     */
    public PopulationStabilityIndex(List<Double> expected, List<Double> actual, int buckets) {
        if (expected == null || actual == null || expected.isEmpty() || actual.isEmpty()) {
            throw new IllegalArgumentException("Beide Populationen muessen nicht-leer sein");
        }
        if (buckets < 2) {
            throw new IllegalArgumentException("buckets muss >= 2 sein");
        }
        double[] edges = quantileEdges(expected, buckets);
        int[] expCounts = bucketCounts(expected, edges);
        int[] actCounts = bucketCounts(actual, edges);

        int nExp = expected.size();
        int nAct = actual.size();
        double sum = 0.0;
        for (int i = 0; i < buckets; i++) {
            double pExp = Math.max((double) expCounts[i] / nExp, EPSILON);
            double pAct = Math.max((double) actCounts[i] / nAct, EPSILON);
            sum += (pAct - pExp) * Math.log(pAct / pExp);
        }
        this.psi = sum;
    }

    /** @return PSI-Wert (>= 0; groesser = staerkere Drift) */
    public double value() {
        return psi;
    }

    /**
     * @return grobe Interpretation des PSI-Werts
     */
    public String interpretation() {
        if (psi < 0.1) {
            return "keine relevante Drift";
        }
        if (psi < 0.25) {
            return "maessige Drift – beobachten";
        }
        return "starke Drift – Rekalibrierung empfohlen";
    }

    private static double[] quantileEdges(List<Double> values, int buckets) {
        List<Double> sorted = new java.util.ArrayList<>(values);
        sorted.sort(Double::compareTo);
        double min = sorted.get(0);
        double max = sorted.get(sorted.size() - 1);
        double[] edges = new double[buckets - 1];
        for (int i = 1; i < buckets; i++) {
            double q = (double) i / buckets;
            edges[i - 1] = min + q * (max - min);
        }
        return edges;
    }

    private static int[] bucketCounts(List<Double> values, double[] edges) {
        int buckets = edges.length + 1;
        int[] counts = new int[buckets];
        for (double v : values) {
            int b = 0;
            while (b < edges.length && v >= edges[b]) {
                b++;
            }
            counts[b]++;
        }
        return counts;
    }
}
