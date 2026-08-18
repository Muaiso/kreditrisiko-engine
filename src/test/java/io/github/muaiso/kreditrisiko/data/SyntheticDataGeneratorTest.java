package io.github.muaiso.kreditrisiko.data;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import io.github.muaiso.kreditrisiko.domain.LoanApplication;

import java.util.List;

/**
 * Testet den synthetischen Daten-Generator.
 */
class SyntheticDataGeneratorTest {

    @Test
    void generatesRequestedCount() {
        var apps = new SyntheticDataGenerator(1L).generate(50);
        assertEquals(50, apps.size());
    }

    @Test
    void reproducibleWithSeed() {
        var a = new SyntheticDataGenerator(7L).generate(20);
        var b = new SyntheticDataGenerator(7L).generate(20);
        assertEquals(a, b);
    }

    @Test
    void containsBothClasses() {
        var apps = new SyntheticDataGenerator(3L).generate(200);
        long pos = apps.stream().filter(LoanApplication::defaulted).count();
        assertTrue(pos > 0 && pos < apps.size(), "beide Klassen sollten vorkommen");
    }
}
