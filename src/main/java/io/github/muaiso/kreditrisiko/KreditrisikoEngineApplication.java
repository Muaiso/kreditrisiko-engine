package io.github.muaiso.kreditrisiko;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Einstiegspunkt der Kreditrisiko-Scoring-Engine.
 *
 * <p>Spring-Boot-Anwendung mit REST-API zum Trainieren von
 * Klassifikationsmodellen und zur Bewertung von Kreditantraegen.</p>
 */
@SpringBootApplication
public class KreditrisikoEngineApplication {

    public static void main(String[] args) {
        SpringApplication.run(KreditrisikoEngineApplication.class, args);
    }
}
