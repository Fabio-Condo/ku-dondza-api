package com.fabiocondo.controller;

import com.fabiocondo.domain.Payment;
import com.fabiocondo.service.impl.PaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @GetMapping("/all")
    public ResponseEntity<List<Payment>> getAllPayments() {
        List<Payment> payments = paymentService.findAll();
        return ResponseEntity.ok(payments);
    }
}
