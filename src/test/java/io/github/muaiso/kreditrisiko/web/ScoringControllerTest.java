package io.github.muaiso.kreditrisiko.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Integrationstest des Scoring-Controllers über MockMvc.
 */
@WebMvcTest(ScoringController.class)
class ScoringControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void scoreReturnsProbabilityAndRating() throws Exception {
        String body = """
                {
                  "age": 30,
                  "income": 80000,
                  "debt": 1000,
                  "employmentYears": 5,
                  "purpose": "CAR"
                }
                """;
        mockMvc.perform(post("/api/score").contentType("application/json").content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.probability").exists())
                .andExpect(jsonPath("$.rating").exists())
                .andExpect(jsonPath("$.declined").exists());
    }

    @Test
    void trainReturnsEvaluationMetrics() throws Exception {
        String body = """
                {
                  "algorithm": "LOGISTIC_REGRESSION",
                  "seed": 7,
                  "applications": [
                    {"age": 30, "income": 80000, "debt": 1000, "employmentYears": 5, "purpose": "CAR", "defaulted": false},
                    {"age": 55, "income": 20000, "debt": 40000, "employmentYears": 1, "purpose": "OTHER", "defaulted": true},
                    {"age": 28, "income": 62000, "debt": 8000, "employmentYears": 7, "purpose": "HOUSE", "defaulted": false},
                    {"age": 52, "income": 28000, "debt": 30000, "employmentYears": 1, "purpose": "OTHER", "defaulted": true}
                  ]
                }
                """;
        mockMvc.perform(post("/api/train").contentType("application/json").content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.auc").exists())
                .andExpect(jsonPath("$.ks").exists())
                .andExpect(jsonPath("$.confusion").exists());
    }
}
