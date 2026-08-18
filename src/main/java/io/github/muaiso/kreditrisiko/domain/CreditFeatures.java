package io.github.muaiso.kreditrisiko.domain;

import java.util.List;
import java.util.Map;

/**
 * Merkmale einer Kreditanfrage, aufbereitet fuer das Modell.
 *
 * <p>Numerische Merkmale (Alter, Einkommen, Schulden, Beschaeftigungsjahre)
 * und ein kategorisches Merkmal (Verwendungszweck). Die Klasse ist immutable.</p>
 *
 * @param age               Alter des Antragstellers in Jahren
 * @param income            Jahresnettoeinkommen
 * @param debt             bestehende Gesamtschulden
 * @param employmentYears  Jahre in Beschaeftigung
 * @param purpose          Verwendungszweck (z. B. CAR, HOUSE, OTHER)
 */
public record CreditFeatures(
        int age,
        double income,
        double debt,
        int employmentYears,
        String purpose) {

    public CreditFeatures {
        if (age < 18 || age > 120) {
            throw new IllegalArgumentException("age muss in [18, 120] liegen");
        }
        if (income < 0) {
            throw new IllegalArgumentException("income muss >= 0 sein");
        }
        if (debt < 0) {
            throw new IllegalArgumentException("debt muss >= 0 sein");
        }
        if (employmentYears < 0) {
            throw new IllegalArgumentException("employmentYears muss >= 0 sein");
        }
        if (purpose == null || purpose.isBlank()) {
            throw new IllegalArgumentException("purpose darf nicht leer sein");
        }
    }

    /**
     * Schulden-Einkommen-Verhaeltnis (Debt-to-Income, DTI) als abgeleitetes
     * Merkmal. Ein hoeheres DTI signalisiert hoehere Ausfallgefahr.
     *
     * @return DTI = debt / max(income, 1)
     */
    public double debtToIncome() {
        return debt / Math.max(income, 1.0);
    }

    /**
     * Liefert die numerischen Merkmale in fester Reihenfolge (fuer Modelle).
     *
     * @return Liste [age, income, debt, employmentYears, dti]
     */
    public List<Double> numericVector() {
        return List.of((double) age, income, debt, (double) employmentYears, debtToIncome());
    }

    /**
     * Liefert die bekannten Verwendungszwecke als Menge fuer das Encoding.
     *
     * @return Map mit Zweck -> 1.0 (fuer One-Hot-Aufbereitung)
     */
    public Map<String, Double> categoricalMap() {
        return Map.of(purpose, 1.0);
    }
}
