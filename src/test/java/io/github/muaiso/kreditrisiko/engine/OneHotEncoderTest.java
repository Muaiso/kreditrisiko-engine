package io.github.muaiso.kreditrisiko.engine;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import java.util.List;

/**
 * Testet das One-Hot-Encoding kategorischer Merkmale.
 */
class OneHotEncoderTest {

    @Test
    void encodesKnownCategory() {
        var enc = new OneHotEncoder(List.of("CAR", "HOUSE", "OTHER"));
        assertArrayEquals(new double[]{1.0, 0.0, 0.0}, enc.encode("CAR"));
        assertArrayEquals(new double[]{0.0, 0.0, 1.0}, enc.encode("OTHER"));
    }

    @Test
    void unknownCategoryIsAllZero() {
        var enc = new OneHotEncoder(List.of("CAR", "HOUSE"));
        assertArrayEquals(new double[]{0.0, 0.0}, enc.encode("UNKNOWN"));
    }

    @Test
    void deduplicatesCategories() {
        var enc = new OneHotEncoder(List.of("A", "A", "B"));
        assertEquals(2, enc.dimension());
    }

    @Test
    void rejectsEmpty() {
        assertThrows(IllegalArgumentException.class, () -> new OneHotEncoder(List.of()));
    }
}
