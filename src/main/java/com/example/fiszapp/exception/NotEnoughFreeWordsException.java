package com.example.fiszapp.exception;

public class NotEnoughFreeWordsException extends RuntimeException {
    public NotEnoughFreeWordsException(String message) {
        super(message);
    }
}
