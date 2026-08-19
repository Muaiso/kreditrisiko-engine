package io.github.muaiso.kreditrisiko.web;

import io.github.muaiso.kreditrisiko.domain.CreditFeatures;

/**
 * Anfrage zum Erklaeren einer Score-Vorhersage.
 *
 * @param age               Alter in Jahren
 * @param income           Jahresnettoeinkommen
 * @param debt             Gesamtschulden
 * @param employmentYears  Beschaeftigungsdauer
 * @param purpose          Verwendungszweck
 */
public record ExplainRequest(
        int age,
        double income,
        double debt,
        int employmentYears,
        String purpose) {

    /** @return umgewandelte Domain-Entitaet */
    public io.github.muaiso.kreditrisiko.domain.LoanApplication toDomain() {
        CreditFeatures f = new CreditFeatures(age, income, debt, employmentYears, purpose);
        return new io.github.muaiso.kreditrisiko.domain.LoanApplication(f, false);
    }
}
