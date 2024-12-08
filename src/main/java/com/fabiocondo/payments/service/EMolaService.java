package com.fabiocondo.payments.service;

import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class EMolaService {

    private static final String BASE_URL = "https://api.emola.movitel.co.mz"; // e-Mola API URL
    private static final String TOKEN = "your-authentication-token"; // Replace with the actual token

    private final RestTemplate restTemplate;

    public EMolaService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String transferMoney(String phoneNumber, double amount) {
        String endpoint = BASE_URL + "/transfer";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(TOKEN);

        String body = String.format("{\"recipientNumber\":\"%s\", \"amount\":%.2f}", phoneNumber, amount);

        HttpEntity<String> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(endpoint, request, String.class);
            if (response.getStatusCode() == HttpStatus.OK) {
                return response.getBody();
            } else {
                throw new RuntimeException("Failed to transfer money: " + response.getStatusCode());
            }
        } catch (Exception e) {
            throw new RuntimeException("Connection error: " + e.getMessage(), e);
        }
    }
}
