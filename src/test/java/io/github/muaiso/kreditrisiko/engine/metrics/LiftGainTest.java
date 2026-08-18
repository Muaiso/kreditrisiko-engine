package io.github.muaiso.kreditrisiko.engine.metrics;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import java.util.List;

/**
 * Testet Lift/Gain-Kurve.
 */
class LiftGainTest {

    @Test
    void gainReachesOneAtFullPopulation() {
        var scores = List.of(0.9, 0.8, 0.4, 0.3);
        var actual = List.of(1, 1, 0, 0);
        var lg = new LiftGain(scores, actual);
        double[] last = lg.gainPoints().get(lg.gainPoints().size() - 1);
        assertEquals(1.0, last[1], 1e-9);
    }

    @Test
    void topDecileLiftAboveOne() {
        var scores = List.of(0.95, 0.9, 0.85, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1);
        // 3 von 10 Ausfaelle, alle im oberen Drittel -> Lift im Top-Decile > 1
        var actual = List.of(1, 1, 1, 0, 0, 0, 0, 0, 0, 0);
        var lg = new LiftGain(scores, actual);
        assertTrue(lg.liftAtTopDecile() > 1.0);
    }
}
