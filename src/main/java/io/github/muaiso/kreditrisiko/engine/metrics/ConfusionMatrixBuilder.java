package io.github.muaiso.kreditrisiko.engine.metrics;

import java.util.List;

/**
 * Hilfsfunktionen zum Aufbau einer {@link ConfusionMatrix} aus
 * Beobachtungen (tatsaechliche vs. vorhergesagte Klasse).
 */
public final class ConfusionMatrixBuilder {

    private ConfusionMatrixBuilder() {
    }

    /**
     * Erzeugt die Matrix aus parallelen Listen von Ist- und Soll-Klassen.
     *
     * @param actual   tatsaechliche Labels (0/1)
     * @param predicted vorhergesagte Labels (0/1)
     * @return die zugehoerige Confusion Matrix
     */
    public static ConfusionMatrix fromLabels(List<Integer> actual, List<Integer> predicted) {
        if (actual.size() != predicted.size()) {
            throw new IllegalArgumentException("Listen muessen gleich lang sein");
        }
        long tp = 0, fp = 0, tn = 0, fn = 0;
        for (int i = 0; i < actual.size(); i++) {
            int a = actual.get(i);
            int p = predicted.get(i);
            if (a == 1 && p == 1) {
                tp++;
            } else if (a == 0 && p == 1) {
                fp++;
            } else if (a == 0 && p == 0) {
                tn++;
            } else {
                fn++;
            }
        }
        return new ConfusionMatrix(tp, fp, tn, fn);
    }
}
