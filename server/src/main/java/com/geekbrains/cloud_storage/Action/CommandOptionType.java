package com.geekbrains.cloud_storage.Action;

import com.geekbrains.cloud_storage.Contract.OptionType;

public enum CommandOptionType implements OptionType {
    UNKNOWN((byte) -1),
    FILE_LIST((byte)1);

    private byte type;

    public static CommandOptionType fromByte(byte item) {
        for (CommandOptionType value : CommandOptionType.values()) {
            if (value.type == item) {
                return value;
            }
        }

        return CommandOptionType.UNKNOWN;
    }

    CommandOptionType(byte type) {
        this.type = type;
    }

    public byte getValue() {
        return type;
    }
}
