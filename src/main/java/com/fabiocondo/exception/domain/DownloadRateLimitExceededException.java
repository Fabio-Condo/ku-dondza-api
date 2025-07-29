package com.fabiocondo.exception.domain;

public class DownloadRateLimitExceededException extends Exception{
    public DownloadRateLimitExceededException(String message){
        super(message);
    }
}
