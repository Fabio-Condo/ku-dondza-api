package com.fabiocondo.controller;

import com.fabiocondo.service.impl.GptService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/gpt-assistant")
public class GptController {

    private final GptService gptService;

    public GptController(GptService gptService) {
        this.gptService = gptService;
    }

    @PostMapping("/ask")
    public ResponseEntity<String> askAssistant(@RequestBody String prompt) {
        try {
            String response = gptService.askAssistant(prompt);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("Limite de requisições excedido")) {
                return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(e.getMessage());
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao processar requisição: " + e.getMessage());
        }
    }

}

