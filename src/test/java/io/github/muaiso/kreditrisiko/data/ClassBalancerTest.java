package io.github.muaiso.kreditrisiko.data;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import io.github.muaiso.kreditrisiko.domain.CreditFeatures;
import io.github.muaiso.kreditrisiko.domain.LoanApplication;

import java.util.ArrayList;
import java.util.List;

/**
 * Testet den ClassBalancer.
 */
class ClassBalancerTest {

    private List<LoanApplication> imbalanced() {
        List<LoanApplication> apps = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            apps.add(new LoanApplication(new CreditFeatures(30, 50000, 1000, 4, "CAR"), false));
        }
        for (int i = 0; i < 2; i++) {
            apps.add(new LoanApplication(new CreditFeatures(52, 28000, 30000, 1, "OTHER"), true));
        }
        return apps;
    }

    @Test
    void balancesToEqualCounts() {
        var balanced = new ClassBalancer(1L).balance(imbalanced());
        long pos = balanced.stream().filter(LoanApplication::defaulted).count();
        assertEquals(pos, balanced.size() - pos, "beide Klassen gleich stark");
    }

    @Test
    void keepsOriginalEntries() {
        var src = imbalanced();
        var balanced = new ClassBalancer(1L).balance(src);
        assertTrue(balanced.size() >= src.size());
    }
}
