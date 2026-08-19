package io.github.muaiso.kreditrisiko.engine.metrics;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.github.muaiso.kreditrisiko.domain.Exposure;

import org.junit.jupiter.api.Test;

import java.util.List;

class ExpectedLossTest {

    @Test
    void aggregatesSumOfSingleExpectedLosses() {
        var exposures = List.of(
                new Exposure(100_000, 0.4, 0.05),
                new Exposure(50_000, 0.6, 0.10));
        double expected = 100_000 * 0.4 * 0.05 + 50_000 * 0.6 * 0.10;
        assertEquals(expected, new ExpectedLoss(exposures).totalExpectedLoss(), 1e-6);
    }

    @Test
    void expectedLossRateEqualsElOverEad() {
        var exposures = List.of(
                new Exposure(100_000, 0.4, 0.05),
                new Exposure(50_000, 0.6, 0.10));
        ExpectedLoss el = new ExpectedLoss(exposures);
        assertEquals(el.totalExpectedLoss() / el.totalEad(),
                el.expectedLossRate(), 1e-9);
    }

    @Test
    void weightedPdUsesEadWeights() {
        var exposures = List.of(
                new Exposure(100, 0.0, 0.10),
                new Exposure(100, 0.0, 0.20));
        assertEquals(0.15, new ExpectedLoss(exposures).weightedPd(), 1e-9);
    }

    @Test
    void rejectsEmptyPortfolio() {
        assertThrows(IllegalArgumentException.class,
                () -> new ExpectedLoss(List.of()));
    }
}
