package com.geekbrains.cs.common.OptionTypes;

import com.geekbrains.cs.common.Contracts.OptionType;

public enum DownloadOptionType implements OptionType {
    UNKNOWN((byte) -1),
    FILE((byte)1);

    private byte type;

    DownloadOptionType(byte type) {
        this.type = type;
    }

    public static DownloadOptionType fromByte(byte item) {
        for (DownloadOptionType value : DownloadOptionType.values()) {
            if (value.type == item) {
                return value;
            }
        }

        return DownloadOptionType.UNKNOWN;
    }

    public byte getValue() {
        return type;
    }
}
