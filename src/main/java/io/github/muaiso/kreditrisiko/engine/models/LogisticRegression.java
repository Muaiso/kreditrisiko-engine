package io.github.muaiso.kreditrisiko.engine.models;

import io.github.muaiso.kreditrisiko.domain.LoanApplication;

import io.github.muaiso.kreditrisiko.engine.FeatureAggregator;

import java.util.ArrayList;
import java.util.List;

/**
 * Logistic Regression mit stochastischem Gradientenabstieg und L2-Regularisierung.
 *
 * <p>Modelliert die Ausfallwahrscheinlichkeit ueber
 * {@code P(default) = sigmoid(w^T x + b)}. Die Gewichte werden ueber
 * Kreuzentropie mit L2-Strafterm trainiert. Features werden ueber den
 * {@code FeatureAggregator} aus den Applications gewonnen.</p>
 */
public final class LogisticRegression implements CreditModel {

    private final double learningRate;
    private final int epochs;
    private final double regularization;
    private double[] weights;
    private double bias;
    private boolean trained;
    private FeatureAggregator aggregator;
    private List<String> purposes = List.of();

    /**
     * @param learningRate Schrittweite des Gradientenabstiegs
     * @param epochs        Anzahl der Trainingsdurchlaeufe
     * @param regularization L2-Strafterm (>= 0)
     */
    public LogisticRegression(double learningRate, int epochs, double regularization) {
        this.learningRate = learningRate;
        this.epochs = epochs;
        this.regularization = regularization;
    }

    /**
     * Rekonstruiert ein bereits trainiertes Modell aus gespeicherten
     * Gewichten und Bias (z. B. nach {@link ModelSerializer}).
     *
     * @param weights die gelernten Feature-Gewichte
     * @param bias    die gelernte Bias-Konstante
     */
    public LogisticRegression(double[] weights, double bias) {
        this(0.1, 1, 0.0);
        if (weights == null || weights.length == 0) {
            throw new IllegalArgumentException("weights darf nicht leer sein");
        }
        this.weights = weights.clone();
        this.bias = bias;
        this.trained = true;
    }

    /**
     * Rekonstruiert ein bereits trainiertes Modell aus gespeicherten
     * Gewichten, Bias und den bekannten Kategorien (z. B. nach
     * {@link io.github.muaiso.kreditrisiko.engine.ModelSerializer}).
     *
     * @param weights   die gelernten Feature-Gewichte
     * @param bias      die gelernte Bias-Konstante
     * @param purposes  die bekannten Verwendungszwecke (One-Hot-Kodierung)
     */
    public LogisticRegression(double[] weights, double bias, List<String> purposes) {
        this(0.1, 1, 0.0);
        if (weights == null || weights.length == 0) {
            throw new IllegalArgumentException("weights darf nicht leer sein");
        }
        if (purposes == null || purposes.isEmpty()) {
            throw new IllegalArgumentException("purposes darf nicht leer sein");
        }
        this.weights = weights.clone();
        this.bias = bias;
        this.purposes = List.copyOf(purposes);
        this.aggregator = new FeatureAggregator(
                purposes.stream()
                        .map(p -> new LoanApplication(new io.github.muaiso.kreditrisiko.domain.CreditFeatures(30, 40000, 5000, 3, p), false))
                        .toList());
        this.trained = true;
    }

    /**
     * @return die gelernte Bias-Konstante
     */
    public double getBias() {
        return bias;
    }

    /**
     * @return die bekannten Verwendungszwecke (One-Hot-Kodierung)
     */
    public List<String> getPurposes() {
        return purposes;
    }

    @Override
    public void train(List<LoanApplication> applications) {
        if (applications == null || applications.isEmpty()) {
            throw new IllegalArgumentException("Trainingsdaten duerfen nicht leer sein");
        }
        this.aggregator = new FeatureAggregator(applications);
        List<String> known = new ArrayList<>();
        for (LoanApplication a : applications) {
            String p = a.features().purpose();
            if (!known.contains(p)) {
                known.add(p);
            }
        }
        this.purposes = List.copyOf(known);
        int dim = aggregator.vectorSize();
        this.weights = new double[dim];
        this.bias = 0.0;
        double[][] x = new double[applications.size()][];
        int[] y = new int[applications.size()];
        for (int i = 0; i < applications.size(); i++) {
            x[i] = aggregator.toVector(applications.get(i).features());
            y[i] = applications.get(i).label();
        }

        for (int e = 0; e < epochs; e++) {
            for (int i = 0; i < x.length; i++) {
                double z = bias;
                for (int j = 0; j < dim; j++) {
                    z += weights[j] * x[i][j];
                }
                double p = sigmoid(z);
                double error = p - y[i];
                // Gradient mit L2-Regularisierung
                bias -= learningRate * error;
                for (int j = 0; j < dim; j++) {
                    weights[j] -= learningRate * (error * x[i][j] + regularization * weights[j]);
                }
            }
        }
        this.trained = true;
    }

    @Override
    public double predictProbability(LoanApplication application) {
        if (!trained) {
            throw new IllegalStateException("Modell nicht trainiert");
        }
        double[] x = aggregator.toVector(application.features());
        double z = bias;
        for (int j = 0; j < weights.length; j++) {
            z += weights[j] * x[j];
        }
        return clamp(sigmoid(z));
    }

    /**
     * @return die gelernten Gewichte (inkl. Bias am Ende)
     */
    public double[] getWeights() {
        return weights == null ? new double[0] : weights.clone();
    }

    private double sigmoid(double z) {
        return 1.0 / (1.0 + Math.exp(-z));
    }

    private double clamp(double p) {
        return Math.min(1.0 - 1e-15, Math.max(1e-15, p));
    }

    @Override
    public String algorithmName() {
        return "LOGISTIC_REGRESSION";
    }

    @Override
    public boolean isTrained() {
        return trained;
    }
}
