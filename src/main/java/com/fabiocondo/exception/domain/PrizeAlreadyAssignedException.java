package com.fabiocondo.exception.domain;

public class PrizeAlreadyAssignedException extends Exception{
    public PrizeAlreadyAssignedException(String message){
        super(message);
    }
}
