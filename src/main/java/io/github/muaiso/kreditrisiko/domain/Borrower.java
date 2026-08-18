package io.github.muaiso.kreditrisiko.domain;

/**
 * Stammdaten eines Antragstellers.
 *
 * @param age               Alter in Jahren
 * @param income            Jahresnettoeinkommen
 * @param debt             bestehende Gesamtschulden
 * @param employmentYears  Jahre in Beschaeftigung
 * @param purpose          Verwendungszweck des Kredits
 */
public record Borrower(
        int age,
        double income,
        double debt,
        int employmentYears,
        String purpose) {

    public Borrower {
        if (age < 18) {
            throw new IllegalArgumentException("age muss >= 18 sein");
        }
        if (income < 0) {
            throw new IllegalArgumentException("income muss >= 0 sein");
        }
        if (debt < 0) {
            throw new IllegalArgumentException("debt muss >= 0 sein");
        }
    }

    /**
     * @return die aus den Stammdaten abgeleiteten Kreditmerkmale
     */
    public CreditFeatures toFeatures() {
        return new CreditFeatures(age, income, debt, employmentYears, purpose);
    }
}
