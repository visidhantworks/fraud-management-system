package com.sidhant.fraudmanagement.exception;

public class ActiveSessionException extends RuntimeException {
    public ActiveSessionException(String message){
        super(message);
    }
    
}
