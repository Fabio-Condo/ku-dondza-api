package com.fabiocondo.exception.domain;

public class InvalidOtpException extends Exception{
    public InvalidOtpException(String message){
        super(message);
    }
}
