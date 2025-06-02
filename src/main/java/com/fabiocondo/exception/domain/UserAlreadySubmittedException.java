package com.fabiocondo.exception.domain;

public class UserAlreadySubmittedException extends Exception{
    public UserAlreadySubmittedException(String message){
        super(message);
    }
}
