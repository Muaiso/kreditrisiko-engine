package io.github.muaiso.kreditrisiko.engine;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.github.muaiso.kreditrisiko.domain.LoanApplication;
import io.github.muaiso.kreditrisiko.engine.models.CreditModel;
import io.github.muaiso.kreditrisiko.engine.models.LogisticRegression;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Persistenz trainierter Modelle als JSON.
 *
 * <p>Banken muessen Modelle versionieren und archivieren (Modell-Governance,
 * Audit-Trail). Dieser Serializer schreibt die relevanten Modellparameter
 * (Typ + Gewichte) als menschenlesbares JSON und laedt sie zurueck, sodass
 * ein trainiertes Modell ohne erneutes Training eingesetzt werden kann.</p>
 */
public final class ModelSerializer {

    private final ObjectMapper mapper = new ObjectMapper();

    /**
     * Serialisiert ein unterstuetztes Modell nach JSON.
     *
     * @param model das trainierte Modell
     * @return JSON-Repraesentation
     */
    public String serialize(CreditModel model) {
        if (model == null || !model.isTrained()) {
            throw new IllegalStateException("Nur trainierte Modelle sind serialisierbar");
        }
        ObjectNode root = mapper.createObjectNode();
        root.put("algorithm", model.algorithmName());
        if (model instanceof LogisticRegression lr) {
            ArrayNode weights = mapper.createArrayNode();
            for (double w : lr.getWeights()) {
                weights.add(w);
            }
            root.set("weights", weights);
            root.put("bias", lr.getBias());
            ArrayNode purposes = mapper.createArrayNode();
            for (String p : lr.getPurposes()) {
                purposes.add(p);
            }
            root.set("purposes", purposes);
        }
        try {
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(root);
        } catch (IOException e) {
            throw new IllegalStateException("Serialisierung fehlgeschlagen", e);
        }
    }

    /**
     * Schreibt ein Modell in eine Datei.
     *
     * @param model das trainierte Modell
     * @param file  Zieldatei
     */
    public void writeToFile(CreditModel model, File file) {
        try {
            mapper.writerWithDefaultPrettyPrinter().writeValue(file, mapper.readTree(serialize(model)));
        } catch (IOException e) {
            throw new IllegalStateException("Schreiben fehlgeschlagen: " + file, e);
        }
    }

    /**
     * Laedt ein {@link LogisticRegression}-Modell zurueck.
     *
     * @param json JSON-Repraesentation
     * @return rekonstruiertes Modell (trainiert)
     */
    public LogisticRegression deserializeLogisticRegression(String json) {
        try {
            ObjectNode root = (ObjectNode) mapper.readTree(json);
            if (!"LOGISTIC_REGRESSION".equals(root.get("algorithm").asText())) {
                throw new IllegalArgumentException("Unbekanntes Modell: " + root.get("algorithm"));
            }
            var arr = root.get("weights");
            double[] weights = new double[arr.size()];
            for (int i = 0; i < arr.size(); i++) {
                weights[i] = arr.get(i).asDouble();
            }
            double bias = root.get("bias").asDouble();
            List<String> purposes = new java.util.ArrayList<>();
            if (root.has("purposes")) {
                for (var p : root.get("purposes")) {
                    purposes.add(p.asText());
                }
            }
            return new LogisticRegression(weights, bias, purposes);
        } catch (IOException e) {
            throw new IllegalStateException("Lesen fehlgeschlagen", e);
        }
    }

    /**
     * Laedt ein Modell aus einer Datei.
     *
     * @param file Quelldatei
     * @return rekonstruiertes Logistic-Regression-Modell
     */
    public LogisticRegression readFromFile(File file) {
        try {
            return deserializeLogisticRegression(java.nio.file.Files.readString(file.toPath()));
        } catch (IOException e) {
            throw new IllegalStateException("Lesen fehlgeschlagen: " + file, e);
        }
    }
}
