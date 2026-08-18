package io.github.muaiso.kreditrisiko.engine.validation;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Bootstrap-Validierung (Sampling mit Zuruecklegen).
 *
 * <p>Erzeugt je Iteration einen Trainingsdatensatz durch Ziehen von
 * {@code totalSize} Indizes mit Zuruecklegen. Als Out-of-Bag-Testmenge
 * dienen alle Indizes, die nicht gezogen wurden. Liefert eine stabile
 * Varianzschaetzung der Modellguete.</p>
 */
public final class BootstrapValidator implements CrossValidator {

    private final int iterations;
    private final long seed;

    /**
     * @param iterations Anzahl der Bootstrap-Durchlaeufe (>= 1)
     * @param seed       Startwert fuer die Reproduzierbarkeit
     */
    public BootstrapValidator(int iterations, long seed) {
        if (iterations < 1) {
            throw new IllegalArgumentException("iterations muss >= 1 sein");
        }
        this.iterations = iterations;
        this.seed = seed;
    }

    @Override
    public void validate(Consumer<Fold> foldConsumer, int totalSize) {
        if (totalSize < 1) {
            throw new IllegalArgumentException("Datensatz leer");
        }
        java.util.Random rng = new java.util.Random(seed);
        for (int it = 0; it < iterations; it++) {
            boolean[] drawn = new boolean[totalSize];
            List<Integer> train = new ArrayList<>();
            for (int i = 0; i < totalSize; i++) {
                int idx = rng.nextInt(totalSize);
                drawn[idx] = true;
                train.add(idx);
            }
            List<Integer> test = new ArrayList<>();
            for (int i = 0; i < totalSize; i++) {
                if (!drawn[i]) {
                    test.add(i);
                }
            }
            // falls durch Zufall alle gezogen wurden, nehmen wir Index 0 als OOB
            if (test.isEmpty()) {
                test.add(0);
            }
            foldConsumer.accept(new Fold(train, test));
        }
    }
}
