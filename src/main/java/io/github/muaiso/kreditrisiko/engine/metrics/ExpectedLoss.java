package io.github.muaiso.kreditrisiko.engine.metrics;

import io.github.muaiso.kreditrisiko.domain.Exposure;

import java.util.List;

/**
 * Erwarteter Verlust (Expected Loss, EL) eines Kreditportfolios.
 *
 * <p>Grundgleichung des Credit Risk Managements:</p>
 * <pre>
 *   EL = EAD * LGD * PD
 * </pre>
 * <p>Das Modul aggregiert die Einzelengagements zu Gesamt-EL,
 * durchschnittlicher PD und gewichtetem LGD und liefert damit die
 * Kennzahlen, die ein Kreditrisiko-Manager taeglich berichtet.</p>
 */
public final class ExpectedLoss {

    private final double totalExpectedLoss;
    private final double weightedPd;
    private final double weightedLgd;
    private final double totalEad;

    /**
     * Aggregiert eine Liste von Engagements.
     *
     * @param exposures die Einzelengagements des Portfolios
     */
    public ExpectedLoss(List<Exposure> exposures) {
        if (exposures == null || exposures.isEmpty()) {
            throw new IllegalArgumentException("exposures darf nicht leer sein");
        }
        double el = 0.0;
        double ead = 0.0;
        double pdWeighted = 0.0;
        double lgdWeighted = 0.0;
        for (Exposure e : exposures) {
            el += e.expectedLoss();
            ead += e.ead();
            pdWeighted += e.pd() * e.ead();
            lgdWeighted += e.lgd() * e.ead();
        }
        this.totalExpectedLoss = el;
        this.totalEad = ead;
        this.weightedPd = ead > 0 ? pdWeighted / ead : 0.0;
        this.weightedLgd = ead > 0 ? lgdWeighted / ead : 0.0;
    }

    /** @return Summe der erwarteten Verluste ueber alle Engagements */
    public double totalExpectedLoss() {
        return totalExpectedLoss;
    }

    /** @return EAD-gewichtete mittlere Ausfallwahrscheinlichkeit */
    public double weightedPd() {
        return weightedPd;
    }

    /** @return EAD-gewichtete mittlere Verlustquote */
    public double weightedLgd() {
        return weightedLgd;
    }

    /** @return Gesamtexposure (Summe EAD) */
    public double totalEad() {
        return totalEad;
    }

    /**
     * @return EL-Quote relativ zum Gesamtexposure (EL/EAD)
     */
    public double expectedLossRate() {
        return totalEad > 0 ? totalExpectedLoss / totalEad : 0.0;
    }
}
