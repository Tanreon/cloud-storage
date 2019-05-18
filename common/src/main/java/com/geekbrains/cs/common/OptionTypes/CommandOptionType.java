package com.geekbrains.cs.common.OptionTypes;

import com.geekbrains.cs.common.Contracts.OptionType;

public enum CommandOptionType implements OptionType {
    UNKNOWN((byte) -1),
    FILE_LIST((byte)1),
    RENAME_FILE((byte)2),
    DELETE_FILE((byte)3);

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
