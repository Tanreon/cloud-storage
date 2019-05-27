package com.geekbrains.cs.common;


public enum HeaderType {
    UNKNOWN((byte) -1),
    AUTH((byte)10);

    private byte type;

    HeaderType(byte type) {
        this.type = type;
    }

    public static HeaderType fromByte(byte item) {
        for (HeaderType value : HeaderType.values()) {
            if (value.getValue() == item) {
                return value;
            }
        }

        return HeaderType.UNKNOWN;
    }

    public byte getValue() {
        return type;
    }
}
