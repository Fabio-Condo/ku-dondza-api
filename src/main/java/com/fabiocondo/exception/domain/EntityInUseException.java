package com.fabiocondo.exception.domain;

public class EntityInUseException extends Exception{
    public EntityInUseException(String message){
        super(message);
    }
}
