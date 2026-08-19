# Changelog

Alle wesentlichen Aenderungen an diesem Projekt werden in dieser Datei
dokumentiert. Das Format lehnt sich an [Keep a Changelog](https://keepachangelog.com/)
an.

## [0.2.0] – 2026-08-19

### Hinzugefuegt
- **Exposure-Modell** (`domain.Exposure`): IRB-Groessen EAD/LGD/PD.
- **Expected Loss** (`engine.metrics.ExpectedLoss`): EL = EAD · LGD · PD mit EAD-gewichteten Kennzahlen.
- **Portfolio Risk** (`engine.metrics.PortfolioRisk`): aggregierte EL, HHI-Konzentration, Segmentanalyse.
- **Scorecard-Modell** (`engine.models.ScorecardModel`): Punkte-basierte, auditierbare Scorecard aus Logit.
- **SHAP-Explainer** (`engine.ShapExplainer`): lokale Merkmalsbeitraege zur Modell-Erklaerbarkeit (EU AI Act).
- **Kostenoptimale Schwelle** (`engine.CostSensitiveThreshold`): erwartungswert-minimierende Entscheidungsschwelle.
- **Population Stability Index** (`engine.metrics.PopulationStabilityIndex`): Modell-Drift-Monitoring (PSI).
- **Modell-Serialisierung** (`engine.ModelSerializer`): JSON-Persistenz trainierter Logit-Modelle (Governance/Audit).
- **REST-Erweiterung**: `/api/explain`, `/api/portfolio`, `/api/models/register`, `/api/models/serialize`; `ModelRegistry`-Verdrahtung in `/api/score`.

## [0.1.0] – 2026-08-18

### Hinzugefuegt
- Domänenmodell: `Borrower`, `LoanApplication`, `CreditFeatures`, `Rating`.
- Feature-Engineering: `FeatureNormalizer`, `StandardScaler`, `OneHotEncoder`, `FeatureAggregator`.
- Modelle: `BaselineModel`, `LogisticRegression`, `DecisionTree`, `RandomForest`, `GaussianNaiveBayes`, `EnsembleModel`.
- Metriken: `ConfusionMatrix`, `RocCurve`, `PrCurve`, `KsStatistic`, `LiftGain`, `Calibration`, `LogLoss`.
- Validierung: `KFoldCrossValidator`, `StratifiedKFoldValidator`, `HoldoutValidator`, `BootstrapValidator`, `TimeSeriesSplitValidator`.
- Daten: `CsvDataLoader`, `SyntheticDataGenerator`, `ClassBalancer`.
- Engine-Utilities: `ModelBenchmark`, `ThresholdOptimizer`, `FeatureImportance`, `ModelComparison`.
- REST-API: `ScoringController` mit `/api/train` und `/api/score`.
- Service-Fassade: `ScoringService` (Train/Score/Evaluate).
- DTOs: `TrainRequest`, `TrainApplication`, `ScoreRequest`, `ScoreResponse`, `EvaluationResult`.
- Umfangreiche Unit-Tests auf allen Ebenen.
- Spring Boot 4.1 / Java 25 Setup, OpenAPI-Dokumentation, CI-Workflow.
