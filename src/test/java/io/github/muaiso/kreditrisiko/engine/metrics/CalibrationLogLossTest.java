package io.github.muaiso.kreditrisiko.engine.metrics;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import java.util.List;

/**
 * Testet Kalibrierung und Log Loss.
 */
class CalibrationLogLossTest {

    @Test
    void perfectCalibrationHasZeroBrier() {
        var probs = List.of(1.0, 0.0, 1.0, 0.0);
        var actual = List.of(1, 0, 1, 0);
        assertEquals(0.0, new Calibration(probs, actual).brierScore(), 1e-9);
        assertEquals(0.0, new Calibration(probs, actual).meanAbsoluteError(), 1e-9);
    }

    @Test
    void logLossPerfectIsZero() {
        var probs = List.of(0.99, 0.01, 0.99, 0.01);
        var actual = List.of(1, 0, 1, 0);
        assertEquals(0.0, new LogLoss(probs, actual).value(), 1e-6);
    }

    @Test
    void logLossHigherForWrongProb() {
        var correct = List.of(0.9, 0.1);
        var wrong = List.of(0.1, 0.9);
        var actual = List.of(1, 0);
        double good = new LogLoss(correct, actual).value();
        double bad = new LogLoss(wrong, actual).value();
        assertEquals(true, bad > good);
    }
}
