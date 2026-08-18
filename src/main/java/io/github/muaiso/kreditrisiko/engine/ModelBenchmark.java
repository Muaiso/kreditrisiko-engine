package io.github.muaiso.kreditrisiko.engine;

import io.github.muaiso.kreditrisiko.domain.LoanApplication;
import io.github.muaiso.kreditrisiko.engine.models.BaselineModel;
import io.github.muaiso.kreditrisiko.engine.models.CreditModel;
import io.github.muaiso.kreditrisiko.engine.models.DecisionTree;
import io.github.muaiso.kreditrisiko.engine.models.GaussianNaiveBayes;
import io.github.muaiso.kreditrisiko.engine.models.LogisticRegression;
import io.github.muaiso.kreditrisiko.engine.models.RandomForest;
import io.github.muaiso.kreditrisiko.engine.validation.CrossValidator;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Benchmark-Hilfsmittel: trainiert mehrere Modelle und vergleicht ihre
 * durchschnittliche AUC über Cross-Validation-Folds.
 */
public final class ModelBenchmark {

    private final CrossValidator validator;
    private final long seed;

    /**
     * @param validator die zu nutzende Validierungsstrategie
     * @param seed Startwert für reproduzierbare Modelle
     */
    public ModelBenchmark(CrossValidator validator, long seed) {
        this.validator = validator;
        this.seed = seed;
    }

    /**
     * @return eine Map von Algorithmus-Name zu durchschnittlicher AUC
     */
    public Map<String, Double> compare(List<LoanApplication> data) {
        List<CreditModel> models = List.of(
                new BaselineModel(0.3),
                new LogisticRegression(0.1, 100, 0.01),
                new DecisionTree(6, 2),
                new RandomForest(10, 5, 2, seed),
                new GaussianNaiveBayes());

        Map<String, Double> result = new LinkedHashMap<>();
        for (CreditModel m : models) {
            result.put(m.algorithmName(), averageAuc(m, data));
        }
        return result;
    }

    private double averageAuc(CreditModel model, List<LoanApplication> data) {
        List<Double> aucs = new ArrayList<>();
        validator.validate(fold -> {
            List<LoanApplication> train = select(data, fold.trainIndices());
            List<LoanApplication> test = select(data, fold.testIndices());
            model.train(train);
            double auc = evaluateAuc(model, test);
            aucs.add(auc);
        }, data.size());
        return aucs.stream().mapToDouble(Double::doubleValue).average().orElse(0.5);
    }

    private double evaluateAuc(CreditModel model, List<LoanApplication> test) {
        List<Double> scores = new ArrayList<>();
        List<Integer> actual = new ArrayList<>();
        for (LoanApplication a : test) {
            scores.add(model.predictProbability(a));
            actual.add(a.label());
        }
        return new io.github.muaiso.kreditrisiko.engine.metrics.RocCurve(scores, actual).auc();
    }

    private List<LoanApplication> select(List<LoanApplication> data, List<Integer> indices) {
        List<LoanApplication> out = new ArrayList<>();
        for (int i : indices) {
            out.add(data.get(i));
        }
        return out;
    }
}
