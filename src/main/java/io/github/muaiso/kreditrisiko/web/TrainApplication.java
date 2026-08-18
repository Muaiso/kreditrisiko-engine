package io.github.muaiso.kreditrisiko.web;

import io.github.muaiso.kreditrisiko.domain.CreditFeatures;

/**
 * Eine Anfrage innerhalb eines Trainings- oder Score-Requests.
 *
 * @param age Alter in Jahren
 * @param income Jahresnettoeinkommen
 * @param debt Gesamtschulden
 * @param employmentYears Beschäftigungsdauer
 * @param purpose Verwendungszweck
 * @param defaulted Zielvariable (nur beim Training)
 */
public record TrainApplication(
        int age,
        double income,
        double debt,
        int employmentYears,
        String purpose,
        Boolean defaulted) {

    /**
     * Wandelt dieses DTO in eine Domain-Entität um.
     */
    public io.github.muaiso.kreditrisiko.domain.LoanApplication toDomain() {
        CreditFeatures f = new CreditFeatures(age, income, debt, employmentYears, purpose);
        return new io.github.muaiso.kreditrisiko.domain.LoanApplication(f, Boolean.TRUE.equals(defaulted));
    }
}
