package io.github.muaiso.kreditrisiko.service;

import io.github.muaiso.kreditrisiko.domain.LoanApplication;
import io.github.muaiso.kreditrisiko.engine.models.CreditModel;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-Memory-Registry für trainierte Modelle.
 *
 * <p>Ermöglicht das mehrfache Scoring mit einem zuvor trainierten Modell,
 * ohne es bei jeder Anfrage neu trainieren zu muessen. Modelle sind über
 * eine ID adressierbar.</p>
 */
public final class ModelRegistry {

    private final Map<String, CreditModel> models = new ConcurrentHashMap<>();

    /**
     * Registriert ein trainiertes Modell unter einer ID.
     *
     * @param id eindeutige Modell-ID
     * @param model das trainierte Modell
     */
    public void register(String id, CreditModel model) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Modell-ID darf nicht leer sein");
        }
        models.put(id, model);
    }

    /**
     * Liefert ein registriertes Modell.
     *
     * @param id die Modell-ID
     * @return das Modell
     * @throws IllegalArgumentException wenn die ID unbekannt ist
     */
    public CreditModel get(String id) {
        CreditModel m = models.get(id);
        if (m == null) {
            throw new IllegalArgumentException("Unbekanntes Modell: " + id);
        }
        return m;
    }

    /**
     * @return Anzahl registrierter Modelle
     */
    public int size() {
        return models.size();
    }
}
