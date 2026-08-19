package io.github.muaiso.kreditrisiko.domain;

/**
 * Kreditengagement (Exposure at Default) eines einzelnen Vertrags.
 *
 * <p>Buendelt die drei Groessen der IRB-Kreditrisikomaße:</p>
 * <ul>
 *   <li>{@code ead} – Exposure at Default (Forderung zum Ausfallzeitpunkt)</li>
 *   <li>{@code lgd} – Loss Given Default (Verlustquote bei Ausfall, 0..1)</li>
 *   <li>{@code pd}  – Probability of Default (Ausfallwahrscheinlichkeit, 0..1)</li>
 * </ul>
 *
 * <p>Aus diesen drei Groessen folgt der erwartete Verlust (Expected Loss),
 * siehe {@code io.github.muaiso.kreditrisiko.engine.metrics.ExpectedLoss}.</p>
 *
 * @param ead Exposure at Default (>= 0)
 * @param lgd Loss Given Default in [0, 1]
 * @param pd  Probability of Default in [0, 1]
 */
public record Exposure(double ead, double lgd, double pd) {

    public Exposure {
        if (ead < 0) {
            throw new IllegalArgumentException("ead muss >= 0 sein");
        }
        if (lgd < 0.0 || lgd > 1.0) {
            throw new IllegalArgumentException("lgd muss in [0, 1] liegen");
        }
        if (pd < 0.0 || pd > 1.0) {
            throw new IllegalArgumentException("pd muss in [0, 1] liegen");
        }
    }

    /**
     * @return erwarteter Verlust = ead * lgd * pd
     */
    public double expectedLoss() {
        return ead * lgd * pd;
    }
}
