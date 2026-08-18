package io.github.muaiso.kreditrisiko.engine.validation;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Holdout-Validierung: ein festgelegter Anteil wird als Testdatensatz
 * zurueckgehalten, der Rest dient zum Training.
 *
 * <p>Reproduzierbar ueber einen Seed. Der Testanteil wird vom Ende des
 * (gemischten) Datensatzes genommen.</p>
 */
public final class HoldoutValidator implements CrossValidator {

    private final double testFraction;
    private final long seed;

    /**
     * @param testFraction Anteil des Tests (0 &lt; f &lt; 1)
     * @param seed         Startwert fuer die Reproduzierbarkeit
     */
    public HoldoutValidator(double testFraction, long seed) {
        if (testFraction <= 0.0 || testFraction >= 1.0) {
            throw new IllegalArgumentException("testFraction muss in (0, 1) liegen");
        }
        this.testFraction = testFraction;
        this.seed = seed;
    }

    @Override
    public void validate(Consumer<Fold> foldConsumer, int totalSize) {
        if (totalSize < 2) {
            throw new IllegalArgumentException("Datensatz zu klein");
        }
        List<Integer> indices = new ArrayList<>();
        for (int i = 0; i < totalSize; i++) {
            indices.add(i);
        }
        java.util.Random rng = new java.util.Random(seed);
        for (int i = totalSize - 1; i > 0; i--) {
            int j = rng.nextInt(i + 1);
            int tmp = indices.get(i);
            indices.set(i, indices.get(j));
            indices.set(j, tmp);
        }
        int testSize = (int) Math.round(totalSize * testFraction);
        List<Integer> test = new ArrayList<>(indices.subList(0, testSize));
        List<Integer> train = new ArrayList<>(indices.subList(testSize, totalSize));
        foldConsumer.accept(new Fold(train, test));
    }
}
