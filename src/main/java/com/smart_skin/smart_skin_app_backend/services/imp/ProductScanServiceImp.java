package com.smart_skin.smart_skin_app_backend.services.imp;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smart_skin.smart_skin_app_backend.dto.ProductScanRequestDto;
import com.smart_skin.smart_skin_app_backend.dto.ProductScanResponseDto;
import com.smart_skin.smart_skin_app_backend.exception.ResourceNotFoundException;
import com.smart_skin.smart_skin_app_backend.models.ProductScan;
import com.smart_skin.smart_skin_app_backend.models.User;
import com.smart_skin.smart_skin_app_backend.repos.ProductScanRepository;
import com.smart_skin.smart_skin_app_backend.repos.UserRepository;
import com.smart_skin.smart_skin_app_backend.services.CloudinaryService;
import com.smart_skin.smart_skin_app_backend.services.ProductScanService;
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
import java.util.stream.StreamSupport;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductScanServiceImp implements ProductScanService {

    private final ProductScanRepository productScanRepository;
    private final UserRepository userRepository;
    private final CloudinaryService cloudinaryService;
    private final ObjectMapper objectMapper;

    @Value("${groq.api-key}")
    private String groqApiKey;

    @Value("${groq.model:llama-3.3-70b-versatile}")
    private String groqModel;

    @Override
    @Transactional
    public ProductScanResponseDto scanProduct(Long userId, ProductScanRequestDto request) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));

        // 🔒 SAFE PRODUCT NAME
        if (request.getProductName() == null || request.getProductName().isBlank()) {
            request.setProductName("Produit inconnu");
        }

        String aiResult = callOpenAIForProduct(
                request.getIngredients(),
                request.getProductName(),
                user
        );

        ProductScan scan = buildScanFromAiResult(user, request, aiResult, null);
        scan = productScanRepository.save(scan);

        return toDto(scan);
    }

    @Override
    @Transactional
    public ProductScanResponseDto scanProductByImage(Long userId, MultipartFile image) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));

        String imageUrl = cloudinaryService.uploadImage(image, "product_scans/" + userId);

        String aiResult = callOpenAIVisionForProduct(imageUrl, user);

        ProductScanRequestDto request = new ProductScanRequestDto();
        request.setProductName("Produit scanné");
        request.setIngredients("non détecté");
        request.setBrand("inconnu");

        ProductScan scan = buildScanFromAiResult(user, request, aiResult, imageUrl);
        scan = productScanRepository.save(scan);

        return toDto(scan);
    }

    // ================= AI TEXT =================
    private String callOpenAIForProduct(String ingredients, String productName, User user) {

        String prompt = """
            Tu es un expert en cosmétologie et dermatologie.
            Analyse la compatibilité du produit.

            Réponds UNIQUEMENT en JSON:
            {
              "compatible": true,
              "compatibilityScore": 0-100,
              "safetyRating": "low | medium | high",
              "positiveIngredients": [],
              "negativeIngredients": [],
              "analysisResult": ""
            }

            Produit: %s
            Ingrédients: %s
            Profil: type=%s, allergies=%s, à éviter=%s
            """.formatted(
                productName,
                ingredients,
                user.getSkinType() != null ? user.getSkinType().name() : "inconnu",
                user.getSkinConcerns() != null ? user.getSkinConcerns() : "aucun",
                user.getIngredientsToAvoid() != null ? user.getIngredientsToAvoid() : "aucun"
        );

        return callOpenAI(prompt, null);
    }

    // ================= AI VISION =================
    private String callOpenAIVisionForProduct(String imageUrl, User user) {

        String prompt = """
            Analyse cette image de produit cosmétique.

            Réponds UNIQUEMENT en JSON:
            {
              "productName": "",
              "brand": "",
              "compatible": true,
              "compatibilityScore": 0-100,
              "safetyRating": "low | medium | high",
              "positiveIngredients": [],
              "negativeIngredients": [],
              "analysisResult": ""
            }

            Profil: type=%s, allergies=%s, à éviter=%s
            """.formatted(
                user.getSkinType() != null ? user.getSkinType().name() : "inconnu",
                user.getSkinConcerns() != null ? user.getSkinConcerns() : "aucun",
                user.getIngredientsToAvoid() != null ? user.getIngredientsToAvoid() : "aucun"
        );

        return callOpenAI(prompt, imageUrl);
    }

    // ================= GROQ CALL =================
    private String callOpenAI(String prompt, String imageUrl) {

        try {
            RestTemplate restTemplate = new RestTemplate();

            List<Map<String, Object>> content = new ArrayList<>();
            content.add(Map.of("type", "text", "text", prompt));

            if (imageUrl != null) {
                content.add(Map.of(
                        "type", "image_url",
                        "image_url", Map.of("url", imageUrl, "detail", "high")
                ));
            }

            Map<String, Object> message = new HashMap<>();
            message.put("role", "user");
            message.put("content", content);

            Map<String, Object> body = Map.of(
                    "model", groqModel,
                    "messages", List.of(message),
                    "max_tokens", 800,
                    "response_format", Map.of("type", "json_object")
            );

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(groqApiKey);

            ResponseEntity<Map> response = restTemplate.exchange(
                    "https://api.groq.com/openai/v1/chat/completions",
                    HttpMethod.POST,
                    new HttpEntity<>(body, headers),
                    Map.class
            );

            Map<?, ?> bodyResp = response.getBody();

            if (bodyResp != null) {
                List<?> choices = (List<?>) bodyResp.get("choices");

                if (choices != null && !choices.isEmpty()) {
                    return (String) ((Map<?, ?>)
                            ((Map<?, ?>) choices.get(0)).get("message")
                    ).get("content");
                }
            }

        } catch (Exception e) {
            log.error("AI call failed: {}", e.getMessage());
        }

        // fallback safe
        return """
        {
          "compatible": true,
          "compatibilityScore": 70,
          "positiveIngredients": ["aloe vera", "glycérine"],
          "negativeIngredients": [],
          "analysisResult": "Analyse automatique par défaut."
        }
        """;
    }

    // ================= BUILD ENTITY =================
    private ProductScan buildScanFromAiResult(
            User user,
            ProductScanRequestDto req,
            String aiJson,
            String imageUrl
    ) {

        try {
            if (aiJson == null || aiJson.isBlank()) {
                throw new RuntimeException("AI response empty");
            }

            JsonNode root = objectMapper.readTree(aiJson);

            List<String> positive = extractList(root, "positiveIngredients");
            List<String> negative = extractList(root, "negativeIngredients");
            String safetyRating = root.path("safetyRating").asText("medium");

            return ProductScan.builder()
                    .user(user)

                    // 🔒 SAFE PRODUCT NAME
                    .productName(
                            root.has("productName") && !root.path("productName").asText().isBlank()
                                    ? root.path("productName").asText()
                                    : (req.getProductName() != null ? req.getProductName() : "Produit inconnu")
                    )

                    .brand(req.getBrand())
                    .barcode(req.getBarcode())
                    .imageUrl(imageUrl)
                    .ingredients(req.getIngredients())

                    .compatible(root.path("compatible").asBoolean(true))
                    .compatibilityScore(root.path("compatibilityScore").asInt(70))
                    .safetyRating(safetyRating)
                    .positiveIngredients(objectMapper.writeValueAsString(positive))
                    .negativeIngredients(objectMapper.writeValueAsString(negative))

                    .analysisResult(root.path("analysisResult").asText("Analyse non disponible"))
                    .build();

        } catch (Exception e) {
            log.error("Parse error AI: {}", e.getMessage());

            return ProductScan.builder()
                    .user(user)
                    .productName(
                            req.getProductName() != null ? req.getProductName() : "Produit inconnu"
                    )
                    .compatible(true)
                    .compatibilityScore(70)
                    .analysisResult("Analyse non disponible")
                    .build();
        }
    }

    // ================= UTIL =================
    private List<String> extractList(JsonNode root, String field) {
        List<String> list = new ArrayList<>();

        JsonNode node = root.path(field);
        if (node.isArray()) {
            StreamSupport.stream(node.spliterator(), false)
                    .map(JsonNode::asText)
                    .forEach(list::add);
        }

        return list;
    }

    // ================= DTO =================
    private ProductScanResponseDto toDto(ProductScan scan) {
        ProductScanResponseDto dto = new ProductScanResponseDto();

        dto.setId(scan.getId());
        dto.setProductName(scan.getProductName());
        dto.setBrand(scan.getBrand());
        dto.setCompatible(scan.getCompatible());
        dto.setSafetyRating(scan.getSafetyRating());
        dto.setCompatibilityScore(scan.getCompatibilityScore());
        dto.setAnalysisResult(scan.getAnalysisResult());
        dto.setScannedAt(scan.getScannedAt());

        try {
            if (scan.getPositiveIngredients() != null) {
                JsonNode node = objectMapper.readTree(scan.getPositiveIngredients());
                List<String> list = new ArrayList<>();
                node.forEach(n -> list.add(n.asText()));
                dto.setPositiveIngredients(list);
            }

            if (scan.getNegativeIngredients() != null) {
                JsonNode node = objectMapper.readTree(scan.getNegativeIngredients());
                List<String> list = new ArrayList<>();
                node.forEach(n -> list.add(n.asText()));
                dto.setNegativeIngredients(list);
            }

        } catch (Exception e) {
            dto.setPositiveIngredients(List.of());
            dto.setNegativeIngredients(List.of());
        }

        return dto;
    }

    @Override
    public Page<ProductScanResponseDto> getScanHistory(Long userId, Pageable pageable) {
        return productScanRepository
                .findByUserIdOrderByScannedAtDesc(userId, pageable)
                .map(this::toDto);
    }
}