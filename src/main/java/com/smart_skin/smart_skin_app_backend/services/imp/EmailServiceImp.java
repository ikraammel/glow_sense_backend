package com.smart_skin.smart_skin_app_backend.services.imp;


import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class EmailServiceImp {

    @Value("${resend.api-key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    public void sendEmail(String to, String subject, String html) {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        Map<String, Object> body = Map.of(
                "from", "Smart Skin <onboarding@resend.dev>",
                "to", to,
                "subject", subject,
                "html", html
        );

        restTemplate.postForEntity(
                "https://api.resend.com/emails",
                new HttpEntity<>(body, headers),
                String.class
        );
    }
}