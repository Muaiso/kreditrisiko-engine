package io.github.muaiso.kreditrisiko.engine.validation;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Time-Series-Split fuer zeitlich geordnete Kreditdatensaetze.
 *
 * <p>Bei zeitlicher Abhaengigkeit darf nicht zufaellig gemischt werden.
 * Stattdessen wird der Trainingsbereich schrittweise vergroessert und der
 * jeweils anschliessende Block als Testmenge genutzt (kein Leak aus der
 * Zukunft).</p>
 */
public final class TimeSeriesSplitValidator implements CrossValidator {

    private final int folds;

    /**
     * @param folds Anzahl der aufsteigenden Testfenster (>= 1)
     */
    public TimeSeriesSplitValidator(int folds) {
        if (folds < 1) {
            throw new IllegalArgumentException("folds muss >= 1 sein");
        }
        this.folds = folds;
    }

    @Override
    public void validate(Consumer<Fold> foldConsumer, int totalSize) {
        if (totalSize < folds + 1) {
            throw new IllegalArgumentException("Datensatz zu klein fuer " + folds + " Splits");
        }
        int step = totalSize / (folds + 1);
        for (int f = 1; f <= folds; f++) {
            int trainEnd = f * step;
            int testStart = trainEnd;
            int testEnd = Math.min(totalSize, (f + 1) * step);
            List<Integer> train = new ArrayList<>();
            List<Integer> test = new ArrayList<>();
            for (int i = 0; i < trainEnd; i++) {
                train.add(i);
            }
            for (int i = testStart; i < testEnd; i++) {
                test.add(i);
            }
            foldConsumer.accept(new Fold(train, test));
        }
    }
}
