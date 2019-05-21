package com.geekbrains.cs.server;


public class AbstractResponse {
    protected String message;
    protected boolean last;

    public boolean isLast() {
        return this.last;
    }

    public boolean hasMessage() {
        return this.message.length() > 0;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
