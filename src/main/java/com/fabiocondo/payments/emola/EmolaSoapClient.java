package com.fabiocondo.payments.emola;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class EmolaSoapClient {

    @Value("${emola.url}")
    private String url;

    private final RestTemplate restTemplate = new RestTemplate();

    public String sendSoap(String xml) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_XML);

        HttpEntity<String> request = new HttpEntity<>(xml, headers);
        ResponseEntity<String> response =
                restTemplate.exchange(url, HttpMethod.POST, request, String.class);

        return response.getBody();
    }
}

