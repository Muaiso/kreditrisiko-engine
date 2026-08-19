package io.github.muaiso.kreditrisiko.engine.metrics;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.muaiso.kreditrisiko.domain.Exposure;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

class PortfolioRiskTest {

    @Test
    void equalExposureYieldsLowHhi() {
        var exposures = List.of(
                new Exposure(100, 0.5, 0.1),
                new Exposure(100, 0.5, 0.1),
                new Exposure(100, 0.5, 0.1),
                new Exposure(100, 0.5, 0.1));
        PortfolioRisk risk = new PortfolioRisk(exposures);
        assertEquals(0.25, risk.hhi(), 1e-9, "4 gleich grosse Positionen -> HHI = 1/4");
    }

    @Test
    void singleDominantExposureYieldsHighHhi() {
        var exposures = List.of(
                new Exposure(900, 0.5, 0.1),
                new Exposure(50, 0.5, 0.1),
                new Exposure(50, 0.5, 0.1));
        PortfolioRisk risk = new PortfolioRisk(exposures);
        assertTrue(risk.hhi() > 0.7, "eine dominierende Position -> hohes HHI, war: " + risk.hhi());
        assertEquals("hoch", risk.concentrationLevel());
    }

    @Test
    void segmentCountReflectsDistinctSegments() {
        var exposures = List.of(
                new Exposure(100, 0.5, 0.1),
                new Exposure(100, 0.5, 0.1),
                new Exposure(100, 0.5, 0.1));
        Map<Integer, String> seg = Map.of(0, "A", 1, "B", 2, "B");
        assertEquals(2, new PortfolioRisk(exposures, seg).segmentCount());
    }

    @Test
    void rejectsEmptyPortfolio() {
        assertThrows(IllegalArgumentException.class, () -> new PortfolioRisk(List.of()));
    }
}
