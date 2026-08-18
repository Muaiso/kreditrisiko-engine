# Changelog

Alle wesentlichen Aenderungen an diesem Projekt werden in dieser Datei
dokumentiert. Das Format lehnt sich an [Keep a Changelog](https://keepachangelog.com/)
an.

## [0.1.0] – 2026-08-16

### Hinzugefuegt
- Projekt-Scaffold: Spring Boot 4.1, Java 25, Maven, OpenAPI/Swagger
- Domänenmodell: `Borrower`, `LoanApplication`, `CreditFeatures`, `Rating`
- Feature-Engineering: Normalisierung, One-Hot-Encoding, Aggregation
- Klassifikationsmodelle: Baseline, LogisticRegression, DecisionTree, RandomForest, GaussianNB, Ensemble
- Metriken: Confusion Matrix, Accuracy, Precision, Recall, F1, ROC/AUC, PR/AUC, KS, Gini, Lift, Gain, Calibration, LogLoss
- Validierung: Holdout, KFold, StratifiedKFold, Bootstrap, TimeSeriesSplit
- Daten: CSV-Loader, synthetischer Generator, Balancing (SMOTE-like, Undersampling)
- REST-API: Train, Score, Evaluate, Metriken
- Umfassende Unit-/Integrationstests
