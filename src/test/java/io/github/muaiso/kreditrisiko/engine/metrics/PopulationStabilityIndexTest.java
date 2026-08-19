package io.github.muaiso.kreditrisiko.engine.metrics;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

class PopulationStabilityIndexTest {

    @Test
    void identicalPopulationsHaveNearZeroPsi() {
        List<Double> a = new ArrayList<>();
        List<Double> b = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            a.add((double) i / 100);
            b.add((double) i / 100);
        }
        double psi = new PopulationStabilityIndex(a, b, 10).value();
        assertTrue(psi < 1e-6, "identische Populationen liefern PSI ~ 0, war: " + psi);
    }

    @Test
    void shiftedPopulationHasPositivePsi() {
        List<Double> expected = new ArrayList<>();
        List<Double> actual = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            expected.add((double) i / 100);
            actual.add(Math.min(1.0, (double) (i + 40) / 100));
        }
        double psi = new PopulationStabilityIndex(expected, actual, 10).value();
        assertTrue(psi > 0.1, "verschobene Population liefert nennenswerten PSI, war: " + psi);
    }

    @Test
    void interpretationReflectsMagnitude() {
        List<Double> a = new ArrayList<>();
        List<Double> b = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            a.add((double) i / 100);
            b.add((double) i / 100);
        }
        assertEquals("keine relevante Drift",
                new PopulationStabilityIndex(a, b, 10).interpretation());
    }

    @Test
    void rejectsEmptyPopulations() {
        assertThrows(IllegalArgumentException.class,
                () -> new PopulationStabilityIndex(List.of(), List.of(1.0), 5));
    }

    @Test
    void rejectsTooFewBuckets() {
        assertThrows(IllegalArgumentException.class,
                () -> new PopulationStabilityIndex(List.of(0.1), List.of(0.2), 1));
    }
}
