package com.fabiocondo.service.impl;

import com.fabiocondo.domain.Payment;
import com.fabiocondo.repository.PaymentRepository;
import com.fabiocondo.repository.filter.PaymentFilter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;

    public PaymentService(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    public Payment save(Payment payment) {
        return paymentRepository.save(payment);
    }

    public Page<Payment> filter(PaymentFilter paymentFilter, Pageable pageable) {
        return paymentRepository.filter(paymentFilter, pageable);
    }
}

