package io.github.muaiso.kreditrisiko.engine.models;

import io.github.muaiso.kreditrisiko.domain.LoanApplication;

import java.util.ArrayList;
import java.util.List;

/**
 * Entscheidungsbaum (CART) fuer binäre Klassifikation.
 *
 * <p>Rekursives Aufteilen der Features nach dem Gini-Gewinn. Bei kleinen
 * Teilmengen oder reinen Knoten wird gestoppt. Liefert eine
 * Wahrscheinlichkeit aus dem Klassenanteil im Blatt.</p>
 */
public final class DecisionTree implements CreditModel {

    private final int maxDepth;
    private final int minSamplesSplit;
    private Node root;
    private boolean trained;
    private FeatureAggregator aggregator;

    /**
     * @param maxDepth         maximale Baumtiefe
     * @param minSamplesSplit  minimale Praesenz je Split
     */
    public DecisionTree(int maxDepth, int minSamplesSplit) {
        this.maxDepth = maxDepth;
        this.minSamplesSplit = minSamplesSplit;
    }

    @Override
    public void train(List<LoanApplication> applications) {
        if (applications == null || applications.isEmpty()) {
            throw new IllegalArgumentException("Trainingsdaten duerfen nicht leer sein");
        }
        this.aggregator = new FeatureAggregator(applications);
        double[][] x = new double[applications.size()][];
        int[] y = new int[applications.size()];
        for (int i = 0; i < applications.size(); i++) {
            x[i] = aggregator.toVector(applications.get(i).features());
            y[i] = applications.get(i).label();
        }
        this.root = build(x, y, 0);
        this.trained = true;
    }

    private Node build(double[][] x, int[] y, int depth) {
        long pos = 0;
        for (int v : y) {
            pos += v;
        }
        // rein oder zu klein/tief -> Blatt
        if (y.length <= minSamplesSplit || depth >= maxDepth || pos == 0 || pos == y.length) {
            double p = y.length == 0 ? 0.5 : (double) pos / y.length;
            return new Node(-1, 0.0, p, null, null);
        }
        int bestFeat = -1;
        double bestThresh = 0.0;
        double bestGain = -1.0;
        int dim = x[0].length;
        for (int f = 0; f < dim; f++) {
            double[] vals = new double[y.length];
            for (int i = 0; i < y.length; i++) {
                vals[i] = x[i][f];
            }
            double mean = mean(vals);
            double gain = giniGain(y, splitBy(vals, mean));
            if (gain > bestGain) {
                bestGain = gain;
                bestFeat = f;
                bestThresh = mean;
            }
        }
        if (bestFeat == -1) {
            return new Node(-1, 0.0, (double) pos / y.length, null, null);
        }
        boolean[] leftMask = new boolean[y.length];
        for (int i = 0; i < y.length; i++) {
            leftMask[i] = x[i][bestFeat] <= bestThresh;
        }
        var leftX = select(x, leftMask, true);
        var leftY = select(y, leftMask, true);
        var rightX = select(x, leftMask, false);
        var rightY = select(y, leftMask, false);
        if (leftY.length == 0 || rightY.length == 0) {
            return new Node(-1, 0.0, (double) pos / y.length, null, null);
        }
        Node left = build(leftX, leftY, depth + 1);
        Node right = build(rightX, rightY, depth + 1);
        return new Node(bestFeat, bestThresh, (double) pos / y.length, left, right);
    }

    @Override
    public double predictProbability(LoanApplication application) {
        if (!trained) {
            throw new IllegalStateException("Modell nicht trainiert");
        }
        double[] x = aggregator.toVector(application.features());
        Node node = root;
        while (node.left != null && node.right != null) {
            node = (x[node.feature] <= node.threshold) ? node.left : node.right;
        }
        return node.probability;
    }

    private double giniGain(int[] y, int[] mask) {
        // mask: -1 = nicht zugeordnet, 0 = links, 1 = rechts
        int[] left = filter(y, mask, 0);
        int[] right = filter(y, mask, 1);
        if (left.length == 0 || right.length == 0) {
            return -1.0;
        }
        double parentGini = gini(y);
        double wL = (double) left.length / y.length;
        double wR = (double) right.length / y.length;
        return parentGini - wL * gini(left) - wR * gini(right);
    }

    private int[] splitBy(double[] vals, double thresh) {
        int[] mask = new int[vals.length];
        for (int i = 0; i < vals.length; i++) {
            mask[i] = vals[i] <= thresh ? 0 : 1;
        }
        return mask;
    }

    private double gini(int[] y) {
        long pos = 0;
        for (int v : y) {
            pos += v;
        }
        double p = y.length == 0 ? 0.5 : (double) pos / y.length;
        return 1.0 - p * p - (1.0 - p) * (1.0 - p);
    }

    private double mean(double[] v) {
        double s = 0;
        for (double x : v) {
            s += x;
        }
        return s / v.length;
    }

    private int[] filter(int[] y, int[] mask, int which) {
        List<Integer> out = new ArrayList<>();
        for (int i = 0; i < y.length; i++) {
            if (mask[i] == which) {
                out.add(y[i]);
            }
        }
        return out.stream().mapToInt(i -> i).toArray();
    }

    private double[][] select(double[][] x, boolean[] mask, boolean keep) {
        List<double[]> out = new ArrayList<>();
        for (int i = 0; i < x.length; i++) {
            if (mask[i] == keep) {
                out.add(x[i]);
            }
        }
        return out.toArray(new double[0][]);
    }

    private int[] select(int[] y, boolean[] mask, boolean keep) {
        List<Integer> out = new ArrayList<>();
        for (int i = 0; i < y.length; i++) {
            if (mask[i] == keep) {
                out.add(y[i]);
            }
        }
        return out.stream().mapToInt(i -> i).toArray();
    }

    private record Node(int feature, double threshold, double probability,
                        Node left, Node right) {
    }

    @Override
    public String algorithmName() {
        return "DECISION_TREE";
    }

    @Override
    public boolean isTrained() {
        return trained;
    }
}
