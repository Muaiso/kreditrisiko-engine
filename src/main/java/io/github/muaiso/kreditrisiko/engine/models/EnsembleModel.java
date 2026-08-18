package io.github.muaiso.kreditrisiko.engine.models;

import io.github.muaiso.kreditrisiko.domain.LoanApplication;

import java.util.List;

/**
 * Ensemble (Soft Voting) mehrerer Basis-Modelle.
 *
 * <p>Kombiniert die Ausfallwahrscheinlichkeiten gleichgewichteter
 * Einzelmodelle zum Mittel. Erhoeht die Robustheit gegenueber
 * Modell-Bias einzelner Verfahren.</p>
 */
public final class EnsembleModel implements CreditModel {

    private final List<CreditModel> models;
    private boolean trained;

    /**
     * @param models die zu kombinierenden Basis-Modelle (>= 2)
     */
    public EnsembleModel(List<CreditModel> models) {
        if (models == null || models.size() < 2) {
            throw new IllegalArgumentException("Ensemble braucht >= 2 Modelle");
        }
        this.models = models;
    }

    @Override
    public void train(List<LoanApplication> applications) {
        if (applications == null || applications.isEmpty()) {
            throw new IllegalArgumentException("Trainingsdaten duerfen nicht leer sein");
        }
        for (CreditModel m : models) {
            m.train(applications);
        }
        this.trained = true;
    }

    @Override
    public double predictProbability(LoanApplication application) {
        if (!trained) {
            throw new IllegalStateException("Modell nicht trainiert");
        }
        double sum = 0.0;
        for (CreditModel m : models) {
            sum += m.predictProbability(application);
        }
        return sum / models.size();
    }

    /** @return Anzahl der kombinierten Modelle */
    public int size() {
        return models.size();
    }

    @Override
    public String algorithmName() {
        return "ENSEMBLE_VOTING";
    }

    @Override
    public boolean isTrained() {
        return trained;
    }
}
