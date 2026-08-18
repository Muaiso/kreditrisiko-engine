# Changelog

Alle wesentlichen Aenderungen an diesem Projekt werden in dieser Datei
dokumentiert. Das Format lehnt sich an [Keep a Changelog](https://keepachangelog.com/)
an.

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
