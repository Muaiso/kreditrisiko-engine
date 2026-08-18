package io.github.muaiso.kreditrisiko.engine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import java.util.List;

/**
 * Testet die Min-Max-Normalisierung.
 */
class FeatureNormalizerTest {

    @Test
    void mapsEndpointsToZeroAndOne() {
        var n = new FeatureNormalizer(List.of(10.0, 20.0, 30.0));
        assertEquals(0.0, n.normalize(10.0), 1e-9);
        assertEquals(1.0, n.normalize(30.0), 1e-9);
        assertEquals(0.5, n.normalize(20.0), 1e-9);
    }

    @Test
    void constantColumnReturnsZero() {
        var n = new FeatureNormalizer(List.of(5.0, 5.0, 5.0));
        assertEquals(0.0, n.normalize(5.0), 1e-9);
    }

    @Test
    void rejectsEmpty() {
        assertThrows(IllegalArgumentException.class, () -> new FeatureNormalizer(List.of()));
    }
}
