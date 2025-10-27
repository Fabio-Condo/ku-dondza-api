package com.fabiocondo.payments.mpesa;

import com.fabiocondo.enumeration.Plan;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class MpesaPaymentService {

    private static final String SERVICE_PROVIDER_CODE = "171717";
    private static final String PAYMENT_URL = "https://api.vm.co.mz:18352/ipg/v1x/c2bPayment/singleStage/";

    private final RestTemplate restTemplate = new RestTemplate();
    private final MpesaAuthService authService = new MpesaAuthService();

    public void sendC2BPayment(String msisdn, String amount, String reference) {
        String token = authService.getAccessToken();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);

        String body = String.format(
                "{\"input_TransactionReference\":\"%s\"," +
                        "\"input_CustomerMSISDN\":\"%s\"," +
                        "\"input_Amount\":\"%s\"," +
                        "\"input_ThirdPartyReference\":\"%s\"," +
                        "\"input_ServiceProviderCode\":\"%s\"}",
                reference, msisdn, amount, reference, SERVICE_PROVIDER_CODE
        );

        HttpEntity<String> entity = new HttpEntity<>(body, headers);

        ResponseEntity<String> response = restTemplate.exchange(
                PAYMENT_URL,
                HttpMethod.POST,
                entity,
                String.class
        );

        System.out.println("Status: " + response.getStatusCode());
        System.out.println("Resposta: " + response.getBody());
    }

    // DEPOIS REMOVER
    public boolean simulateMpesaPayment(String phoneNumber, Plan plan) {
        System.out.println("Simulando pagamento MPESA para " + phoneNumber +
                " no plano " + plan + "...");
        simulateNetworkDelay();
        // Lógica fictícia de sucesso (exemplo: sempre dá certo se começa com 84 ou 85)
        return phoneNumber.startsWith("84") || phoneNumber.startsWith("85");
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

