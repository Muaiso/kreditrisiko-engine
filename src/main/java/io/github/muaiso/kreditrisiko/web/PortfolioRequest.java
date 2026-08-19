package io.github.muaiso.kreditrisiko.web;

import java.util.List;

/**
 * Anfrage zur Portfolio-Risiko-Auswertung.
 *
 * @param exposures die Einzelengagements des Portfolios
 */
public record PortfolioRequest(List<PortfolioExposure> exposures) {
}
