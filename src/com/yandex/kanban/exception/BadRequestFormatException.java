package com.yandex.kanban.exception;

public class BadRequestFormatException extends RuntimeException {
    public BadRequestFormatException(String message) {
        super(message);
    }
}