package com.fabiocondo.exception.domain;

public class BlogNotFoundException extends Exception{
    public BlogNotFoundException(String message){
        super(message);
    }
}
