package io.github.muaiso.kreditrisiko.engine.validation;

import java.util.List;
import java.util.function.Consumer;

/**
 * Gemeinsame Schnittstelle aller Validierungsstrategien.
 *
 * <p>Teilt einen Datensatz in Trainings-/Validierungs-Folds und ruft den
 * uebergebenen Consumer je Fold mit den Indizes auf. Die konkrete
 * Aufteilung (zufaellig, geschichtet, zeitlich) uebernimmt die
 * jeweilige Implementierung.</p>
 */
public interface CrossValidator {

    /**
     * Fuehrt die Validierung durch.
     *
     * @param foldConsumer wird je Fold mit (trainIndices, testIndices) aufgerufen
     * @param totalSize    Gesamtzahl der Datenpunkte
     */
    void validate(Consumer<Fold> foldConsumer, int totalSize);

    /**
     * Ein einzelnes Train/Test-Fold.
     *
     * @param trainIndices Indizes des Trainingsdatensatzes
     * @param testIndices  Indizes des Testdatensatzes
     */
    record Fold(List<Integer> trainIndices, List<Integer> testIndices) {
    }
}
