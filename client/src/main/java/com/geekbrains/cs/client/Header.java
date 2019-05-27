package com.geekbrains.cs.client;

import com.geekbrains.cs.common.HeaderType;

public class Header {
    private HeaderType headerType;
    private String value;

    public Header(HeaderType headerType, String value) {
        this.headerType = headerType;
        this.value = value;
    }

    public HeaderType getHeaderType() {
        return headerType;
    }

    public String getValue() {
        return value;
    }
}
