package com.example.fiszapp.exception;

public class WordNotFoundException extends RuntimeException {
    public WordNotFoundException(String message) {
        super(message);
    }
}
