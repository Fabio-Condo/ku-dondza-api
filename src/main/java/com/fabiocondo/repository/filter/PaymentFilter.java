package com.fabiocondo.repository.filter;

import com.fabiocondo.enumeration.PaymentStatus;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;

public class PaymentFilter {

    private String searchParam;

    @Enumerated(EnumType.STRING)
    private PaymentStatus status; // SUCCESS, FAILED, PENDING

    private String paymentOrderBy;

    public String getSearchParam() {
        return searchParam;
    }

    public void setSearchParam(String searchParam) {
        this.searchParam = searchParam;
    }

    public String getPaymentOrderBy() {
        return paymentOrderBy;
    }

    public void setPaymentOrderBy(String paymentOrderBy) {
        this.paymentOrderBy = paymentOrderBy;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public void setStatus(PaymentStatus status) {
        this.status = status;
    }
}