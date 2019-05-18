package com.geekbrains.cs.server.Contracts;

public class EmptyResultException extends Exception {
    public EmptyResultException(String message) {
        super(message);
    }
}
