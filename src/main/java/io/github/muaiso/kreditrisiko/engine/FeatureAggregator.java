package io.github.muaiso.kreditrisiko.engine;

import io.github.muaiso.kreditrisiko.domain.CreditFeatures;
import io.github.muaiso.kreditrisiko.domain.LoanApplication;

import java.util.ArrayList;
import java.util.List;

/**
 * Aggregiert die Rohmerkmale einer Anfrage zu einem fixen Feature-Vektor.
 *
 * <p>Kombiniert numerische Merkmale (normalisiert), das DTI und die
 * One-Hot-kodierten Kategorien zu einem konsistenten Eingabevektor fuer
 * die Modelle. Die Kodierung wird aus den Trainingsdaten gelernt.</p>
 */
public final class FeatureAggregator {

    private final OneHotEncoder purposeEncoder;

    /**
     * Lernt die Kodierung aus einem Trainingsdatensatz.
     *
     * @param applications die Trainingsanfragen
     */
    public FeatureAggregator(List<LoanApplication> applications) {
        if (applications == null || applications.isEmpty()) {
            throw new IllegalArgumentException("Trainingsdaten duerfen nicht leer sein");
        }
        List<String> purposes = new ArrayList<>();
        for (LoanApplication a : applications) {
            purposes.add(a.features().purpose());
        }
        this.purposeEncoder = new OneHotEncoder(purposes);
    }

    /**
     * @return Laenge des erzeugten Vektors (5 numerisch + Kategorien)
     */
    public int vectorSize() {
        return 5 + purposeEncoder.dimension();
    }

    /**
     * Wandelt eine Anfrage in den Feature-Vektor um.
     *
     * @param f die Kreditmerkmale
     * @return Feature-Vektor
     */
    public double[] toVector(CreditFeatures f) {
        double[] vec = new double[vectorSize()];
        List<Double> numeric = f.numericVector();
        for (int i = 0; i < numeric.size(); i++) {
            vec[i] = numeric.get(i);
        }
        double[] oneHot = purposeEncoder.encode(f.purpose());
        System.arraycopy(oneHot, 0, vec, 5, oneHot.length);
        return vec;
    }
}
