package com.fabiocondo.exception.domain;

public class OtpExpiredException extends Exception{
    public OtpExpiredException(String message){
        super(message);
    }
}
