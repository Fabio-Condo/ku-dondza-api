package com.fabiocondo.payments.mpesa;

import com.fc.sdk.APIContext;
import com.fc.sdk.APIRequest;
import com.fc.sdk.APIResponse;
import com.fc.sdk.APIMethodType;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Map;
import java.util.UUID;

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

    public String processPayment(String phoneNumber, String amount) {
        try {

            log.info("Iniciando pagamento M-Pesa...");
            log.info("Base URL: {}", baseUrl);
            log.info("Port: {}", port);
            log.info("Path: {}", c2bPath);
            log.info("Phone Number: {}", phoneNumber);
            log.info("Amount: {}", amount);

            // Gerar Bearer Token manualmente
            String bearerToken = generateBearerToken(apiKey, publicKey);

            APIContext context = new APIContext();
            context.setApiKey(apiKey);
            context.setPublicKey(publicKey);

            // Sandbox usa false
            context.setSsl(false);
            context.setMethodType(APIMethodType.POST);
            context.setAddress(baseUrl);
            context.setPort(port);
            context.setPath(c2bPath);

            // Adicionar parâmetros da transação
            context.addParameter("input_TransactionReference", UUID.randomUUID().toString());
            context.addParameter("input_CustomerMSISDN", phoneNumber);
            context.addParameter("input_Amount", amount);
            context.addParameter("input_ThirdPartyReference", UUID.randomUUID().toString());
            context.addParameter("input_ServiceProviderCode", initiator);

            // Adicionar Bearer Token manualmente
            context.addHeader("Authorization", "Bearer " + bearerToken);
            context.addHeader("Origin", "developer.mpesa.vm.co.mz");

            APIRequest request = new APIRequest(context);
            APIResponse response = request.execute();

            if (response != null) {
                log.info("==================================");
                log.info("Status: {} - {}", response.getStatusCode(), response.getReason());
                log.info("==================================");

                for (Map.Entry<String, String> entry : response.getParameters().entrySet()) {
                    log.info("{} : {}", entry.getKey(), response.getParameter(entry.getKey()));
                }

                return response.getResult();
            }

        } catch (Exception e) {
            log.error("Erro ao processar pagamento M-Pesa", e);
        }

        return null;
    }

    // Geração do Bearer Token conforme doc oficial
    private String generateBearerToken(String apiKey, String publicKey) {
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
            return null;
        }
    }
}
