package io.github.muaiso.kreditrisiko.data;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import io.github.muaiso.kreditrisiko.domain.LoanApplication;

import java.nio.file.Path;
import java.util.List;

/**
 * Testet den CSV-Loader (temp. Datei wird geschrieben).
 */
class CsvDataLoaderTest {

    @Test
    void loadsCsvCorrectly() throws Exception {
        Path tmp = Path.of(System.getProperty("java.io.tmpdir"), "kredit-test.csv");
        String csv = "age,income,debt,employmentYears,purpose,default\n"
                + "30,50000,1000,4,CAR,0\n"
                + "52,28000,30000,1,OTHER,1\n";
        java.nio.file.Files.writeString(tmp, csv);
        List<LoanApplication> apps = CsvDataLoader.load(tmp);
        assertEquals(2, apps.size());
        assertTrue(apps.get(1).defaulted());
        assertEquals("OTHER", apps.get(1).features().purpose());
        java.nio.file.Files.deleteIfExists(tmp);
    }

    @Test
    void missingFileThrows() {
        assertThrows(Exception.class,
                () -> CsvDataLoader.load(Path.of("nicht/vorhanden.csv")));
    }
}
