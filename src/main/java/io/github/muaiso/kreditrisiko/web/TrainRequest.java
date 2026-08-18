package io.github.muaiso.kreditrisiko.web;

import java.util.List;

/**
 * Request-DTO zum Trainieren eines Modells.
 *
 * @param algorithm Name des Algorithmus (LOGISTIC_REGRESSION, RANDOM_FOREST, ...)
 * @param applications die Trainingsdatensätze
 * @param seed Startwert für reproduzierbare Modelle
 */
public record TrainRequest(
        String algorithm,
        List<TrainApplication> applications,
        Long seed) {
}
