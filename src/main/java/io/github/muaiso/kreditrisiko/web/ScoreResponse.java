package io.github.muaiso.kreditrisiko.web;

/**
 * Antwort auf eine Score-Anfrage.
 *
 * @param probability Ausfallwahrscheinlichkeit (PD) in [0, 1]
 * @param rating abgeleitete Bonitätsstufe
 * @param declined ob der Antrag abgelehnt wird (PD über Schwelle)
 */
public record ScoreResponse(
        double probability,
        String rating,
        boolean declined) {
}
