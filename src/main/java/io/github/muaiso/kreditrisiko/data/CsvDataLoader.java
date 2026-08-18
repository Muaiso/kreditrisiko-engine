package io.github.muaiso.kreditrisiko.data;

import io.github.muaiso.kreditrisiko.domain.CreditFeatures;
import io.github.muaiso.kreditrisiko.domain.LoanApplication;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Laedt Kreditantraege aus einer CSV-Datei.
 *
 * <p>Erwartetes Format (Headerzeile):
 * {@code age,income,debt,employmentYears,purpose,default}
 * Die Spalte {@code default} ist 1 (Ausfall) oder 0 (kein Ausfall).</p>
 */
public final class CsvDataLoader {

    private CsvDataLoader() {
    }

    /**
     * Liest alle Antraege aus der CSV-Datei.
     *
     * @param path Pfad zur CSV-Datei
     * @return Liste der geladenen Anfragen
     */
    public static List<LoanApplication> load(Path path) throws IOException {
        List<LoanApplication> apps = new ArrayList<>();
        try (BufferedReader reader = Files.newBufferedReader(path)) {
            String line = reader.readLine(); // Header ueberspringen
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) {
                    continue;
                }
                apps.add(parseLine(line));
            }
        }
        return apps;
    }

    private static LoanApplication parseLine(String line) {
        String[] c = line.split(",");
        int age = Integer.parseInt(c[0].trim());
        double income = Double.parseDouble(c[1].trim());
        double debt = Double.parseDouble(c[2].trim());
        int employment = Integer.parseInt(c[3].trim());
        String purpose = c[4].trim();
        boolean defaulted = Integer.parseInt(c[5].trim()) == 1;
        return new LoanApplication(
                new CreditFeatures(age, income, debt, employment, purpose), defaulted);
    }
}
