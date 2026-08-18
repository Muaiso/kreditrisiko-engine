package io.github.muaiso.kreditrisiko.engine.models;

import io.github.muaiso.kreditrisiko.domain.CreditFeatures;
import io.github.muaiso.kreditrisiko.domain.LoanApplication;

import java.util.List;

/**
 * Baseline-Modell: entscheidet ueber einen festen Schwellwert auf dem
 * Debt-to-Income-Verhaeltnis (DTI).
 *
 * <p>Klassisches "naives" Modell als Referenz (Benchmark) fuer aufwendigere
 * Verfahren. Ein DTI ueber dem Schwellwert gilt als Ausfall.</p>
 */
public final class BaselineModel implements CreditModel {

    private final double dtiThreshold;
    private boolean trained;

    /**
     * @param dtiThreshold DTI ab dem eine Anfrage als Ausfall gilt
     */
    public BaselineModel(double dtiThreshold) {
        this.dtiThreshold = dtiThreshold;
    }

    @Override
    public void train(List<LoanApplication> applications) {
        if (applications == null || applications.isEmpty()) {
            throw new IllegalArgumentException("Trainingsdaten duerfen nicht leer sein");
        }
        this.trained = true;
    }

    @Override
    public double predictProbability(LoanApplication application) {
        if (!trained) {
            throw new IllegalStateException("Modell nicht trainiert");
        }
        CreditFeatures f = application.features();
        // stetige, von DTI abgeleitete "Wahrscheinlichkeit" (Sigmoid-aehnlich)
        double dti = f.debtToIncome();
        return sigmoid((dti - dtiThreshold) * 10.0);
    }

    private double sigmoid(double z) {
        return 1.0 / (1.0 + Math.exp(-z));
    }

    @Override
    public String algorithmName() {
        return "BASELINE_DTI";
    }

    @Override
    public boolean isTrained() {
        return trained;
    }
}
