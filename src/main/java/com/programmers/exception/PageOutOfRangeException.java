package com.programmers.exception;

public class PageOutOfRangeException extends RuntimeException {
    public PageOutOfRangeException(String message) {
        super(message);
    }
}
