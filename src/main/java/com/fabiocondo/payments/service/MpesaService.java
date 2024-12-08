package com.fabiocondo.payments.service;

import org.springframework.stereotype.Service;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Service
public class MpesaService {

    // Variáveis de configuração (podem ser configuradas em um arquivo de propriedades ou variáveis de ambiente)
    private static final String API_KEY = "YourAPIKey"; // Substitua pela sua API Key
    private static final String API_SECRET = "YourAPISecret"; // Substitua pela sua API Secret
    private static final String SHORTCODE = "YourShortcode"; // Substitua pelo seu Shortcode
    private static final String LIPA_NA_MPESA_SHORTCODE = "YourLipaNaMpesaShortcode"; // Substitua pelo seu Shortcode Lipa na Mpesa
    private static final String LIPA_NA_MPESA_SHORTCODE_PASSWORD = "YourPassword"; // Substitua pela senha do Lipa na Mpesa
    private static final String SENDER_ID = "SenderID"; // Substitua pelo seu Sender ID
    private static final String MPESA_BASE_URL = "https://sandbox.safaricom.co.ke/mpesa/"; // URL do sandbox (mude para produção quando necessário)

    // Função para obter o Access Token
    public String getAccessToken() throws Exception {
        String tokenUrl = MPESA_BASE_URL + "oauth/v1/generate?grant_type=client_credentials";

        URL url = new URL(tokenUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Authorization", "Basic " + encodeCredentials());

        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            // Usando InputStream e ByteArrayOutputStream para ler a resposta
            InputStream inputStream = connection.getInputStream();
            String response = readInputStream(inputStream);
            // Parse o JSON da resposta para obter o token
            return response.split(":")[1].split("\"")[1]; // Obtendo o access_token
        } else {
            throw new Exception("Failed to get access token");
        }
    }

    // Função para criar as credenciais codificadas
    public String encodeCredentials() {
        String apiCredentials = API_KEY + ":" + API_SECRET; // Usa as credenciais fornecidas
        return Base64.getEncoder().encodeToString(apiCredentials.getBytes());
    }

    // Função para realizar a transação C2B (Customer to Business)
    public String makeC2BTransaction(String accessToken, String phoneNumber, double amount) throws Exception {
        String c2bUrl = MPESA_BASE_URL + "mpesa/c2b/v1/paymentrequest";

        // Dados do corpo da solicitação (JSON)
        String jsonPayload = "{\n" +
                "\"Shortcode\":\"" + SHORTCODE + "\",\n" +
                "\"LipaNaMpesaShortcode\":\"" + LIPA_NA_MPESA_SHORTCODE + "\",\n" +
                "\"LipaNaMpesaShortcodePassword\":\"" + LIPA_NA_MPESA_SHORTCODE_PASSWORD + "\",\n" +
                "\"PhoneNumber\":\"" + phoneNumber + "\",\n" +
                "\"Amount\":\"" + amount + "\",\n" +
                "\"SenderId\":\"" + SENDER_ID + "\"\n" +
                "}";

        // Realizando o envio da solicitação
        URL url = new URL(c2bUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Authorization", "Bearer " + accessToken);
        connection.setDoOutput(true);

        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = jsonPayload.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            InputStream inputStream = connection.getInputStream();
            return readInputStream(inputStream);
        } else {
            throw new Exception("Failed to make C2B transaction");
        }
    }

    // Função para ler o InputStream e converter em String
    private String readInputStream(InputStream inputStream) throws Exception {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length;
        while ((length = inputStream.read(buffer)) != -1) {
            byteArrayOutputStream.write(buffer, 0, length);
        }
        return byteArrayOutputStream.toString("UTF-8");
    }
}
