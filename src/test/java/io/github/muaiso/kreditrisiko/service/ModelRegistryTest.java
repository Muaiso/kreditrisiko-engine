package io.github.muaiso.kreditrisiko.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import io.github.muaiso.kreditrisiko.domain.CreditFeatures;
import io.github.muaiso.kreditrisiko.domain.LoanApplication;
import io.github.muaiso.kreditrisiko.engine.models.BaselineModel;

import java.util.List;

/**
 * Testet die ModelRegistry.
 */
class ModelRegistryTest {

    @Test
    void registerAndRetrieve() {
        var registry = new ModelRegistry();
        var model = new BaselineModel(0.3);
        model.train(List.of(new LoanApplication(new CreditFeatures(30, 50000, 1000, 4, "CAR"), false)));
        registry.register("m1", model);
        assertEquals(true, registry.get("m1").isTrained());
        assertEquals(1, registry.size());
    }

    @Test
    void unknownModelThrows() {
        var registry = new ModelRegistry();
        assertThrows(IllegalArgumentException.class, () -> registry.get("nope"));
    }

    @Test
    void emptyIdRejected() {
        var registry = new ModelRegistry();
        var model = new BaselineModel(0.3);
        model.train(List.of(new LoanApplication(new CreditFeatures(30, 50000, 1000, 4, "CAR"), false)));
        assertThrows(IllegalArgumentException.class, () -> registry.register("", model));
    }
}
