package com.geekbrains.cs.common.Contracts;


public class ProcessException extends Exception {
    private int status;
    private String message;

    public ProcessException(int status, String message) {
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
