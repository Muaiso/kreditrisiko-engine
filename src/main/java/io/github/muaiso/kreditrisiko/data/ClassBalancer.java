package io.github.muaiso.kreditrisiko.data;

import io.github.muaiso.kreditrisiko.domain.LoanApplication;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Gleicht die Klassenverteilung von Ausfall / Nicht-Ausfall aus.
 *
 * <p>Im Kreditrisiko sind Ausfaelle selten. Diese Hilfsklasse ueber-
 * oder unterabtaktet die Minderheitsklasse, sodass beide Klassen
 * etwa gleich stark vertreten sind (naiveres Oversampling mit Zuruecklegen).</p>
 */
public final class ClassBalancer {

    private final long seed;

    /**
     * @param seed Startwert fuer die Reproduzierbarkeit
     */
    public ClassBalancer(long seed) {
        this.seed = seed;
    }

    /**
     * Bringt beide Klassen auf die Groesse der Majoritaetsklasse.
     *
     * @param applications die (evtl. unausgewogenen) Anfragen
     * @return ausgeglichener Datensatz
     */
    public List<LoanApplication> balance(List<LoanApplication> applications) {
        List<LoanApplication> pos = new ArrayList<>();
        List<LoanApplication> neg = new ArrayList<>();
        for (LoanApplication a : applications) {
            if (a.defaulted()) {
                pos.add(a);
            } else {
                neg.add(a);
            }
        }
        int target = Math.max(pos.size(), neg.size());
        Random rng = new Random(seed);
        List<LoanApplication> result = new ArrayList<>(applications);
        oversample(pos, target, rng, result);
        oversample(neg, target, rng, result);
        return result;
    }

    private void oversample(List<LoanApplication> minority, int target,
                            Random rng, List<LoanApplication> sink) {
        while (minority.size() < target) {
            sink.add(minority.get(rng.nextInt(minority.size())));
        }
    }
}
