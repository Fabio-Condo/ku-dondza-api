package com.fabiocondo.payments.emola;

import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

public class EMolaAuthService {

    private static final String AUTH_URL = "https://api.emola.co.mz/token"; // exemplo
    private static final String CLIENT_ID = "teu_client_id";
    private static final String CLIENT_SECRET = "teu_client_secret";

    public String getAccessToken() {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setBasicAuth(CLIENT_ID, CLIENT_SECRET);

        HttpEntity<String> request = new HttpEntity<>("grant_type=client_credentials", headers);
        ResponseEntity<Map> response = restTemplate.exchange(AUTH_URL, HttpMethod.POST, request, Map.class);

        return (String) response.getBody().get("access_token");
    }
}


