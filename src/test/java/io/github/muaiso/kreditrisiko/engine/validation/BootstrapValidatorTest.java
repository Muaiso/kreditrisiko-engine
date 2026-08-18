package io.github.muaiso.kreditrisiko.engine.validation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

/**
 * Testet Bootstrap-Validierung (Sampling mit Zuruecklegen, OOB-Test).
 */
class BootstrapValidatorTest {

    @Test
    void trainSizeEqualsTotalPerIteration() {
        var v = new BootstrapValidator(3, 11L);
        int total = 30;
        v.validate(fold -> assertEquals(total, fold.trainIndices().size()), total);
    }

    @Test
    void producesOutOfBagSamples() {
        var v = new BootstrapValidator(5, 3L);
        int total = 20;
        Set<Integer> everInTest = new HashSet<>();
        v.validate(fold -> everInTest.addAll(fold.testIndices()), total);
        // Bei 5 Durchlaeufen à 20 Ziehungen sollte OOB nicht leer sein
        assertTrue(everInTest.size() > 0);
    }

    @Test
    void reproducibleWithSeed() {
        var v1 = new BootstrapValidator(2, 21L);
        var v2 = new BootstrapValidator(2, 21L);
        Set<Integer> t1 = new HashSet<>();
        Set<Integer> t2 = new HashSet<>();
        v1.validate(f -> t1.addAll(f.testIndices()), 15);
        v2.validate(f -> t2.addAll(f.testIndices()), 15);
        assertEquals(t1, t2);
    }
}
