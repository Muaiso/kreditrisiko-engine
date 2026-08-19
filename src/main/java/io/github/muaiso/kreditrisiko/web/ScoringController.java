package io.github.muaiso.kreditrisiko.web;

import io.github.muaiso.kreditrisiko.domain.Exposure;
import io.github.muaiso.kreditrisiko.domain.LoanApplication;
import io.github.muaiso.kreditrisiko.engine.metrics.ExpectedLoss;
import io.github.muaiso.kreditrisiko.engine.metrics.PortfolioRisk;
import io.github.muaiso.kreditrisiko.engine.models.CreditModel;
import io.github.muaiso.kreditrisiko.service.ModelRegistry;
import io.github.muaiso.kreditrisiko.service.ScoringService;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * REST-API fuer Kredit-Scoring.
 *
 * <p>Endpunkte:</p>
 * <ul>
 *   <li>{@code POST /api/score} – bewertet eine Anfrage mit registriertem Modell</li>
 *   <li>{@code POST /api/train} – trainiert ein Modell und wertet es aus</li>
 *   <li>{@code POST /api/explain} – erklaert eine Vorhersage (Merkmalsbeitraege)</li>
 *   <li>{@code POST /api/portfolio} – erwarteter Verlust + Konzentration</li>
 *   <li>{@code POST /api/models/register} – trainiert & registriert ein Modell</li>
 *   <li>{@code GET /api/models/{id}/serialize} – Modell als JSON</li>
 * </ul>
 */
@RestController
@RequestMapping("/api")
public final class ScoringController {

    private final ScoringService service = new ScoringService();
    private final ModelRegistry registry = new ModelRegistry();

    /**
     * Bewertet eine Anfrage mit einem registrierten Modell.
     */
    @PostMapping("/score")
    public ScoreResponse score(@RequestParam String modelId,
                               @RequestBody ScoreRequest request) {
        CreditModel model = registry.get(modelId);
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

    /**
     * Erklaert eine Vorhersage ueber Merkmalsbeitraege (SHAP-aehnlich).
     */
    @PostMapping("/explain")
    public ExplainResponse explain(@RequestParam String modelId,
                                   @RequestBody ExplainRequest request) {
        CreditModel model = registry.get(modelId);
        LoanApplication app = request.toDomain();
        List<LoanApplication> background = demoData(42L);
        Map<String, Double> contributions = service.explain(model, app, background);
        return new ExplainResponse(model.predictProbability(app), 0.0, contributions);
    }

    /**
     * Bewertet ein Kreditportfolio (Expected Loss + Konzentration).
     */
    @PostMapping("/portfolio")
    public Map<String, Object> portfolio(@RequestBody PortfolioRequest request) {
        List<Exposure> exposures = new ArrayList<>();
        Map<Integer, String> segments = new LinkedHashMap<>();
        int i = 0;
        for (PortfolioExposure e : request.exposures()) {
            exposures.add(e.toDomain());
            if (e.segment() != null) {
                segments.put(i, e.segment());
            }
            i++;
        }
        PortfolioRisk risk = new PortfolioRisk(exposures, segments);
        ExpectedLoss el = risk.expectedLoss();
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("totalExpectedLoss", el.totalExpectedLoss());
        result.put("totalEad", el.totalEad());
        result.put("expectedLossRate", el.expectedLossRate());
        result.put("weightedPd", el.weightedPd());
        result.put("weightedLgd", el.weightedLgd());
        result.put("hhi", risk.hhi());
        result.put("concentrationLevel", risk.concentrationLevel());
        result.put("segmentCount", risk.segmentCount());
        return result;
    }

    /**
     * Trainiert ein Modell und registriert es unter der uebergebenen ID.
     */
    @PostMapping("/models/register")
    public Map<String, Object> register(@RequestParam String modelId,
                                        @RequestBody TrainRequest request) {
        List<LoanApplication> apps = new ArrayList<>();
        for (TrainApplication a : request.applications()) {
            apps.add(a.toDomain());
        }
        long seed = request.seed() != null ? request.seed() : 1L;
        CreditModel model = service.train(request.algorithm(), apps, seed);
        registry.register(modelId, model);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("modelId", modelId);
        result.put("algorithm", model.algorithmName());
        result.put("registeredModels", registry.size());
        return result;
    }

    /**
     * Liefert ein registriertes Modell als JSON.
     */
    @PostMapping("/models/serialize")
    public Map<String, String> serialize(@RequestParam String modelId) {
        CreditModel model = registry.get(modelId);
        Map<String, String> result = new LinkedHashMap<>();
        result.put("modelId", modelId);
        result.put("json", service.serializeModel(model));
        return result;
    }

    private List<LoanApplication> demoData(long seed) {
        return new io.github.muaiso.kreditrisiko.data.SyntheticDataGenerator(seed).generate(200);
    }
}
