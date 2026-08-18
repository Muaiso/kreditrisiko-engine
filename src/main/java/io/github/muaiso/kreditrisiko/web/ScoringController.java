package io.github.muaiso.kreditrisiko.web;

import io.github.muaiso.kreditrisiko.domain.LoanApplication;
import io.github.muaiso.kreditrisiko.engine.models.CreditModel;
import io.github.muaiso.kreditrisiko.service.ScoringService;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

/**
 * REST-API für Kredit-Scoring.
 *
 * <p>Endpunkte:</p>
 * <ul>
 *   <li>{@code POST /api/score} – bewertet eine Anfrage</li>
 *   <li>{@code POST /api/train} – trainiert ein Modell und wertet es aus</li>
 * </ul>
 */
@RestController
@RequestMapping("/api")
public final class ScoringController {

    private final ScoringService service = new ScoringService();

    /**
     * Bewertet eine einzelne Kreditanfrage mit einem bereits trainierten Modell.
     */
    @PostMapping("/score")
    public ScoreResponse score(@RequestBody ScoreRequest request) {
        // Vereinfachte Demo: trainiere ein Logit-Modell auf synthetischen Daten
        List<LoanApplication> demo = demoData(42L);
        CreditModel model = service.train("LOGISTIC_REGRESSION", demo, 42L);
        return service.score(model, request.toDomain());
    }

    /**
     * Trainiert das angeforderte Modell und liefert Evaluierungskennzahlen.
     */
    @PostMapping("/train")
    public EvaluationResult train(@RequestBody TrainRequest request) {
        List<LoanApplication> apps = new ArrayList<>();
        for (TrainApplication a : request.applications()) {
            apps.add(a.toDomain());
        }
        long seed = request.seed() != null ? request.seed() : 1L;
        CreditModel model = service.train(request.algorithm(), apps, seed);
        return service.evaluate(model, apps);
    }

    private List<LoanApplication> demoData(long seed) {
        return new io.github.muaiso.kreditrisiko.data.SyntheticDataGenerator(seed).generate(200);
    }
}
