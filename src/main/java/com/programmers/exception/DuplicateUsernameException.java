package com.programmers.exception;

public class DuplicateUsernameException extends RuntimeException {
    public DuplicateUsernameException(String username) {
        super("Username " + username + " already exists");
    }
}
