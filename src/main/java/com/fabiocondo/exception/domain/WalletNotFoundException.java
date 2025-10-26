package com.fabiocondo.exception.domain;

public class WalletNotFoundException extends Exception {
    public WalletNotFoundException(String message){
        super(message);
    }
}
