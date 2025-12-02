package com.example.fiszapp.exception;

public class CardInvalidStatusException extends RuntimeException {
    public CardInvalidStatusException(String message) {
        super(message);
    }
}
