# Contributing

## Architektur-Regeln

- `domain` enthaelt fachliche Modellierung ohne Abhaengigkeiten nach aussen.
- `engine` enthaelt Algorithmen (Modelle, Metriken, Validierung).
- `data` enthaelt Laden/Erzeugen von Datensaetzen.
- `service` orchestriert fachliche Ablaeufe (zustandslos, thread-sicher).
- `web` ist die HTTP-Schicht (Controller, DTOs, Validierung).
- `config` und `exception` sind querschnittliche Belange.

Schichten duerfen nur nach innen (web -> service -> engine -> domain)
referenzieren, nie umgekehrt.

## Commit-Stil

```
<typ>(<bereich>): <kurze Beschreibung im Imperativ>

typ: feat | fix | refactor | test | docs | chore | ci
bereich: domain | engine | metrics | validation | data | service | web | config
```

Beispiele:
- `feat(engine): LogisticRegression mit Gradientenabstieg`
- `test(metrics): AUC ueber trapezoidale Integration`
- `docs: README mit Beispiel-Request`

Jeder Commit sollte kompilierfaehig und einzeln testbar sein.
