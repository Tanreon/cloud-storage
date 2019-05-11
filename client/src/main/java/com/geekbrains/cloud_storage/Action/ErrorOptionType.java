package com.geekbrains.cloud_storage.Action;

import com.geekbrains.cloud_storage.Contract.OptionType;

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