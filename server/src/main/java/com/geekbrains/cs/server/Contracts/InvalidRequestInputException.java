package com.geekbrains.cs.server.Contracts;


public class InvalidRequestInputException extends Exception {
    private int status;
    private String message;

    public InvalidRequestInputException(int status, String message) {
        super();
        this.status = status;
        this.message = message;
    }

    public int getStatus() {
        return this.status;
    }

    public String getMessage() {
        return this.message;
    }
}
