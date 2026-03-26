package com.fabiocondo.repository.query;

import com.fabiocondo.domain.Payment;
import com.fabiocondo.repository.filter.PaymentFilter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PaymentRepositoryQuery {
    public Page<Payment> filter(PaymentFilter paymentFilter, Pageable pageable);
}
