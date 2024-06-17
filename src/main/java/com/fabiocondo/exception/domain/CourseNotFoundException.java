package com.fabiocondo.exception.domain;

public class CourseNotFoundException extends Exception {
    public CourseNotFoundException(String message){
        super(message);
    }
}
