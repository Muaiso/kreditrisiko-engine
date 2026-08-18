package io.github.muaiso.kreditrisiko.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Einfacher Status-Endpunkt (neben Actuator /health).
 */
@RestController
@RequestMapping("/api")
public final class HealthController {

    /**
     * @return Status-Informationen der Engine
     */
    @GetMapping("/status")
    public Map<String, Object> status() {
        return Map.of(
                "status", "UP",
                "engine", "kreditrisiko-engine",
                "version", "0.1.0");
    }
}
