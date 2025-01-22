package com.fabiocondo.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.http.MediaType;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/assistant")
public class AssistantController {

    private static final String API_URL = "https://api.openai.com/v1/completions";
    private static final String API_KEY = "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";  // Substitua com sua chave de API da OpenAI

    private final Semaphore rateLimiter = new Semaphore(10); // Permite 10 requisições por minuto

    @PostMapping("/ask")
    public ResponseEntity<String> askAssistant(@RequestBody String prompt) {
        try {
            // Tenta adquirir uma permissão (bloqueia se o limite for atingido)
            if (!rateLimiter.tryAcquire(1, TimeUnit.MINUTES)) {
                return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                        .body("Limite de requisições excedido. Tente novamente mais tarde.");
            }

            // Restante do código...
            Map<String, Object> requestBodyMap = new HashMap<>();
            requestBodyMap.put("model", "gpt-3.5-turbo");
            requestBodyMap.put("messages", new Object[] {
                    new HashMap<String, String>() {{
                        put("role", "user");
                        put("content", prompt);
                    }}
            });

            ObjectMapper objectMapper = new ObjectMapper();
            String requestBody = objectMapper.writeValueAsString(requestBodyMap);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + API_KEY);

            HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);
            RestTemplate restTemplate = new RestTemplate();

            String CHAT_API_URL = "https://api.openai.com/v1/chat/completions";
            ResponseEntity<String> response = restTemplate.exchange(CHAT_API_URL, HttpMethod.POST, entity, String.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                return ResponseEntity.ok(response.getBody());
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Erro ao chamar a API da OpenAI: " + response.getBody());
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao fazer a requisição para a API da OpenAI: " + e.getMessage());
        } finally {
            rateLimiter.release(); // Libera a permissão após a requisição
        }
    }

}

