package io.github.muaiso.kreditrisiko.engine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.muaiso.kreditrisiko.domain.CreditFeatures;
import io.github.muaiso.kreditrisiko.domain.LoanApplication;
import io.github.muaiso.kreditrisiko.engine.models.LogisticRegression;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.util.List;

class ModelSerializerTest {

    private LogisticRegression trained() {
        LogisticRegression lr = new LogisticRegression(0.1, 200, 0.001);
        lr.train(new io.github.muaiso.kreditrisiko.data.SyntheticDataGenerator(7L).generate(100));
        return lr;
    }

    @Test
    void roundTripPreservesPredictions() {
        LogisticRegression original = trained();
        ModelSerializer ser = new ModelSerializer();
        String json = ser.serialize(original);
        assertTrue(json.contains("LOGISTIC_REGRESSION"));
        assertTrue(json.contains("weights"));
        assertTrue(json.contains("bias"));

        LogisticRegression restored = ser.deserializeLogisticRegression(json);
        LoanApplication app = new LoanApplication(
                new CreditFeatures(35, 50000, 5000, 6, "CAR"), false);
        assertEquals(original.predictProbability(app),
                restored.predictProbability(app), 1e-12,
                "rekonstruiertes Modell liefert identische PD");
    }

    @Test
    void fileRoundTrip(@TempDir File dir) {
        LogisticRegression original = trained();
        ModelSerializer ser = new ModelSerializer();
        File file = new File(dir, "model.json");
        ser.writeToFile(original, file);
        assertTrue(file.exists());

        LogisticRegression restored = ser.readFromFile(file);
        LoanApplication app = new LoanApplication(
                new CreditFeatures(35, 50000, 5000, 6, "CAR"), false);
        assertEquals(original.predictProbability(app),
                restored.predictProbability(app), 1e-12);
    }

    @Test
    void rejectsUntrainedModel() {
        ModelSerializer ser = new ModelSerializer();
        assertThrows(IllegalStateException.class,
                () -> ser.serialize(new LogisticRegression(0.1, 10, 0.01)));
    }
}
