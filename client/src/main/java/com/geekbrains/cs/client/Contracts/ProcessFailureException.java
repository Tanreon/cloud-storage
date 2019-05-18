package com.geekbrains.cs.client.Contracts;

public class ProcessFailureException extends Exception {
    public ProcessFailureException(String message) {
        super(message);
    }

    public ProcessFailureException(Throwable cause, String message) {
        super(message, cause);
    }
}
