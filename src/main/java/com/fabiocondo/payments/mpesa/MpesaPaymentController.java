package com.fabiocondo.payments.mpesa;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/mpesa")
public class MpesaPaymentController {

    private final MpesaPaymentService mpesaPaymentService;


    public MpesaPaymentController(MpesaPaymentService mpesaPaymentService) {
        this.mpesaPaymentService = mpesaPaymentService;
    }

    @PostMapping("/pay")
    public ResponseEntity<MpesaPaymentResponse> pay(
            @RequestParam String phone,
            @RequestParam String amount
    ) {

        MpesaPaymentResponse response = mpesaPaymentService.processPayment(phone, amount);

        return ResponseEntity.ok(response);
    }
}

