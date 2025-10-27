package com.fabiocondo.payments.emola;

import com.fabiocondo.enumeration.Plan;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.HashMap;
import java.util.Map;

@Service
public class EMolaPaymentService {

    private static final String PAYMENT_URL = "https://api.emola.co.mz/v1/payments";

    public Map<String, Object> makePayment(String accessToken, String phoneNumber, double amount) {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = new HashMap<>();
        body.put("amount", amount);
        body.put("msisdn", phoneNumber);
        body.put("reference", "TX-" + System.currentTimeMillis());

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(PAYMENT_URL, request, Map.class);

        return response.getBody();
    }

    // DEPOIS REMOVER
    public boolean simulateEmolaPayment(String phoneNumber, Plan plan) {
        System.out.println("Simulando pagamento EMOLA para " + phoneNumber +
                " no plano " + plan + "...");
        simulateNetworkDelay();
        // Lógica fictícia de sucesso (exemplo: sempre dá certo se começa com 86 ou 87)
        return phoneNumber.startsWith("86") || phoneNumber.startsWith("87");
    }

    // DEPOIS REMOVER
    private void simulateNetworkDelay() {
        try {
            Thread.sleep(1500); // simula um pequeno atraso da transação
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}

