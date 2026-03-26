package com.fabiocondo.repository.filter;

public class PaymentFilter {

    private String searchParam;

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
}