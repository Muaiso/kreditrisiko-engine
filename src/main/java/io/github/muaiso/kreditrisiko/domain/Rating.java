package io.github.muaiso.kreditrisiko.domain;

import java.util.Arrays;

/**
 * Bonitaetsstufe, abgeleitet aus der Ausfallwahrscheinlichkeit (PD).
 *
 * <p>Die Stufen folgen einer vereinfachten S&P-artigen Notation
 * (AAA = beste, D = Ausfall). Die Grenzen sind konfigurierbar.</p>
 */
public enum Rating {

    AAA,
    AA,
    A,
    BBB,
    BB,
    B,
    CCC,
    CC,
    C,
    D;

    /** PD-Obergrenzen je Stufe (aufsteigend zum Ausfall). */
    private static final double[] THRESHOLDS = {
            0.0005, 0.001, 0.002, 0.005, 0.012, 0.03, 0.07, 0.15, 0.30, 1.0
    };

    /**
     * Leitet das Rating aus einer Ausfallwahrscheinlichkeit ab.
     *
     * @param probabilityOfDefault PD in [0, 1]
     * @return die entsprechende Bonitaetsstufe
     */
    public static Rating fromPd(double probabilityOfDefault) {
        if (probabilityOfDefault < 0.0 || probabilityOfDefault > 1.0) {
            throw new IllegalArgumentException("PD muss in [0, 1] liegen");
        }
        for (int i = 0; i < THRESHOLDS.length; i++) {
            if (probabilityOfDefault <= THRESHOLDS[i]) {
                return values()[i];
            }
        }
        return D;
    }

    /**
     * @return die PD-Obergrenze dieser Stufe
     */
    public double threshold() {
        return THRESHOLDS[ordinal()];
    }

    /**
     * @return true wenn die Stufe als spekulativ (schlechter als BBB) gilt
     */
    public boolean isNonInvestmentGrade() {
        return ordinal() > BBB.ordinal();
    }

    /**
     * @return alle gueltigen Ratings in aufsteigender Reihenfolge
     */
    public static java.util.List<Rating> all() {
        return Arrays.asList(values());
    }
}
