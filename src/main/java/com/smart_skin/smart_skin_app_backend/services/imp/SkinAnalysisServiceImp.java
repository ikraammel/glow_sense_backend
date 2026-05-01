package com.smart_skin.smart_skin_app_backend.services.imp;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smart_skin.smart_skin_app_backend.dto.RecommandationDto;
import com.smart_skin.smart_skin_app_backend.dto.SkinAnalysisResponseDto;
import com.smart_skin.smart_skin_app_backend.dto.SkinProblemDto;
import com.smart_skin.smart_skin_app_backend.enums.Severity;
import com.smart_skin.smart_skin_app_backend.enums.SkinType;
import com.smart_skin.smart_skin_app_backend.exception.BadRequestException;
import com.smart_skin.smart_skin_app_backend.exception.ResourceNotFoundException;
import com.smart_skin.smart_skin_app_backend.models.*;
import com.smart_skin.smart_skin_app_backend.repos.SkinAnalysisRepository;
import com.smart_skin.smart_skin_app_backend.repos.UserRepository;
import com.smart_skin.smart_skin_app_backend.services.CloudinaryService;
import com.smart_skin.smart_skin_app_backend.services.NotificationService;
import com.smart_skin.smart_skin_app_backend.services.SkinAnalysisService;
import com.smart_skin.smart_skin_app_backend.services.SkinModelService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SkinAnalysisServiceImp implements SkinAnalysisService {

    private final SkinAnalysisRepository skinAnalysisRepository;
    private final UserRepository         userRepository;
    private final CloudinaryService      cloudinaryService;
    private final NotificationService    notificationService;
    private final ObjectMapper           objectMapper;
    private final SkinModelService       skinModelService;

    @Value("${groq.api-key}")
    private String groqApiKey;

    @Value("${groq.model:llama-3.3-70b-versatile}")
    private String groqModel;

    // ════════════════════════════════════════════════════════════════
    //  PUBLIC METHODS
    // ════════════════════════════════════════════════════════════════

    @Override
    @Transactional
    public SkinAnalysisResponseDto analyzeImage(Long userId, MultipartFile image) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));

        // ── Validation image ─────────────────────────────────────────
        if (image.isEmpty()) throw new BadRequestException("Image vide");
        String contentType = image.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new BadRequestException("Le fichier doit être une image");
        }

        // ── 1. Upload Cloudinary ──────────────────────────────────────
        Map<String, String> uploadResult = cloudinaryService.uploadImageWithDetails(
                image, "skin_analyses/" + userId);
        String imageUrl = uploadResult.get("url");

        SkinImage skinImage = SkinImage.builder()
                .cloudinaryUrl(imageUrl)
                .cloudinaryPublicId(uploadResult.get("publicId"))
                .thumbnailUrl(uploadResult.get("thumbnailUrl"))
                .format(uploadResult.get("format"))
                .fileSize(image.getSize())
                .build();

        // ── 2. Modèle Python ─────────────────────────────────────────
        SkinModelService.SkinModelResult modelResult = skinModelService.predict(imageUrl);
        log.info("Python model → prediction: {} | confidence: {:.1f}% | available: {}",
                modelResult.prediction, modelResult.confidence, modelResult.modelAvailable);

        // ── 3. Groq (description + recommandations) ──────────────────
        String aiResponse = callGroqWithModelContext(imageUrl, user, modelResult);

        // ── 4. Parser la réponse Groq ─────────────────────────────────
        SkinAnalysis analysis = parseAiResponse(aiResponse, user, skinImage);

        // ── 5. Écraser les scores avec ceux du modèle Python ─────────
        if (modelResult.modelAvailable) {
            applyModelScores(analysis, modelResult);
        }

        // ── 6. Sauvegarder + notifier ─────────────────────────────────
        analysis = skinAnalysisRepository.save(analysis);
        notificationService.createAnalysisCompleteNotification(user, analysis);

        log.info("Analysis complete for user {} — overall score: {}", userId, analysis.getOverallScore());

        // ── 7. Retourner DTO avec données du modèle Python ───────────
        return toResponseDto(analysis, modelResult);
    }

    @Override
    public SkinAnalysisResponseDto getAnalysisById(Long userId, Long analysisId) {
        SkinAnalysis analysis = skinAnalysisRepository.findById(analysisId)
                .orElseThrow(() -> new ResourceNotFoundException("Analyse non trouvée"));
        if (!analysis.getUser().getId().equals(userId)) {
            throw new BadRequestException("Accès non autorisé");
        }
        // Pas de modelResult disponible pour l'historique → surcharge sans model
        return toResponseDto(analysis);
    }

    @Override
    public Page<SkinAnalysisResponseDto> getAnalysisHistory(Long userId, Pageable pageable) {
        return skinAnalysisRepository
                .findByUserIdOrderByAnalyzedAtDesc(userId, pageable)
                .map(this::toResponseDto);
    }

    @Override
    public List<SkinAnalysisResponseDto> getRecentAnalyses(Long userId, int limit) {
        return skinAnalysisRepository
                .findRecentByUserId(userId, limit)
                .stream()
                .map(this::toResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteAnalysis(Long userId, Long analysisId) {
        SkinAnalysis analysis = skinAnalysisRepository.findById(analysisId)
                .orElseThrow(() -> new ResourceNotFoundException("Analyse non trouvée"));
        if (!analysis.getUser().getId().equals(userId)) {
            throw new BadRequestException("Accès non autorisé");
        }
        if (analysis.getSkinImage() != null) {
            cloudinaryService.deleteImage(analysis.getSkinImage().getCloudinaryUrl());
        }
        skinAnalysisRepository.delete(analysis);
    }

    // ════════════════════════════════════════════════════════════════
    //  PRIVATE — SCORES DU MODÈLE PYTHON
    // ════════════════════════════════════════════════════════════════

    /**
     * Écrase les scores issus de Groq par les probabilités réelles du modèle Python.
     * Logique : score = 100 − probabilité (ex: acne à 80% → acneScore = 20)
     * Plus le problème est présent, plus le score est bas.
     */
    private void applyModelScores(SkinAnalysis analysis,
                                  SkinModelService.SkinModelResult modelResult) {

        Map<String, Double> probs = modelResult.allProbabilities;

        if (probs.containsKey("acne")) {
            analysis.setAcneScore(100 - probs.get("acne").intValue());
        }
        if (probs.containsKey("dark spots")) {
            analysis.setPigmentationScore(100 - probs.get("dark spots").intValue());
        }
        if (probs.containsKey("wrinkles")) {
            analysis.setWrinkleScore(100 - probs.get("wrinkles").intValue());
        }
        if (probs.containsKey("pores")) {
            analysis.setPoreScore(100 - probs.get("pores").intValue());
        }
        // hydrationScore reste celui de Groq (le modèle Python ne le prédit pas)

        // Recalculer overallScore = moyenne des 5 scores
        int overall = (
                analysis.getAcneScore()         +
                        analysis.getPigmentationScore() +
                        analysis.getWrinkleScore()      +
                        analysis.getPoreScore()         +
                        analysis.getHydrationScore()
        ) / 5;
        analysis.setOverallScore(overall);

        // Ajouter le problème principal du modèle en tête de liste
        if (modelResult.prediction != null && !modelResult.prediction.equals("unknown")) {

            Severity severity = modelResult.confidence > 70 ? Severity.SEVERE
                    : modelResult.confidence > 40 ? Severity.MODEREE
                    : Severity.LEGERE;

            SkinProblem modelProblem = SkinProblem.builder()
                    .skinAnalysis(analysis)
                    .problemType(modelResult.prediction)
                    .severity(severity)
                    .zone("global")
                    .description(String.format(
                            "Détecté par le modèle IA avec %.1f%% de confiance.",
                            modelResult.confidence))
                    .confidence(modelResult.confidence / 100.0)
                    .build();

            List<SkinProblem> problems = new ArrayList<>();
            problems.add(modelProblem);                          // modèle Python en 1er
            if (analysis.getDetectedProblems() != null) {
                problems.addAll(analysis.getDetectedProblems()); // puis ceux de Groq
            }
            analysis.setDetectedProblems(problems);
        }
    }

    // ════════════════════════════════════════════════════════════════
    //  PRIVATE — APPEL GROQ
    // ════════════════════════════════════════════════════════════════

    private String callGroqWithModelContext(String imageUrl,
                                            User user,
                                            SkinModelService.SkinModelResult modelResult) {

        String skinContext  = buildSkinContext(user);
        String modelContext = buildModelContext(modelResult);

        String prompt = """
                Tu es un dermatologue IA expert. Analyse cette image de peau.
                
                Contexte modèle IA : %s
                Contexte utilisateur : %s
                
                Réponds UNIQUEMENT en JSON valide avec cette structure exacte:
                {
                  "detectedSkinType": "NORMAL|SEC|GRAS|MIXTE|SENSIBLE|ACNEIQUE|MATURE",
                  "overallScore": <0-100>,
                  "hydrationScore": <0-100>,
                  "acneScore": <0-100>,
                  "pigmentationScore": <0-100>,
                  "wrinkleScore": <0-100>,
                  "poreScore": <0-100>,
                  "analysisDescription": "<description générale en français, cohérente avec le modèle IA>",
                  "problems": [
                    {
                      "problemType": "<acne|tache|ride|pore|deshydratation|rougeur|cicatrice|comedons>",
                      "severity": "LEGERE|MODEREE|SEVERE",
                      "zone": "<front|joues|nez|menton|tempes|contour_yeux|global>",
                      "description": "<description>",
                      "confidence": <0.0-1.0>
                    }
                  ],
                  "recommandations": [
                    {
                      "category": "<nettoyant|hydratant|traitement|protection_solaire|gommage|serum|alimentation|style_de_vie>",
                      "title": "<titre court>",
                      "description": "<explication>",
                      "activeIngredient": "<ingredient cle>",
                      "priority": <1|2|3>,
                      "applicationFrequency": "<matin|soir|matin_et_soir|hebdomadaire>",
                      "tips": "<conseil pratique>"
                    }
                  ]
                }
                
                Important: aligne ta description avec la détection du modèle IA.
                Minimum 3 problèmes et 5 recommandations.
                """.formatted(modelContext, skinContext);

        try {
            RestTemplate restTemplate = new RestTemplate();

            Map<String, Object> textContent  = Map.of("type", "text", "text", prompt);
            Map<String, Object> imageContent = Map.of(
                    "type", "image_url",
                    "image_url", Map.of("url", imageUrl, "detail", "high"));
            Map<String, Object> message = Map.of(
                    "role", "user",
                    "content", List.of(textContent, imageContent));
            Map<String, Object> body = Map.of(
                    "model", "llama-3.3-70b-versatile",
                    "messages", List.of(message),
                    "temperature", 0.2,
                    "max_tokens", 2000,
                    "response_format", Map.of("type", "json_object"));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(groqApiKey);

            ResponseEntity<Map> response = restTemplate.exchange(
                    "https://api.groq.com/openai/v1/chat/completions",
                    HttpMethod.POST,
                    new HttpEntity<>(body, headers),
                    Map.class);

            Map<?, ?> responseBody = response.getBody();
            if (responseBody != null && responseBody.get("choices") != null) {
                List<?> choices = (List<?>) responseBody.get("choices");
                if (!choices.isEmpty()) {
                    Map<?, ?> msg = (Map<?, ?>) ((Map<?, ?>) choices.get(0)).get("message");
                    if (msg != null && msg.get("content") != null) {
                        return msg.get("content").toString();
                    }
                }
            }

        } catch (Exception e) {
            log.error("Groq API error: {}", e.getMessage(), e);
        }

        return buildFallbackResponse();
    }

    // ════════════════════════════════════════════════════════════════
    //  PRIVATE — HELPERS
    // ════════════════════════════════════════════════════════════════

    private String buildSkinContext(User user) {
        return String.format(
                "Type de peau déclaré: %s | Problèmes: %s | Ethnicity: %s | Soleil: %s",
                user.getSkinType()    != null ? user.getSkinType().name() : "inconnu",
                user.getSkinConcerns()!= null ? user.getSkinConcerns()   : "non renseigné",
                user.getEthnicity()   != null ? user.getEthnicity()      : "non renseigné",
                user.getSunExposure() != null ? user.getSunExposure()     : "non renseigné");
    }

    private String buildModelContext(SkinModelService.SkinModelResult modelResult) {
        if (!modelResult.modelAvailable) return "Modèle Python indisponible — analyse visuelle directe.";

        return String.format(
                "Détection principale: %s (confiance: %.1f%%). " +
                        "Probabilités — acne: %.1f%%, dark spots: %.1f%%, " +
                        "wrinkles: %.1f%%, pores: %.1f%%, blackheads: %.1f%%.",
                modelResult.prediction,
                modelResult.confidence,
                modelResult.allProbabilities.getOrDefault("acne",        0.0),
                modelResult.allProbabilities.getOrDefault("dark spots",  0.0),
                modelResult.allProbabilities.getOrDefault("wrinkles",    0.0),
                modelResult.allProbabilities.getOrDefault("pores",       0.0),
                modelResult.allProbabilities.getOrDefault("blackheades", 0.0));
    }

    private SkinAnalysis parseAiResponse(String aiJson, User user, SkinImage skinImage) {
        try {
            JsonNode root = objectMapper.readTree(aiJson);

            SkinAnalysis analysis = SkinAnalysis.builder()
                    .user(user)
                    .skinImage(skinImage)
                    .detectedSkinType(parseSkinType(root.path("detectedSkinType").asText()))
                    .overallScore(root.path("overallScore").asInt(50))
                    .hydrationScore(root.path("hydrationScore").asInt(50))
                    .acneScore(root.path("acneScore").asInt(50))
                    .pigmentationScore(root.path("pigmentationScore").asInt(50))
                    .wrinkleScore(root.path("wrinkleScore").asInt(50))
                    .poreScore(root.path("poreScore").asInt(50))
                    .analysisDescription(root.path("analysisDescription").asText())
                    .aiRawResponse(aiJson)
                    .build();

            // ── Parse problems ────────────────────────────────────────
            List<SkinProblem> problems = new ArrayList<>();
            JsonNode problemsNode = root.path("problems");
            if (problemsNode.isArray()) {
                for (JsonNode p : problemsNode) {
                    problems.add(SkinProblem.builder()
                            .skinAnalysis(analysis)
                            .problemType(p.path("problemType").asText())
                            .severity(parseSeverity(p.path("severity").asText()))
                            .zone(p.path("zone").asText())
                            .description(p.path("description").asText())
                            .confidence(p.path("confidence").asDouble(0.8))
                            .build());
                }
            }
            analysis.setDetectedProblems(problems);

            // ── Parse recommandations ─────────────────────────────────
            List<Recommandation> recs = new ArrayList<>();
            JsonNode recsNode = root.path("recommandations");
            if (recsNode.isArray()) {
                for (JsonNode r : recsNode) {
                    recs.add(Recommandation.builder()
                            .skinAnalysis(analysis)
                            .category(r.path("category").asText())
                            .title(r.path("title").asText())
                            .description(r.path("description").asText())
                            .activeIngredient(r.path("activeIngredient").asText())
                            .priority(r.path("priority").asInt(2))
                            .applicationFrequency(r.path("applicationFrequency").asText())
                            .tips(r.path("tips").asText())
                            .build());
                }
            }
            analysis.setRecommandations(recs);

            return analysis;

        } catch (Exception e) {
            log.error("Failed to parse AI response: {}", e.getMessage());
            throw new BadRequestException("Erreur lors de l'analyse de l'image. Veuillez réessayer.");
        }
    }

    private SkinType parseSkinType(String value) {
        try   { return SkinType.valueOf(value.toUpperCase()); }
        catch (Exception e) { return SkinType.INCONNU; }
    }

    private Severity parseSeverity(String value) {
        try   { return Severity.valueOf(value.toUpperCase()); }
        catch (Exception e) { return Severity.LEGERE; }
    }

    private String buildFallbackResponse() {
        return """
                {
                  "detectedSkinType": "MIXTE",
                  "overallScore": 65,
                  "hydrationScore": 60,
                  "acneScore": 45,
                  "pigmentationScore": 70,
                  "wrinkleScore": 80,
                  "poreScore": 55,
                  "analysisDescription": "Votre peau présente des caractéristiques mixtes avec une zone T légèrement grasse et des joues normales à sèches.",
                  "problems": [
                    {"problemType": "acne",          "severity": "LEGERE",  "zone": "front",  "description": "Quelques petits boutons sur le front",      "confidence": 0.85},
                    {"problemType": "pore",          "severity": "MODEREE", "zone": "nez",    "description": "Pores dilatés sur la zone du nez",           "confidence": 0.80},
                    {"problemType": "deshydratation","severity": "LEGERE",  "zone": "joues",  "description": "Légère déshydratation sur les joues",         "confidence": 0.75}
                  ],
                  "recommandations": [
                    {"category": "nettoyant",        "title": "Nettoyant doux équilibrant",    "description": "Utilisez un nettoyant gel doux matin et soir",             "activeIngredient": "acide salicylique",  "priority": 1, "applicationFrequency": "matin_et_soir", "tips": "Massez en douceur pendant 60 secondes"},
                    {"category": "hydratant",        "title": "Gel hydratant non-comédogène", "description": "Un gel hydratant léger adapté aux peaux mixtes",            "activeIngredient": "acide hyaluronique", "priority": 1, "applicationFrequency": "matin_et_soir", "tips": "Appliquer sur peau légèrement humide"},
                    {"category": "traitement",       "title": "Sérum anti-imperfections",     "description": "Ciblez les zones à problèmes le soir",                      "activeIngredient": "niacinamide",        "priority": 2, "applicationFrequency": "soir",          "tips": "Quelques gouttes suffisent"},
                    {"category": "protection_solaire","title": "SPF 30+ obligatoire",         "description": "La protection solaire est essentielle chaque matin",         "activeIngredient": "filtres UV",         "priority": 1, "applicationFrequency": "matin",         "tips": "Même par temps nuageux"},
                    {"category": "style_de_vie",     "title": "Hydratation interne",          "description": "Buvez au moins 1,5L d'eau par jour",                        "activeIngredient": "eau",                "priority": 3, "applicationFrequency": "matin",         "tips": "Commencez la journée avec un grand verre d'eau"}
                  ]
                }
                """;
    }

    // ════════════════════════════════════════════════════════════════
    //  PRIVATE — toResponseDto (deux surcharges)
    // ════════════════════════════════════════════════════════════════

    /**
     * Surcharge de base — utilisée pour l'historique, getById, recent.
     * Les champs modelPrediction / modelConfidence / etc. restent null.
     */
    private SkinAnalysisResponseDto toResponseDto(SkinAnalysis a) {

        SkinAnalysisResponseDto dto = new SkinAnalysisResponseDto();
        dto.setId(a.getId());
        dto.setImageUrl(a.getSkinImage() != null ? a.getSkinImage().getCloudinaryUrl() : null);
        dto.setDetectedSkinType(a.getDetectedSkinType());
        dto.setOverallScore(a.getOverallScore());
        dto.setHydrationScore(a.getHydrationScore());
        dto.setAcneScore(a.getAcneScore());
        dto.setPigmentationScore(a.getPigmentationScore());
        dto.setWrainkleScore(a.getWrinkleScore());   // alias setter existant
        dto.setPoreScore(a.getPoreScore());
        dto.setAnalysisDescription(a.getAnalysisDescription());
        dto.setAnalyzedAt(a.getAnalyzedAt());

        // ── Problems ──────────────────────────────────────────────────
        if (a.getDetectedProblems() != null) {
            dto.setDetectedProblems(
                    a.getDetectedProblems().stream().map(p -> {
                        SkinProblemDto pdto = new SkinProblemDto();
                        pdto.setId(p.getId());
                        pdto.setProblemType(p.getProblemType());
                        pdto.setSeverity(p.getSeverity());
                        pdto.setZone(p.getZone());
                        pdto.setDescription(p.getDescription());
                        pdto.setConfidence(p.getConfidence());
                        return pdto;
                    }).collect(Collectors.toList())
            );
        }

        // ── Recommandations ───────────────────────────────────────────
        if (a.getRecommandations() != null) {
            dto.setRecommandations(
                    a.getRecommandations().stream().map(r -> {
                        RecommandationDto rdto = new RecommandationDto();
                        rdto.setId(r.getId());
                        rdto.setCategory(r.getCategory());
                        rdto.setTitle(r.getTitle());
                        rdto.setDescription(r.getDescription());
                        rdto.setActiveIngredient(r.getActiveIngredient());
                        rdto.setPriority(r.getPriority());
                        rdto.setApplicationFrequency(r.getApplicationFrequency());
                        rdto.setTips(r.getTips());
                        return rdto;
                    }).collect(Collectors.toList())
            );
        }

        return dto;
    }

    /**
     * Surcharge enrichie — utilisée uniquement après analyzeImage().
     * Ajoute les 4 champs du modèle Python au-dessus du DTO de base.
     */
    private SkinAnalysisResponseDto toResponseDto(SkinAnalysis a,
                                                  SkinModelService.SkinModelResult modelResult) {
        SkinAnalysisResponseDto dto = toResponseDto(a); // réutilise la surcharge de base

        if (modelResult != null) {
            dto.setModelPrediction(modelResult.prediction);
            dto.setModelConfidence(modelResult.confidence);
            dto.setModelProbabilities(modelResult.allProbabilities);
            dto.setModelWasAvailable(modelResult.modelAvailable);
        }

        return dto;
    }
}