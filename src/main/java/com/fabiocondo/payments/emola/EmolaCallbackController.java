package com.fabiocondo.payments.emola;

import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/emola/callback")
public class EmolaCallbackController {

    @PostMapping
    public Map<String, String> callback(@RequestBody Map<String, String> payload) {

        String requestId = payload.get("reqeustId");
        String transId   = payload.get("transId");
        String errorCode = payload.get("errorCode");
        String message   = payload.get("message");

        // 👉 Atualiza o estado no BD conforme errorCode
        // 0  -> sucesso
        // 11 -> cliente não digitou PIN
        // outros -> falha

        Map<String, String> response = new HashMap<>();
        response.put("ResponseCode", "0");
        response.put("ResponseMessage", "OK");

        return response;
    }
}


