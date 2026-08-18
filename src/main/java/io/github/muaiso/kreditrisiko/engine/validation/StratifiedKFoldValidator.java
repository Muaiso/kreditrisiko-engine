package io.github.muaiso.kreditrisiko.engine.validation;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import io.github.muaiso.kreditrisiko.domain.LoanApplication;

/**
 * Stratifizierte K-Fold-Kreuzvalidierung.
 *
 * <p>Erhaeft die Klassenverteilung (Ausfall vs. Nicht-Ausfall) in jedem
 * Fold, indem pro Klasse separat in Bloecke aufgeteilt wird. Wichtig bei
 * unausgeglichenen Kreditdatensatzen mit seltenen Ausfaellen.</p>
 */
public final class StratifiedKFoldValidator implements CrossValidator {

    private final int k;
    private final long seed;

    /**
     * @param k    Anzahl der Folds (>= 2)
     * @param seed Startwert fuer die Reproduzierbarkeit
     */
    public StratifiedKFoldValidator(int k, long seed) {
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
        // Trennung nach Klasse erfolgt ueber die Applications, die der
        // Aufrufer ueber das Label liefert; hier verwenden wir daher die
        // uebergebene Labels-Liste indirekt ueber totalSize nicht - stattdessen
        // liefert der Consumer die Applications selbst. Wir nutzen daher eine
        // Hilfsmethode mit expliziten Labels.
        throw new UnsupportedOperationException(
                "Nutze validate(applications, foldConsumer) mit Labels");
    }

    /**
     * Stratifizierte Validierung mit Zugriff auf die Applications.
     *
     * @param applications der vollstaendige Datensatz
     * @param foldConsumer  wird je Fold mit den Indices aufgerufen
     */
    public void validate(List<LoanApplication> applications,
                         Consumer<Fold> foldConsumer) {
        List<Integer> posIdx = new ArrayList<>();
        List<Integer> negIdx = new ArrayList<>();
        for (int i = 0; i < applications.size(); i++) {
            if (applications.get(i).defaulted()) {
                posIdx.add(i);
            } else {
                negIdx.add(i);
            }
        }
        java.util.Random rng = new java.util.Random(seed);
        shuffle(posIdx, rng);
        shuffle(negIdx, rng);

        int posFold = posIdx.size() / k;
        int negFold = negIdx.size() / k;
        for (int f = 0; f < k; f++) {
            List<Integer> test = new ArrayList<>();
            test.addAll(slice(posIdx, f * posFold, nextEnd(posIdx.size(), f, k, posFold)));
            test.addAll(slice(negIdx, f * negFold, nextEnd(negIdx.size(), f, k, negFold)));
            List<Integer> train = new ArrayList<>();
            for (int i = 0; i < applications.size(); i++) {
                if (!test.contains(i)) {
                    train.add(i);
                }
            }
            foldConsumer.accept(new Fold(train, test));
        }
    }

    private void shuffle(List<Integer> list, java.util.Random rng) {
        for (int i = list.size() - 1; i > 0; i--) {
            int j = rng.nextInt(i + 1);
            int tmp = list.get(i);
            list.set(i, list.get(j));
            list.set(j, tmp);
        }
    }

    private List<Integer> slice(List<Integer> list, int from, int to) {
        return new ArrayList<>(list.subList(from, Math.min(to, list.size())));
    }

    private int nextEnd(int size, int fold, int k, int foldSize) {
        return fold == k - 1 ? size : (fold + 1) * foldSize;
    }
}
