package com.smart_skin.smart_skin_app_backend.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class SkinModelService {

    @Value("${skin-model.api-url}")
    private String skinModelUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    public SkinModelResult predict(String imageUrl) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, String> body = new HashMap<>();
            body.put("image_url", imageUrl);

            HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                    skinModelUrl + "/predict-url",
                    HttpMethod.POST,
                    request,
                    Map.class
            );

            Map<?, ?> data = response.getBody();
            if (data == null || data.containsKey("error")) {
                log.warn("SkinModel returned error: {}", data);
                return SkinModelResult.fallback();
            }

            // Parse all_probabilities
            Map<String, Double> probs = new HashMap<>();
            Object probsObj = data.get("all_probabilities");
            if (probsObj instanceof Map) {
                ((Map<?, ?>) probsObj).forEach((k, v) ->
                        probs.put(k.toString(), ((Number) v).doubleValue())
                );
            }

            return new SkinModelResult(
                    data.get("prediction").toString(),
                    ((Number) data.get("confidence")).doubleValue(),
                    probs,
                    true
            );

        } catch (Exception e) {
            log.error("SkinModel API unreachable: {}", e.getMessage());
            return SkinModelResult.fallback();
        }
    }

    // ── Inner result class ──────────────────────────────────────────
    public static class SkinModelResult {
        public final String  prediction;    // "acne", "dark spots", etc.
        public final double  confidence;    // 87.5
        public final Map<String, Double> allProbabilities;
        public final boolean modelAvailable;

        public SkinModelResult(String prediction, double confidence,
                               Map<String, Double> allProbabilities, boolean modelAvailable) {
            this.prediction       = prediction;
            this.confidence       = confidence;
            this.allProbabilities = allProbabilities;
            this.modelAvailable   = modelAvailable;
        }

        // Retourné si le modèle Python est injoignable
        public static SkinModelResult fallback() {
            return new SkinModelResult("unknown", 0.0, new HashMap<>(), false);
        }

        // Conversion proba → score /100 pour le problème détecté
        public int toScore(String condition) {
            Double prob = allProbabilities.get(condition);
            return prob != null ? (int) Math.round(prob) : 0;
        }
    }
}