package com.geekbrains.cs.server.Contracts;

import com.geekbrains.cs.server.AbstractResponse;

public class ProcessFailureException extends Exception {
    private AbstractResponse response;

    public ProcessFailureException(AbstractResponse response, String message) {
        super(message);
        this.response = response;
    }

    public ProcessFailureException(Throwable throwable, AbstractResponse response) {
        super(throwable);
        this.response = response;
    }

    public AbstractResponse getResponse() {
        return response;
    }
}
