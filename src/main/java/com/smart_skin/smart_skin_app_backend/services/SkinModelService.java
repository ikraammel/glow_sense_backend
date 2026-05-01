package com.smart_skin.smart_skin_app_backend.services;


import java.util.Map;

public interface SkinModelService {

    SkinModelResult predict(String imageUrl);

    SkinModelResult predict(byte[] imageBytes, String filename);

    class SkinModelResult {
        public final String prediction;
        public final double confidence;
        public final Map<String, Double> allProbabilities;
        public final boolean modelAvailable;

        public SkinModelResult(String prediction, double confidence,
                               Map<String, Double> allProbabilities, boolean modelAvailable) {
            this.prediction = prediction;
            this.confidence = confidence;
            this.allProbabilities = allProbabilities;
            this.modelAvailable = modelAvailable;
        }

        public static SkinModelResult unavailable() {
            return new SkinModelResult("unknown", 0.0, Map.of(), false);
        }
    }
}