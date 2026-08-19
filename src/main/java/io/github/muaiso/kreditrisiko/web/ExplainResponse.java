package io.github.muaiso.kreditrisiko.web;

import java.util.Map;

/**
 * Antwort mit Modell-Erklaerung (Merkmalsbeitraege zur PD).
 *
 * @param probability  vorhergesagte PD
 * @param baseValue    Basiswert (Hintergrund-PD)
 * @param contributions Merkmal -> Beitrag zur PD
 */
public record ExplainResponse(
        double probability,
        double baseValue,
        Map<String, Double> contributions) {
}
