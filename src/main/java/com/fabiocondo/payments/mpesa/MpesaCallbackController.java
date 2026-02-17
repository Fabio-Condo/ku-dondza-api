package com.fabiocondo.payments.mpesa;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/mpesa/callback")
public class MpesaCallbackController {

    @PostMapping
    public ResponseEntity<String> handleCallback(@RequestBody Map<String, Object> payload) {

        System.out.println("Callback recebido: " + payload);

        // Aqui deves:
        // 1. Validar status da transação
        // 2. Atualizar base de dados
        // 3. Confirmar pagamento ao utilizador

        return ResponseEntity.ok("Received");
    }
}

