package com.fabiocondo.exception.domain;

public class CommentNotFoundException extends Exception {
    public CommentNotFoundException(String message){
        super(message);
    }
}
