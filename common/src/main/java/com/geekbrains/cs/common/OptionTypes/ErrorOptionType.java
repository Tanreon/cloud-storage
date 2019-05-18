package com.geekbrains.cs.common.OptionTypes;

import com.geekbrains.cs.common.Contracts.OptionType;

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