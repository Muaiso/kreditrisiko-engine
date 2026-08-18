package io.github.muaiso.kreditrisiko.engine;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * One-Hot-Encoding für kategorische Merkmale (z. B. Verwendungszweck).
 *
 * <p>Lernt die Menge der bekannten Kategorien aus den Trainingsdaten und
 * ordnet jeder Kategorie einen 0/1-Vektor zu. Unbekannte Kategorien zur
 * Inference-Zeit werden als komplett 0 kodiert (alle Merkmale fehlen).</p>
 */
public final class OneHotEncoder {

    private final List<String> categories;
    private final Map<String, Integer> index = new LinkedHashMap<>();

    /**
     * Lernt die Kategorien aus den uebergebenen Werten.
     *
     * @param values die kategorischen Werte (duplikate werden ignoriert)
     */
    public OneHotEncoder(List<String> values) {
        if (values == null || values.isEmpty()) {
            throw new IllegalArgumentException("Kategorien duerfen nicht leer sein");
        }
        int i = 0;
        for (String v : values) {
            index.putIfAbsent(v, i++);
        }
        this.categories = List.copyOf(index.keySet());
    }

    /**
     * @return Anzahl der codierten Spalten
     */
    public int dimension() {
        return categories.size();
    }

    /**
     * @return die gelernten Kategorien in Reihenfolge
     */
    public List<String> categories() {
        return categories;
    }

    /**
     * Kodiert einen Wert als 0/1-Vektor.
     *
     * @param value die Kategorie
     * @return Vektor der Laenge {@link #dimension()}
     */
    public double[] encode(String value) {
        double[] vec = new double[dimension()];
        Integer idx = index.get(value);
        if (idx != null) {
            vec[idx] = 1.0;
        }
        return vec;
    }
}
