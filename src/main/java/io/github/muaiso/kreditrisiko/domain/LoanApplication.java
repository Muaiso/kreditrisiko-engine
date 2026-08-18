package io.github.muaiso.kreditrisiko.domain;

/**
 * Eine konkrete Kreditanfrage mit Zielvariable (Ausfall ja/nein).
 *
 * <p>Dient als Datensatz-Einheit fuer Training und Evaluierung. Die
 * Zielvariable {@code default} markiert einen tatsaechlich ausgefallenen
 * Kredit.</p>
 *
 * @param features die Merkmale der Anfrage
 * @param defaulted true wenn der Kredit ausgefallen ist
 */
public record LoanApplication(CreditFeatures features, boolean defaulted) {

    public LoanApplication {
        if (features == null) {
            throw new IllegalArgumentException("features duerfen nicht null sein");
        }
    }

    /**
     * @return die Zielklasse als 0 (kein Ausfall) oder 1 (Ausfall)
     */
    public int label() {
        return defaulted ? 1 : 0;
    }
}
