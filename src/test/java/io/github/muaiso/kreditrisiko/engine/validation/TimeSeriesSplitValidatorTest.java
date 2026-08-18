package io.github.muaiso.kreditrisiko.engine.validation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

/**
 * Testet TimeSeriesSplit (aufsteigende, nicht gemischte Fenster).
 */
class TimeSeriesSplitValidatorTest {

    @Test
    void foldsIncreaseTrainWindow() {
        var v = new TimeSeriesSplitValidator(3);
        int total = 40;
        List<Integer> trainSizes = new java.util.ArrayList<>();
        v.validate(fold -> trainSizes.add(fold.trainIndices().size()), total);
        // Trainingsfenster waechst: 10, 20, 30
        assertEquals(List.of(10, 20, 30), trainSizes);
    }

    @Test
    void noFutureLeak() {
        var v = new TimeSeriesSplitValidator(2);
        int total = 30;
        v.validate(fold -> {
            int maxTrain = fold.trainIndices().stream().mapToInt(i -> i).max().orElse(-1);
            int minTest = fold.testIndices().stream().mapToInt(i -> i).min().orElse(-1);
            assertTrue(maxTrain < minTest, "Test darf nicht in der Vergangenheit liegen");
        }, total);
    }

    @Test
    void rejectsTooSmallDataset() {
        var v = new TimeSeriesSplitValidator(4);
        assertThrows(IllegalArgumentException.class, () -> v.validate(f -> {}, 3));
    }
}
