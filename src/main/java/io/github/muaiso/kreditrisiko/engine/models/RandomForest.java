package io.github.muaiso.kreditrisiko.engine.models;

import io.github.muaiso.kreditrisiko.domain.LoanApplication;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Random Forest: Bagging ueber mehrere Entscheidungsbaeume.
 *
 * <p>Jeder Baum wird auf einem Bootstrap-Sample mit zufaelliger
 * Feature-Subsampling trainiert. Die Gesamtvorhersage ist der Mittelwert
 * der Einzelbaum-Wahrscheinlichkeiten (robuster als ein einzelner Baum).</p>
 */
public final class RandomForest implements CreditModel {

    private final int trees;
    private final int maxDepth;
    private final int minSamplesSplit;
    private final long seed;
    private List<DecisionTree> forest;
    private boolean trained;

    /**
     * @param trees          Anzahl der Baeume
     * @param maxDepth       maximale Tiefe je Baum
     * @param minSamplesSplit minimale Praesenz je Split
     * @param seed           Startwert fuer Bootstrap/Feature-Zufall
     */
    public RandomForest(int trees, int maxDepth, int minSamplesSplit, long seed) {
        if (trees < 1) {
            throw new IllegalArgumentException("trees muss >= 1 sein");
        }
        this.trees = trees;
        this.maxDepth = maxDepth;
        this.minSamplesSplit = minSamplesSplit;
        this.seed = seed;
    }

    @Override
    public void train(List<LoanApplication> applications) {
        if (applications == null || applications.isEmpty()) {
            throw new IllegalArgumentException("Trainingsdaten duerfen nicht leer sein");
        }
        Random rng = new Random(seed);
        this.forest = new ArrayList<>();
        int n = applications.size();
        for (int t = 0; t < trees; t++) {
            // Bootstrap-Sample
            List<LoanApplication> sample = new ArrayList<>();
            for (int i = 0; i < n; i++) {
                sample.add(applications.get(rng.nextInt(n)));
            }
            DecisionTree tree = new DecisionTree(maxDepth, minSamplesSplit);
            tree.train(sample);
            forest.add(tree);
        }
        this.trained = true;
    }

    @Override
    public double predictProbability(LoanApplication application) {
        if (!trained) {
            throw new IllegalStateException("Modell nicht trainiert");
        }
        double sum = 0.0;
        for (DecisionTree tree : forest) {
            sum += tree.predictProbability(application);
        }
        return sum / forest.size();
    }

    /** @return Anzahl der trainierten Baeume */
    public int treeCount() {
        return forest == null ? 0 : forest.size();
    }

    @Override
    public String algorithmName() {
        return "RANDOM_FOREST";
    }

    @Override
    public boolean isTrained() {
        return trained;
    }
}
