package io.github.muaiso.kreditrisiko.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Testet die Rating-Ableitung aus der Ausfallwahrscheinlichkeit.
 */
class RatingTest {

    @Test
    void lowPdIsHighestGrade() {
        assertEquals(Rating.AAA, Rating.fromPd(0.0001));
        assertEquals(Rating.AA, Rating.fromPd(0.0008));
    }

    @Test
    void highPdIsDefault() {
        assertEquals(Rating.D, Rating.fromPd(0.99));
        // PD 0.5 liegt ueber der C-Grenze (0.30) -> D
        assertEquals(Rating.D, Rating.fromPd(0.5));
    }

    @Test
    void boundaryPdMapsToThreshold() {
        // PD exakt an der BBB-Grenze (0.005) -> BBB
        assertEquals(Rating.BBB, Rating.fromPd(0.005));
    }

    @Test
    void rejectsOutOfRangePd() {
        assertThrows(IllegalArgumentException.class, () -> Rating.fromPd(-0.1));
        assertThrows(IllegalArgumentException.class, () -> Rating.fromPd(1.5));
    }

    @Test
    void nonInvestmentGradeDetection() {
        assertTrue(Rating.BB.isNonInvestmentGrade());
        assertTrue(Rating.AAA.isNonInvestmentGrade() == false);
    }

    @Test
    void thresholdMonotonicIncreasing() {
        double prev = -1.0;
        for (Rating r : Rating.all()) {
            assertTrue(r.threshold() > prev, "Schwellen sollten wachsen");
            prev = r.threshold();
        }
    }
}
