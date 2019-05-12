package com.geekbrains.cs.common.OptionType;

import com.geekbrains.cs.common.Contract.OptionType;

public enum ErrorOptionType implements OptionType {
    UNKNOWN((byte) -1),
    SERVER_ERROR((byte) 1);

    private byte type;

    ErrorOptionType(byte type) {
        this.type = type;
    }

    public byte getValue() {
        return type;
    }
}