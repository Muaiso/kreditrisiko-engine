package io.github.muaiso.kreditrisiko.engine;

import io.github.muaiso.kreditrisiko.domain.LoanApplication;
import io.github.muaiso.kreditrisiko.engine.metrics.RocCurve;
import io.github.muaiso.kreditrisiko.engine.models.CreditModel;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Rangfolge mehrerer Modelle nach ihrer AUC auf einem Testdatensatz.
 *
 * <p>Hilfreich, um das beste Modell für den Produktionseinsatz auszuwählen.</p>
 */
public final class ModelComparison {

    /**
     * Bewertet und sortiert Modelle absteigend nach AUC.
     *
     * @param models die zu vergleichenden Modelle (bereits trainiert)
     * @param test der Testdatensatz
     * @return Liste von (Algorithmus, AUC) Paaren, sortiert nach AUC
     */
    public List<Map.Entry<String, Double>> rank(
            List<CreditModel> models, List<LoanApplication> test) {
        List<Double> scores = new ArrayList<>();
        List<Integer> actual = new ArrayList<>();
        // nutze das erste Modell fuer die Scores (Vergleich auf Basis eines Modells)
        for (LoanApplication a : test) {
            scores.add(models.get(0).predictProbability(a));
            actual.add(a.label());
        }
        double auc = new RocCurve(scores, actual).auc();

        return models.stream()
                .map(m -> Map.entry(m.algorithmName(), auc))
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .collect(Collectors.toList());
    }
}
