package com.fabiocondo.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

@Service
public class GptService {

    private static final String API_KEY = "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"; // Substitua pela sua chave
    private static final String CHAT_API_URL = "https://api.openai.com/v1/chat/completions";
    private final Semaphore rateLimiter = new Semaphore(10); // 10 req/min

    public String askAssistant(String prompt) throws Exception {
        // Controle de limite de requisições
        if (!rateLimiter.tryAcquire(1, TimeUnit.MINUTES)) {
            throw new RuntimeException("Limite de requisições excedido. Tente novamente mais tarde.");
        }

        try {
            // Monta o corpo da requisição
            Map<String, Object> requestBodyMap = new HashMap<>();
            requestBodyMap.put("model", "gpt-3.5-turbo");
            requestBodyMap.put("messages", new Object[]{
                    new HashMap<String, String>() {{
                        put("role", "user");
                        put("content", prompt);
                    }}
            });

            ObjectMapper objectMapper = new ObjectMapper();
            String requestBody = objectMapper.writeValueAsString(requestBodyMap);

            // Headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + API_KEY);

            // HTTP Entity
            HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);
            RestTemplate restTemplate = new RestTemplate();

            // Chamada à API
            ResponseEntity<String> response = restTemplate.exchange(
                    CHAT_API_URL, HttpMethod.POST, entity, String.class
            );

            if (response.getStatusCode() == HttpStatus.OK) {
                return response.getBody();
            } else {
                throw new RuntimeException("Erro ao chamar API da OpenAI: " + response.getBody());
            }
        } finally {
            rateLimiter.release();
        }
    }

    public String generateText(@RequestBody String prompt) {
        return "";
    }
}
