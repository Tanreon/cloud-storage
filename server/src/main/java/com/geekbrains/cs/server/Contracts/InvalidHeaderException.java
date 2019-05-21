package com.geekbrains.cs.server.Contracts;


public class InvalidHeaderException extends Exception {
    private String message;

    public InvalidHeaderException(String message) {
        super();
        this.message = message;
    }

    public String getMessage() {
        return this.message;
    }
}
