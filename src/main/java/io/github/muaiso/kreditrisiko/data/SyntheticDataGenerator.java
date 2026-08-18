package io.github.muaiso.kreditrisiko.data;

import io.github.muaiso.kreditrisiko.domain.CreditFeatures;
import io.github.muaiso.kreditrisiko.domain.LoanApplication;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Erzeugt einen synthetischen, aber realistischen Kreditdatensatz.
 *
 * <p>Ausfaelle korrelieren mit hohem DTI und niedriger Beschaeftigungsdauer.
 * Dient als reproduzierbare Demo-/Testdatenquelle ohne externe Datei.</p>
 */
public final class SyntheticDataGenerator {

    private final long seed;
    private final Random rng;

    /**
     * @param seed Startwert fuer die Reproduzierbarkeit
     */
    public SyntheticDataGenerator(long seed) {
        this.seed = seed;
        this.rng = new Random(seed);
    }

    /**
     * Erzeugt {@code n} zufaellige Antraege.
     *
     * @param n Anzahl der Datensätze
     * @return Liste der generierten Anfragen
     */
    public List<LoanApplication> generate(int n) {
        List<LoanApplication> apps = new ArrayList<>();
        String[] purposes = {"CAR", "HOUSE", "EDUCATION", "OTHER"};
        for (int i = 0; i < n; i++) {
            int age = 21 + rng.nextInt(50);
            double income = 20000 + rng.nextDouble() * 80000;
            double debt = rng.nextDouble() * income * 1.5;
            int emp = rng.nextInt(15);
            String purpose = purposes[rng.nextInt(purposes.length)];
            double dti = debt / Math.max(income, 1.0);
            // Ausfallwahrscheinlichkeit steigt mit DTI und sinkt mit Beschaeftigung
            double pDefault = 1.0 / (1.0 + Math.exp(-(dti * 5.0 - emp * 0.2 - 1.5)));
            boolean defaulted = rng.nextDouble() < pDefault;
            apps.add(new LoanApplication(
                    new CreditFeatures(age, income, debt, emp, purpose), defaulted));
        }
        return apps;
    }
}
