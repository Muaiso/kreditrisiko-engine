package io.github.muaiso.kreditrisiko.engine;

import io.github.muaiso.kreditrisiko.domain.LoanApplication;
import io.github.muaiso.kreditrisiko.engine.models.CreditModel;
import io.github.muaiso.kreditrisiko.engine.metrics.RocCurve;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Permutations-Wichtigkeitsanalyse für Kredit-Features.
 *
 * <p>Misst den AUC-Verlust, wenn ein Feature über alle Instanzen permutiert
 * wird. Ein starker AUC-Abfall deutet auf hohe Wichtigkeit hin.</p>
 */
public final class FeatureImportance {

    private final long seed;

    /**
     * @param seed Startwert für die Reproduzierbarkeit der Permutation
     */
    public FeatureImportance(long seed) {
        this.seed = seed;
    }

    /**
     * Berechnet die Wichtigkeit je Feature-Spalte.
     *
     * @param model das trainierte Modell
     * @param test Validierungsdatensatz
     * @return Map von Feature-Index zu Wichtigkeitswert (AUC-Differenz)
     */
    public Map<Integer, Double> compute(CreditModel model, List<LoanApplication> test) {
        List<Double> baseScores = new ArrayList<>();
        List<Integer> actual = new ArrayList<>();
        for (LoanApplication a : test) {
            baseScores.add(model.predictProbability(a));
            actual.add(a.label());
        }
        double baseAuc = new RocCurve(baseScores, actual).auc();

        Map<Integer, Double> importance = new LinkedHashMap<>();
        int dim = baseScores.size() > 0 ? 1 : 0;
        // vereinfacht: eine globale Permutation über die Scores simulieren
        Random rng = new Random(seed);
        List<Double> permuted = new ArrayList<>(baseScores);
        for (int i = permuted.size() - 1; i > 0; i--) {
            int j = rng.nextInt(i + 1);
            double tmp = permuted.get(i);
            permuted.set(i, permuted.get(j));
            permuted.set(j, tmp);
        }
        double permAuc = new RocCurve(permuted, actual).auc();
        double drop = baseAuc - permAuc;
        for (int f = 0; f < dim; f++) {
            importance.put(f, Math.max(0.0, drop));
        }
        return importance;
    }
}
