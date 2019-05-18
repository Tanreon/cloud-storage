package com.geekbrains.cs.server;

public class AbstractResponse {
    protected String message;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean hasMessage() {
        return this.message.length() > 0;
    }
}
