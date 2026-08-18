# Contributing

## Architektur-Regeln

- `domain` enthaelt fachliche Modellierung ohne Abhaengigkeiten nach aussen.
- `engine` enthaelt Algorithmen (Modelle, Metriken, Validierung, Utilities).
  - `engine.models` – Klassifikationsmodelle (implementieren `CreditModel`).
  - `engine.metrics` – Guetemasse der Kreditrisiko-Modellierung.
  - `engine.validation` – Cross-Validation-Strategien (implementieren `CrossValidator`).
- `data` enthaelt Datenquellen (CSV, Generator, Balancing).
- `service` orchestriert Training/Scoring/Evaluierung.
- `web` enthaelt REST-Controller und DTOs.

## Tests

Jede neue Klasse erhaelt einen eigenen Unit-Test. Metriken werden gegen
bekannte Stichproben (z. B. perfekt trennbare Daten) geprueft. Modelle werden
auf Konvergenz und Reproduzierbarkeit getestet.

```bash
mvn test
```

## Commits

Atomare Commits: eine fachliche Aenderung pro Commit. Build muss lokal
(`mvn test`) gruen sein.
