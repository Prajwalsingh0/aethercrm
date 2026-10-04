package com.aethercrm.ai;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class AiProviderService {

    @Value("${aethercrm.ai.provider:mock}")
    private String provider;

    @Value("${aethercrm.ai.openai.api-key:}")
    private String apiKey;

    @Value("${aethercrm.ai.openai.base-url:https://api.openai.com/v1}")
    private String baseUrl;

    @Value("${aethercrm.ai.openai.model:gpt-4o-mini}")
    private String model;

    private final RestTemplate restTemplate = new RestTemplate();

    public boolean isLive() {
        return "openai".equalsIgnoreCase(provider) && apiKey != null && !apiKey.isBlank();
    }

    public String getProviderName() {
        return isLive() ? "openai" : "mock";
    }

    public String chat(String systemPrompt, String userMessage) {
        if (!isLive()) return null;
        try {
            String url = baseUrl.replaceAll("$", "").replaceAll("/$", "") + "/chat/completions";
            Map<String, Object> body = new HashMap<>();
            body.put("model", model);
            body.put("temperature", 0.3);
            body.put("messages", List.of(
                    Map.of("role", "system", "content", systemPrompt),
                    Map.of("role", "user", "content", userMessage)
            ));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            @SuppressWarnings("rawtypes")
            ResponseEntity<Map> resp = restTemplate.exchange(
                    url, HttpMethod.POST, new HttpEntity<>(body, headers), Map.class);

            if (resp.getBody() == null) return null;
            Object choices = resp.getBody().get("choices");
            if (!(choices instanceof List<?> list) || list.isEmpty()) return null;
            Object first = list.get(0);
            if (!(first instanceof Map<?, ?> choice)) return null;
            Object message = choice.get("message");
            if (!(message instanceof Map<?, ?> msg)) return null;
            Object content = msg.get("content");
            return content != null ? content.toString().trim() : null;
        } catch (Exception e) {
            return null;
        }
    }
}
