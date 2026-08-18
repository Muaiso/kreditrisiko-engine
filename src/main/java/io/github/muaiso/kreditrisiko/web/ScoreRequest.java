package io.github.muaiso.kreditrisiko.web;

/**
 * Anfrage zum Bewerten einer einzelnen Kreditanfrage.
 *
 * @param age Alter in Jahren
 * @param income Jahresnettoeinkommen
 * @param debt Gesamtschulden
 * @param employmentYears Beschäftigungsdauer
 * @param purpose Verwendungszweck
 */
public record ScoreRequest(
        int age,
        double income,
        double debt,
        int employmentYears,
        String purpose) {

    /**
     * Wandelt in Domain-Entität um (Zielvariable unbekannt).
     */
    public io.github.muaiso.kreditrisiko.domain.LoanApplication toDomain() {
        io.github.muaiso.kreditrisiko.domain.CreditFeatures f =
                new io.github.muaiso.kreditrisiko.domain.CreditFeatures(age, income, debt, employmentYears, purpose);
        return new io.github.muaiso.kreditrisiko.domain.LoanApplication(f, false);
    }
}
