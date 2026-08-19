package io.github.muaiso.kreditrisiko.engine.metrics;

import io.github.muaiso.kreditrisiko.domain.Exposure;

import java.util.List;
import java.util.Map;

/**
 * Portfolioebene des Kreditrisikos: aggregierte Expected Loss und
 * Konzentrationsrisiko.
 *
 * <p>Ergaenzt {@link ExpectedLoss} um das Konzentrationsrisiko nach der
 * Herfindahl-Hirschman-Index (HHI) Formel. Ein hoher HHI bedeutet, dass das
 * Gesamtrisiko auf wenige grosse Engagements konzentriert ist – ein
 * klassisches Warnsignal im Kreditportfolio-Management.</p>
 *
 * <pre>
 *   HHI = Summe (EAD_i / EAD_total)^2   in [1/n, 1]
 * </pre>
 */
public final class PortfolioRisk {

    private final ExpectedLoss expectedLoss;
    private final double hhi;
    private final int segmentCount;

    /**
     * Bewertet ein Portfolio aus Engagement-Liste.
     *
     * @param exposures die Einzelengagements
     */
    public PortfolioRisk(List<Exposure> exposures) {
        this(exposures, Map.of());
    }

    /**
     * Bewertet ein Portfolio, optional mit Segmentzuordnung je Engagement.
     *
     * <p>Die Segmente (z. B. Branche, Region) dienen der Konzentrations-
     * analyse: {@code segmentOf} ordnet jedem Engagement ein Segment zu.
     * Fehlt die Zuordnung, wird das Gesamtportfolio als ein Segment
     * betrachtet.</p>
     *
     * @param exposures die Einzelengagements
     * @param segmentOf Zuordnung Engagement-Index -> Segment-Schluessel
     */
    public PortfolioRisk(List<Exposure> exposures, Map<Integer, String> segmentOf) {
        if (exposures == null || exposures.isEmpty()) {
            throw new IllegalArgumentException("exposures darf nicht leer sein");
        }
        this.expectedLoss = new ExpectedLoss(exposures);
        this.hhi = computeHhi(exposures);
        this.segmentCount = computeSegmentCount(exposures, segmentOf);
    }

    /** @return gekapselte Expected-Loss-Kennzahlen */
    public ExpectedLoss expectedLoss() {
        return expectedLoss;
    }

    /** @return Herfindahl-Hirschman-Index des EAD in [1/n, 1] */
    public double hhi() {
        return hhi;
    }

    /** @return Anzahl der (per Segment) unterschiedenen Risikobloecke */
    public int segmentCount() {
        return segmentCount;
    }

    /**
     * @return grobe Konzentrationsstufe als Text
     */
    public String concentrationLevel() {
        if (hhi < 0.1) {
            return "gering";
        }
        if (hhi < 0.25) {
            return "maessig";
        }
        return "hoch";
    }

    private static double computeHhi(List<Exposure> exposures) {
        double total = 0.0;
        for (Exposure e : exposures) {
            total += e.ead();
        }
        if (total <= 0) {
            return 1.0;
        }
        double sumSq = 0.0;
        for (Exposure e : exposures) {
            double share = e.ead() / total;
            sumSq += share * share;
        }
        return sumSq;
    }

    private static int computeSegmentCount(List<Exposure> exposures, Map<Integer, String> segmentOf) {
        java.util.Set<String> segments = new java.util.HashSet<>();
        for (int i = 0; i < exposures.size(); i++) {
            segments.add(segmentOf.getOrDefault(i, "ALL"));
        }
        return segments.size();
    }
}
