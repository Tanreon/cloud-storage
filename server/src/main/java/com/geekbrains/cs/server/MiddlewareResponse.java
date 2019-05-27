package com.geekbrains.cs.server;

import com.geekbrains.cs.common.HeaderType;


public class MiddlewareResponse extends AbstractResponse {
    protected HeaderType headerType;

    public MiddlewareResponse(HeaderType headerType, String message) {
        this.headerType = headerType;
        this.message = message;
    }

    public HeaderType getHeaderType() {
        return this.headerType;
    }
}
