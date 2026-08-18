package io.github.muaiso.kreditrisiko.engine.models;

import io.github.muaiso.kreditrisiko.domain.LoanApplication;

import java.util.List;

/**
 * Gemeinsame Schnittstelle aller Kreditrisiko-Klassifikationsmodelle.
 *
 * <p>Ein Modell wird auf rohen {@link LoanApplication}s trainiert (die
 * Feature-Aufbereitung uebernimmt der {@code FeatureAggregator}) und
 * liefert anschliessend eine Ausfallwahrscheinlichkeit (PD) je Anfrage.</p>
 */
public interface CreditModel {

    /**
     * Trainiert das Modell auf den uebergebenen Anfragen.
     *
     * @param applications die Trainingsdaten
     */
    void train(List<LoanApplication> applications);

    /**
     * Vorhergesagte Ausfallwahrscheinlichkeit einer Anfrage.
     *
     * @param application die zu bewertende Anfrage
     * @return PD in [0, 1]
     */
    double predictProbability(LoanApplication application);

    /**
     * @return eine menschenlesbare Bezeichnung des Modelltyps
     */
    String algorithmName();

    /**
     * @return ob das Modell bereits trainiert wurde
     */
    boolean isTrained();
}
