# Kreditrisiko-Scoring-Engine

[![Java](https://img.shields.io/badge/Java-25-ED8B00?logo=openjdk&logoColor=white)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1-6DB33F?logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![Build](https://github.com/Muaiso/kreditrisiko-engine/actions/workflows/ci.yml/badge.svg)](https://github.com/Muaiso/kreditrisiko-engine/actions/workflows/ci.yml)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)
[![Maven](https://img.shields.io/badge/build-maven-C71A36?logo=apachemaven&logoColor=white)](https://maven.apache.org/)

**Trainierbare Kreditrisiko-Klassifikation** – eine Spring-Boot-Engine, die
Kreditausfaelle (`default`) aus Antragsmerkmalen vorhersagt und die
Modellgüte mit banküblichen Kennzahlen bewertet: **ROC/AUC**, **PR-AUC**,
**KS-Statistik**, **Gini**, **Lift/Gain** sowie **Cross-Validation**.

> Wirtschaftsinformatik-Portfolio-Projekt. Reine Backend-/Engine-Anwendung
> mit REST-API, sauber geschichteter Architektur und umfangreicher
> Testabdeckung.

---

## Inhalt

- [Motivation](#motivation)
- [Funktionsumfang](#funktionsumfang)
- [Architektur](#architektur)
- [Domänenmodell](#domänenmodell)
- [API-Referenz](#api-referenz)
- [Lokales Starten](#lokales-starten)
- [Beispiel-Request](#beispiel-request)
- [Mathematische Grundlagen](#mathematische-grundlagen)
- [Tests](#tests)
- [Tech-Stack](#tech-stack)

---

## Motivation

Kreditinstitute muessen entscheiden, ob ein Antragstragendem Kredit gewaehrt
wird. Ein Scoring-Modell ordnet jeder Anfrage eine Ausfallwahrscheinlichkeit
(`probability of default`, PD) zu. Entscheidend ist nicht nur die Trefferquote,
sondern die **Rangordnung**: ein gutes Modell soll risikoreiche Antraege
zuverlaessig hoeher einstufen als sichere.

Diese Engine liefert:

1. Nachvollziehbare, von Grund auf implementierte Modelle (kein Black-Box-ML).
2. Die fuer Kreditportfolios massgeblichen Gütemaße (AUC, KS, Gini, Lift).
3. Valide Validierungsverfahren (Stratified K-Fold, Bootstrap, Time-Series).

---

## Funktionsumfang

| Funktion | Beschreibung |
|----------|--------------|
| Domänenmodell | `Borrower`, `LoanApplication`, `CreditFeatures`, `Rating` |
| Feature-Engineering | Min-Max/Standard-Normalisierung, One-Hot-Encoding, Aggregation |
| Baseline-Modell | Haeufigkeits- und Schwellwert-Klassifikator |
| Logistic Regression | Gradientenabstieg mit Regularisierung |
| Decision Tree | Rekursives Splitten nach Gini/Entropy |
| Random Forest | Bagging uer mehrere Baeume |
| Gaussian Naive Bayes | Klassenbedingte Gauss-Dichten |
| Ensemble (Voting) | Kombination mehrerer Modelle |
| ROC / AUC | TPR vs. FPR-Kurve und Flaeche |
| PR / AUC | Precision-Recall-Kurve und Flaeche |
| KS-Statistik | Max. kumulativer Abstand der Verteilungen |
| Gini / Lift / Gain | Diskriminierungs- und Anreicherungsmass |
| Calibration | Maechtigkeitskalibrierung (Brier, Reliability) |
| LogLoss | Kreuzentropie der Wahrscheinlichkeiten |
| Validierung | Holdout, K-Fold, Stratified K-Fold, Bootstrap, Time-Series-Split |
| Daten | CSV-Loader, synthetischer Generator, Balancing |

---

## Architektur

```text
┌─────────────────────────────────────────────────────────────┐
│  web (io.github.muaiso.kreditrisiko.web)                    │
│  REST-Controller, DTOs, Request/Response-Mapping            │
└───────────────────────────┬─────────────────────────────────┘
                            │ nutzt
┌───────────────────────────▼─────────────────────────────────┐
│  service (io.github.muaiso.kreditrisiko.service)            │
│  Training/Scoring-Orchestrierung                           │
└───────────────────────────┬─────────────────────────────────┘
                            │ nutzt
┌───────────────────────────▼─────────────────────────────────┐
│  engine (models, metrics, validation)                       │
│  Klassifikation, Guetemasse, Validierung                  │
└───────────────────────────┬─────────────────────────────────┘
                            │ nutzt
┌───────────────────────────▼─────────────────────────────────┐
│  data (io.github.muaiso.kreditrisiko.data)                  │
│  CSV-Loader, Generator, Balancing                          │
└───────────────────────────┬─────────────────────────────────┘
                            │ nutzt
┌───────────────────────────▼─────────────────────────────────┐
│  domain (io.github.muaiso.kreditrisiko.domain)              │
│  Borrower, LoanApplication, CreditFeatures, Rating          │
└─────────────────────────────────────────────────────────────┘
```

## Domänenmodell

- `Borrower` – Stammdaten (Alter, Einkommen, Schulden, Beschaeftigung).
- `LoanApplication` – konkreter Antrag mit Merkmalen und Ziel (`default`).
- `CreditFeatures` – numerische/ kategorische Merkmale einer Anfrage.
- `Rating` – abgeleitete Bonitaetsstufe aus der PD.

---

## API-Referenz

### `POST /api/v1/models/train`

Trainiert ein Modell auf uebergebenen Antraegen.

**Request-Body:**
```json
{
  "algorithm": "LOGISTIC_REGRESSION",
  "applications": [
    { "age": 34, "income": 48000, "debt": 12000, "employmentYears": 5,
      "purpose": "CAR", "default": false },
    { "age": 51, "income": 31000, "debt": 28000, "employmentYears": 1,
      "purpose": "OTHER", "default": true }
  ]
}
```

**Response (gekürzt):**
```json
{
  "algorithm": "LOGISTIC_REGRESSION",
  "trainedAt": "2026-08-16T19:00:00Z",
  "metrics": {
    "accuracy": 0.82,
    "precision": 0.79,
    "recall": 0.74,
    "f1": 0.76,
    "rocAuc": 0.88,
    "prAuc": 0.71,
    "ks": 0.61,
    "gini": 0.76
  }
}
```

### Weitere Endpunkte

- `POST /api/v1/models/score` – Einzelbewertung einer Anfrage
- `POST /api/v1/models/evaluate` – Metriken auf einem Testdatensatz
- `GET /health` – Actuator Health-Check
- `GET /v3/api-docs` – OpenAPI-Spezifikation (JSON)
- `GET /swagger-ui.html` – interaktive API-Dokumentation

---

## Lokales Starten

**Voraussetzungen:** JDK 25, Maven 3.9+.

```bash
mvn package
mvn spring-boot:run
# oder
java -jar target/kreditrisiko-engine-0.0.1-SNAPSHOT.jar
```

Die API ist dann unter `http://localhost:8080` erreichbar.

---

## Beispiel-Request

```bash
curl -X POST http://localhost:8080/api/v1/models/train \
  -H "Content-Type: application/json" \
  -d '{
    "algorithm": "RANDOM_FOREST",
    "applications": [
      { "age": 30, "income": 50000, "debt": 10000, "employmentYears": 4,
        "purpose": "CAR", "default": false },
      { "age": 45, "income": 30000, "debt": 25000, "employmentYears": 1,
        "purpose": "OTHER", "default": true }
    ]
  }'
```

---

## Mathematische Grundlagen

### Logistic Regression
$$ P(y=1 \mid x) = \sigma(w^\top x + b), \quad \sigma(z) = \frac{1}{1+e^{-z}} $$
Training via logistischem Gradientenabstieg mit L2-Regularisierung.

### ROC / AUC
Wahre Positivrate (TPR) gegen falsche Positivrate (FPR) ueber alle
Schwellwerte; AUC als trapezfoermig integrierte Flaeche.

### KS-Statistik
$$ KS = \max_t |\, F^+_{\text{kum}}(t) - F^-_{\text{kum}}(t)\,| $$
Maximaler Abstand der kumulierten Score-Verteilungen von Ausfall vs. Nicht-Ausfall.

### Gini
$$ \text{Gini} = 2 \cdot \text{AUC} - 1 $$

---

## Tests

Das Projekt enthaelt Unit- und Integrationstests auf allen Ebenen:

- **Domain:** Modell-Invarianten, Rating-Ableitung
- **Engine/Metrics:** jede Kennzahl gegen bekannte Stichproben
- **Engine/Models:** Konvergenz, Randfaelle, Reproduzierbarkeit
- **Validation:** Fold-Aufteilungen, Stratifizierung
- **Data:** CSV-Roundtrip, Generator-Eigenschaften
- **Web:** Controller (Validierung, Fehlerantworten)

```bash
mvn test          # nur Tests
mvn verify        # Tests + JaCoCo-Coverage
```

---

## Tech-Stack

| Bereich | Technologie |
|---------|------------|
| Sprache | Java 25 |
| Framework | Spring Boot 4.1 (WebMVC, Validation, Actuator) |
| API-Doku | springdoc-openapi / Swagger UI 2.8 |
| Build | Maven 3.9 |
| Tests | JUnit 5, AssertJ-Stil, MockMvc |
| Coverage | JaCoCo |
| CI | GitHub Actions (JDK 25) |
| Lizenz | MIT |

---

## Status

✅ Abgeschlossenes Portfolio-Projekt. Die Engine ist voll funktionsfaehig,
getestet und über die REST-API nutzbar.
