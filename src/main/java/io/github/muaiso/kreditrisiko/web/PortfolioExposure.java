package io.github.muaiso.kreditrisiko.web;

/**
 * Einzelengagement eines Kreditportfolios (IRB-Groessen).
 *
 * @param ead Exposure at Default (Forderung zum Ausfall)
 * @param lgd Loss Given Default in [0, 1]
 * @param pd  Probability of Default in [0, 1]
 * @param segment optionales Segment (Branche/Region) fuer Konzentration
 */
public record PortfolioExposure(
        double ead,
        double lgd,
        double pd,
        String segment) {

    /** @return umgewandelte Domain-Entitaet */
    public io.github.muaiso.kreditrisiko.domain.Exposure toDomain() {
        return new io.github.muaiso.kreditrisiko.domain.Exposure(ead, lgd, pd);
    }
}
