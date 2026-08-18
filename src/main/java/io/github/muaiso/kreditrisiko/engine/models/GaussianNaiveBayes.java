package io.github.muaiso.kreditrisiko.engine.models;

import io.github.muaiso.kreditrisiko.domain.LoanApplication;

import java.util.List;

/**
 * Gaussian Naive Bayes Klassifikator.
 *
 * <p>Modelliert je Klasse (Ausfall / Nicht-Ausfall) eine multivariate
 * Gauss-Verteilung ueber die Features (naive Unabhaengigkeit). Die
 * Ausfallwahrscheinlichkeit folgt ueber den Satz von Bayes.</p>
 */
public final class GaussianNaiveBayes implements CreditModel {

    private double[] meanPos;
    private double[] varPos;
    private double[] meanNeg;
    private double[] varNeg;
    private double priorPos;
    private boolean trained;
    private FeatureAggregator aggregator;
    private int dim;

    @Override
    public void train(List<LoanApplication> applications) {
        if (applications == null || applications.isEmpty()) {
            throw new IllegalArgumentException("Trainingsdaten duerfen nicht leer sein");
        }
        this.aggregator = new FeatureAggregator(applications);
        this.dim = aggregator.vectorSize();
        meanPos = new double[dim];
        varPos = new double[dim];
        meanNeg = new double[dim];
        varNeg = new double[dim];
        double[][] x = new double[applications.size()][];
        int[] y = new int[applications.size()];
        for (int i = 0; i < applications.size(); i++) {
            x[i] = aggregator.toVector(applications.get(i).features());
            y[i] = applications.get(i).label();
        }
        long pos = 0;
        for (int v : y) {
            pos += v;
        }
        this.priorPos = (double) pos / y.length;

        double[] sumPos = new double[dim];
        double[] sumNeg = new double[dim];
        long nPos = 0, nNeg = 0;
        for (int i = 0; i < y.length; i++) {
            if (y[i] == 1) {
                for (int j = 0; j < dim; j++) {
                    sumPos[j] += x[i][j];
                }
                nPos++;
            } else {
                for (int j = 0; j < dim; j++) {
                    sumNeg[j] += x[i][j];
                }
                nNeg++;
            }
        }
        for (int j = 0; j < dim; j++) {
            meanPos[j] = nPos == 0 ? 0.0 : sumPos[j] / nPos;
            meanNeg[j] = nNeg == 0 ? 0.0 : sumNeg[j] / nNeg;
        }
        // Varianzen
        double[] varSumPos = new double[dim];
        double[] varSumNeg = new double[dim];
        for (int i = 0; i < y.length; i++) {
            if (y[i] == 1) {
                for (int j = 0; j < dim; j++) {
                    double d = x[i][j] - meanPos[j];
                    varSumPos[j] += d * d;
                }
            } else {
                for (int j = 0; j < dim; j++) {
                    double d = x[i][j] - meanNeg[j];
                    varSumNeg[j] += d * d;
                }
            }
        }
        for (int j = 0; j < dim; j++) {
            varPos[j] = (nPos <= 1) ? 1.0 : varSumPos[j] / nPos;
            varNeg[j] = (nNeg <= 1) ? 1.0 : varSumNeg[j] / nNeg;
        }
        this.trained = true;
    }

    @Override
    public double predictProbability(LoanApplication application) {
        if (!trained) {
            throw new IllegalStateException("Modell nicht trainiert");
        }
        double[] x = aggregator.toVector(application.features());
        double logPos = Math.log(priorPos);
        double logNeg = Math.log(1.0 - priorPos);
        for (int j = 0; j < dim; j++) {
            logPos += gaussianLogLikelihood(x[j], meanPos[j], varPos[j]);
            logNeg += gaussianLogLikelihood(x[j], meanNeg[j], varNeg[j]);
        }
        // logistische Kombination (LogSumExp-stabil)
        double max = Math.max(logPos, logNeg);
        double p = Math.exp(logPos - max) / (Math.exp(logPos - max) + Math.exp(logNeg - max));
        return Math.min(1.0 - 1e-15, Math.max(1e-15, p));
    }

    private double gaussianLogLikelihood(double x, double mean, double variance) {
        double v = Math.max(variance, 1e-9);
        return -0.5 * Math.log(2.0 * Math.PI * v) - 0.5 * (x - mean) * (x - mean) / v;
    }

    @Override
    public String algorithmName() {
        return "GAUSSIAN_NAIVE_BAYES";
    }

    @Override
    public boolean isTrained() {
        return trained;
    }
}
