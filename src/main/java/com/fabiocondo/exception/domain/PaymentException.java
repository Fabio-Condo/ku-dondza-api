package com.fabiocondo.exception.domain;

public class PaymentException extends Exception{
    public PaymentException(String message){
        super(message);
    }
}