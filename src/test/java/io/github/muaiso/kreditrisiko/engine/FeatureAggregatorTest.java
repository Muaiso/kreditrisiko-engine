package io.github.muaiso.kreditrisiko.engine;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import io.github.muaiso.kreditrisiko.domain.CreditFeatures;
import io.github.muaiso.kreditrisiko.domain.LoanApplication;

import java.util.List;

/**
 * Testet die Feature-Aggregation zu einem konsistenten Vektor.
 */
class FeatureAggregatorTest {

    private List<LoanApplication> data() {
        return List.of(
                new LoanApplication(new CreditFeatures(30, 50000, 1000, 4, "CAR"), false),
                new LoanApplication(new CreditFeatures(45, 30000, 25000, 1, "OTHER"), true),
                new LoanApplication(new CreditFeatures(28, 62000, 8000, 7, "HOUSE"), false));
    }

    @Test
    void vectorSizeIsNumericPlusCategories() {
        var agg = new FeatureAggregator(data());
        // 5 numerisch + 3 Kategorien
        assertEquals(8, agg.vectorSize());
    }

    @Test
    void vectorContainsFeaturesAndOneHot() {
        var agg = new FeatureAggregator(data());
        double[] v = agg.toVector(new CreditFeatures(30, 50000, 1000, 4, "CAR"));
        // numerisch: age=30, income, debt, employment, dti
        assertEquals(30.0, v[0], 1e-9);
        // One-Hot an Position 5 = CAR
        assertEquals(1.0, v[5], 1e-9);
        assertEquals(0.0, v[6], 1e-9);
        assertEquals(0.0, v[7], 1e-9);
    }

    @Test
    void unknownCategoryEncodesZero() {
        var agg = new FeatureAggregator(data());
        double[] v = agg.toVector(new CreditFeatures(30, 50000, 1000, 4, "EDU"));
        // alle kategorischen Spalten 0
        assertEquals(0.0, v[5], 1e-9);
        assertEquals(0.0, v[6], 1e-9);
        assertEquals(0.0, v[7], 1e-9);
    }
}
