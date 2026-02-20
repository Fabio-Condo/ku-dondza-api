package com.fabiocondo.payments.mpesa;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fc.sdk.APIContext;
import com.fc.sdk.APIRequest;
import com.fc.sdk.APIResponse;
import com.fc.sdk.APIMethodType;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Map;

@Service
@Slf4j
public class MpesaPaymentService {

    @Value("${mpesa.api.api-key}")
    private String apiKey;

    @Value("${mpesa.api.public-key}")
    private String publicKey;

    @Value("${mpesa.api.base-url}")
    private String baseUrl;

    @Value("${mpesa.api.port}")
    private int port;

    @Value("${mpesa.api.c2b-path}")
    private String c2bPath;

    @Value("${mpesa.api.initiator}")
    private String initiator;

    public MpesaPaymentResponse processPayment(String phoneNumber, String amount) {
        try {

            log.info("Iniciando pagamento M-Pesa...");
            log.info("Base URL: {}", baseUrl);
            log.info("Port: {}", port);
            log.info("Path: {}", c2bPath);
            log.info("Phone Number: {}", phoneNumber);
            log.info("Amount: {}", amount);

            String bearerToken = getBearerToken(apiKey, publicKey);

            APIContext context = new APIContext();
            context.setApiKey(apiKey);
            context.setPublicKey(publicKey);
            context.setSsl(true);
            context.setMethodType(APIMethodType.POST);
            context.setAddress(baseUrl);
            context.setPort(port);
            context.setPath(c2bPath);

            context.addParameter("input_TransactionReference", generateReference());
            context.addParameter("input_CustomerMSISDN", phoneNumber);
            context.addParameter("input_Amount", amount);
            context.addParameter("input_ThirdPartyReference", generateReference());
            context.addParameter("input_ServiceProviderCode", initiator);

            context.addHeader("Authorization", "Bearer " + bearerToken);
            context.addHeader("Origin", "*");

            APIRequest request = new APIRequest(context);
            APIResponse response = request.execute();

            if (response == null || response.getResult() == null) {
                log.warn("API M-Pesa retornou resposta nula.");
                throw new RuntimeException("Resposta nula da API M-Pesa");
            }

            log.info("==================================");
            log.info("Status: {} - {}", response.getStatusCode(), response.getReason());
            log.info("==================================");

            for (Map.Entry<String, String> entry : response.getParameters().entrySet()) {
                log.info("{} : {}", entry.getKey(), response.getParameter(entry.getKey()));
            }

            // Converter JSON para entidade
            ObjectMapper mapper = new ObjectMapper();
            MpesaPaymentResponse mpesaResponse =
                    mapper.readValue(response.getResult(), MpesaPaymentResponse.class);

            return mpesaResponse;

        } catch (Exception e) {
            log.error("Erro ao processar pagamento M-Pesa", e);

            MpesaPaymentResponse errorResponse = new MpesaPaymentResponse();
            errorResponse.setOutput_ResponseCode("ERROR");
            errorResponse.setOutput_ResponseDesc(e.getMessage());

            return errorResponse;
        }
    }

    // Geração do Bearer Token conforme doc oficial
    private String getBearerToken(String apiKey, String publicKey) {
        try {
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            Cipher cipher = Cipher.getInstance("RSA");

            byte[] encodedPublicKey = Base64.decodeBase64(publicKey);
            X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(encodedPublicKey);
            PublicKey pk = keyFactory.generatePublic(publicKeySpec);

            cipher.init(Cipher.ENCRYPT_MODE, pk);
            byte[] encryptedApiKey = Base64.encodeBase64(cipher.doFinal(apiKey.getBytes("UTF-8")));

            return new String(encryptedApiKey, "UTF-8");

        } catch (Exception e) {
            log.error("Erro ao gerar Bearer Token", e);
        }

        return null;
    }

    private String generateReference() {
        return RandomStringUtils.randomAlphanumeric(10);
    }
}
