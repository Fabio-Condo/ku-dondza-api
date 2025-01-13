package com.fabiocondo.exception.domain;

public class BookNotFoundException extends Exception{
    public BookNotFoundException(String message){
        super(message);
    }
}
