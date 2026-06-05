package com.fabiocondo.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class GptService {

    private static final String API_KEY = "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"; // Substitua pela sua chave

    private static final String CHAT_API_URL =
            "https://api.openai.com/v1/responses";

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper mapper = new ObjectMapper();

    public String askAssistant(String prompt) throws Exception {

        // =========================
        // REQUEST BODY
        // =========================
        Map<String, Object> requestBodyMap = new HashMap<>();
        //requestBodyMap.put("model", "gpt-3.5-turbo"); // Funciona muito bem
        requestBodyMap.put("model", "gpt-4o"); // Funciona muito bem
        //requestBodyMap.put("model", "gpt-5"); // Devo ajustar na resposta para na retornar null

        requestBodyMap.put("input", prompt);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + API_KEY);

        HttpEntity<Map<String, Object>> entity =
                new HttpEntity<>(requestBodyMap, headers);

        // =========================
        // API CALL
        // =========================
        ResponseEntity<String> response = restTemplate.exchange(
                CHAT_API_URL,
                HttpMethod.POST,
                entity,
                String.class
        );

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Erro OpenAI: " + response.getBody());
        }

        JsonNode root = mapper.readTree(response.getBody());

        // =========================
        // SAFE EXTRACTION
        // =========================
        JsonNode output = root.path("output");

        if (!output.isArray() || output.size() == 0) {
            throw new RuntimeException(
                    "OpenAI retornou output vazio: " + response.getBody()
            );
        }

        JsonNode firstOutput = output.get(0);

        if (firstOutput == null) {
            throw new RuntimeException(
                    "Output inválido da OpenAI: " + response.getBody()
            );
        }

        JsonNode content = firstOutput.path("content");

        if (!content.isArray() || content.size() == 0) {
            throw new RuntimeException(
                    "OpenAI retornou content vazio: " + response.getBody()
            );
        }

        JsonNode firstContent = content.get(0);

        if (firstContent == null) {
            throw new RuntimeException(
                    "Content inválido da OpenAI: " + response.getBody()
            );
        }

        // =========================
        // TEXT EXTRACTION (SAFE)
        // =========================
        JsonNode textNode = firstContent.get("text");

        if (textNode == null || textNode.asText().isEmpty()) {
            JsonNode fallback = firstContent.get("output_text");

            if (fallback != null && !fallback.asText().isEmpty()) {
                return fallback.asText();
            }

            throw new RuntimeException(
                    "Texto não encontrado na resposta OpenAI: " + response.getBody()
            );
        }

        return textNode.asText();
    }
}