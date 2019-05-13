package com.geekbrains.cloud_storage.Action;

import com.geekbrains.cloud_storage.Contract.OptionType;

public enum DownloadOptionType implements OptionType {
    UNKNOWN((byte) -1),
    FILE((byte)1);

    private byte type;

    public static DownloadOptionType fromByte(byte item) {
        for (DownloadOptionType value : DownloadOptionType.values()) {
            if (value.type == item) {
                return value;
            }
        }

        return DownloadOptionType.UNKNOWN;
    }

    DownloadOptionType(byte type) {
        this.type = type;
    }

    public byte getValue() {
        return type;
    }
}
