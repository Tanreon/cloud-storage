package com.geekbrains.cs.common;

public enum ActionType {
    UNKNOWN((byte) -1),
    ACCOUNT((byte)10),
    DOWNLOAD((byte)20),
    UPLOAD((byte)30),
    COMMAND((byte)90);

    private byte type;

    public static ActionType fromByte(byte item) {
        for (ActionType value : ActionType.values()) {
            if (value.getValue() == item) {
                return value;
            }
        }

        return ActionType.UNKNOWN;
    }

    ActionType(byte type) {
        this.type = type;
    }

    public byte getValue() {
        return type;
    }
}
