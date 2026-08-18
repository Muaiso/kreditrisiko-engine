package io.github.muaiso.kreditrisiko.web;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Testet HealthController.
 */
class HealthControllerTest {

    @Test
    void returnsUpStatus() {
        var ctrl = new HealthController();
        var status = ctrl.status();
        assertEquals("UP", status.get("status"));
        assertTrue(status.containsKey("engine"));
        assertTrue(status.containsKey("version"));
    }
}
