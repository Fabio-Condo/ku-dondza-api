package com.fabiocondo.repository;

import com.fabiocondo.domain.Payment;
import com.fabiocondo.repository.query.PaymentRepositoryQuery;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long>, PaymentRepositoryQuery {

}
