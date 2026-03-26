package com.fabiocondo.controller;

import com.fabiocondo.domain.Payment;
import com.fabiocondo.repository.filter.PaymentFilter;
import com.fabiocondo.service.impl.PaymentService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    @GetMapping("/filter")
    public Page<Payment> filter(PaymentFilter paymentFilter, Pageable pageable) {
        return paymentService.filter(paymentFilter, pageable);
    }

}
