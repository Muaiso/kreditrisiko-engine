package io.github.muaiso.kreditrisiko.engine.models;

import io.github.muaiso.kreditrisiko.domain.LoanApplication;

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

    @Override
    public void train(List<LoanApplication> applications) {
        if (applications == null || applications.isEmpty()) {
            throw new IllegalArgumentException("Trainingsdaten duerfen nicht leer sein");
        }
        this.aggregator = new FeatureAggregator(applications);
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
