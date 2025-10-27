package com.fabiocondo.payments.mpesa;

import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import java.util.Base64;

public class MpesaAuthService {

    private static final String API_KEY = "YOUR_API_KEY";
    private static final String PUBLIC_KEY = "YOUR_PUBLIC_KEY";
    private static final String TOKEN_URL = "https://api.vm.co.mz:18352/ipg/v1x/accessToken";

    public String getAccessToken() {
        RestTemplate restTemplate = new RestTemplate();

        // Codifica API_KEY:PUBLIC_KEY em Base64
        String basicAuth = Base64.getEncoder()
                .encodeToString((API_KEY + ":" + PUBLIC_KEY).getBytes());

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Basic " + basicAuth);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
                TOKEN_URL,
                HttpMethod.POST,
                entity,
                String.class
        );

        if (response.getStatusCode() == HttpStatus.OK) {
            return extractToken(response.getBody());
        } else {
            throw new RuntimeException("Falha ao obter token: " + response.getStatusCode());
        }
    }

    // Extrai o valor do token da resposta JSON (pode usar Jackson)
    private String extractToken(String responseBody) {
        // Exemplo simples, dependendo do formato:
        // {"output_ResponseCode":"INS-0","output_AccessToken":"xxxxxx"}
        int start = responseBody.indexOf("output_AccessToken") + 21;
        int end = responseBody.indexOf("\"", start);
        return responseBody.substring(start, end);
    }
}

