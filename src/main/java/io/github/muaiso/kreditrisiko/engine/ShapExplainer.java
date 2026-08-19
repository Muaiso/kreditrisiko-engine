package io.github.muaiso.kreditrisiko.engine;

import io.github.muaiso.kreditrisiko.domain.CreditFeatures;
import io.github.muaiso.kreditrisiko.domain.LoanApplication;
import io.github.muaiso.kreditrisiko.engine.models.CreditModel;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Lokale Erklaerbarkeit von Modellvorhersagen ueber SHAP-aehnliche
 * Merkmalsbeitraege (Feature Attribution).
 *
 * <p>Zerlegt die Vorhersage eines Modells additiv in den Beitrag jedes
 * einzelnen Merkmals. Genutzt wird eine pfadbasierte Ablations-Variante:
 * beginnend beim Hintergrund-Referenzantrag wird die Vorhersage schrittweise
 * mit den echten Merkmalswerten ueberschrieben; der Zuwachs je Schritt ist
 * der Beitrag dieses Merkmals. Die Summe der Beitraege plus Basiswert ergibt
 * exakt die Gesamt-PD.</p>
 *
 * <pre>
 *   PD(x) = base + sum_j contribution_j
 * </pre>
 *
 * <p>Damit laesst sich einem Kunden oder Auditor begruenden, <em>warum</em>
 * ein Antrag abgelehnt wurde – eine Kernanforderung erklaerbarer KI
 * (EU AI Act Art. 13, diskriminierungsfreie Entscheidungen).</p>
 */
public final class ShapExplainer {

    private final CreditModel model;
    private final FeatureAggregator aggregator;
    private final double baseValue;
    private final double avgAge;
    private final double avgIncome;
    private final double avgDebt;
    private final double avgEmp;
    private final String avgPurpose;

    /**
     * Bindet einen trainierten Scorer an eine Referenzpopulation.
     *
     * @param model        das trainierte Modell
     * @param backgroundSet Repraesentative Antraege (Durchschnittswerte)
     */
    public ShapExplainer(CreditModel model, List<LoanApplication> backgroundSet) {
        if (model == null || !model.isTrained()) {
            throw new IllegalStateException("Modell muss trainiert sein");
        }
        if (backgroundSet == null || backgroundSet.isEmpty()) {
            throw new IllegalArgumentException("backgroundSet darf nicht leer sein");
        }
        this.model = model;
        this.aggregator = new FeatureAggregator(backgroundSet);

        double sa = 0, si = 0, sd = 0, se = 0;
        String purpose = backgroundSet.get(0).features().purpose();
        for (LoanApplication a : backgroundSet) {
            CreditFeatures f = a.features();
            sa += f.age();
            si += f.income();
            sd += f.debt();
            se += f.employmentYears();
        }
        int n = backgroundSet.size();
        this.avgAge = sa / n;
        this.avgIncome = si / n;
        this.avgDebt = sd / n;
        this.avgEmp = se / n;
        this.avgPurpose = purpose;
        this.baseValue = model.predictProbability(reference(
                new double[]{avgAge, avgIncome, avgDebt, avgEmp}, avgPurpose));
    }

    /**
     * Zerlegt die Vorhersage in exakt additive Merkmalsbeitraege.
     *
     * @param application zu erklaerender Antrag
     * @return Merkmalsname -> Beitrag zur PD (Summe + basis = PD)
     */
    public Map<String, Double> explain(LoanApplication application) {
        CreditFeatures f = application.features();
        double[] contrib = new double[5];
        // Pfad: starte beim Hintergrund-Antrag, ersetze Merkmal fuer Merkmal
        double[] partial = {avgAge, avgIncome, avgDebt, avgEmp};
        double prior = model.predictProbability(reference(partial, avgPurpose));
        String[] order = {"age", "income", "debt", "employmentYears"};
        for (int j = 0; j < 4; j++) {
            partial[j] = switch (j) {
                case 0 -> f.age();
                case 1 -> f.income();
                case 2 -> f.debt();
                default -> f.employmentYears();
            };
            double next = model.predictProbability(reference(partial, avgPurpose));
            contrib[j] = next - prior;
            prior = next;
        }
        // DTI ist abgeleitet aus debt/income -> kein eigener Pfadschritt,
        // daher bleibt der Beitrag im debt/income-Teil enthalten.

        Map<String, Double> contributions = new LinkedHashMap<>();
        contributions.put("age", contrib[0]);
        contributions.put("income", contrib[1]);
        contributions.put("debt", contrib[2]);
        contributions.put("employmentYears", contrib[3]);
        // letzter Pfadschritt: echter Zweck ersetzt den Hintergrund-Zweck
        contributions.put("purpose", model.predictProbability(application) - prior);
        return contributions;
    }

    /**
     * @return Basiswert (durchschnittliche PD der Hintergrundpopulation)
     */
    public double baseValue() {
        return baseValue;
    }

    private LoanApplication reference(double[] vals, String purpose) {
        CreditFeatures f = new CreditFeatures((int) Math.round(vals[0]), vals[1], vals[2],
                (int) Math.round(vals[3]), purpose);
        return new LoanApplication(f, false);
    }
}

