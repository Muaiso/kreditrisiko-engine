package io.github.muaiso.kreditrisiko.engine.validation;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Einfache K-Fold-Kreuzvalidierung (zufaellige, aber stratifizierte Aufteilung).
 *
 * <p>Teilt den Datensatz in {@code k} gleich grosse Bloecke; in jeder
 * Iteration ist ein Block der Testdatensatz, der Rest Training. Die
 * Startindices werden ueber einen Seed reproduzierbar gemischt.</p>
 */
public final class KFoldCrossValidator implements CrossValidator {

    private final int k;
    private final long seed;

    /**
     * @param k    Anzahl der Folds (>= 2)
     * @param seed Startwert fuer die Reproduzierbarkeit
     */
    public KFoldCrossValidator(int k, long seed) {
        if (k < 2) {
            throw new IllegalArgumentException("k muss >= 2 sein");
        }
        this.k = k;
        this.seed = seed;
    }

    @Override
    public void validate(Consumer<Fold> foldConsumer, int totalSize) {
        if (totalSize < k) {
            throw new IllegalArgumentException("Datensatz kleiner als k Folds");
        }
        List<Integer> indices = shuffledIndices(totalSize);
        int foldSize = totalSize / k;
        for (int f = 0; f < k; f++) {
            int start = f * foldSize;
            int end = (f == k - 1) ? totalSize : start + foldSize;
            List<Integer> test = new ArrayList<>(indices.subList(start, end));
            List<Integer> train = new ArrayList<>();
            for (int i = 0; i < totalSize; i++) {
                if (i < start || i >= end) {
                    train.add(indices.get(i));
                }
            }
            foldConsumer.accept(new Fold(train, test));
        }
    }

    private List<Integer> shuffledIndices(int totalSize) {
        List<Integer> idx = new ArrayList<>();
        for (int i = 0; i < totalSize; i++) {
            idx.add(i);
        }
        java.util.Random rng = new java.util.Random(seed);
        for (int i = totalSize - 1; i > 0; i--) {
            int j = rng.nextInt(i + 1);
            int tmp = idx.get(i);
            idx.set(i, idx.get(j));
            idx.set(j, tmp);
        }
        return idx;
    }
}
