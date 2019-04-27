package com.geekbrains.cloud_storage.Action;

import com.geekbrains.cloud_storage.Contract.OptionType;

public enum UploadOptionType implements OptionType {
    UNKNOWN((byte) -1),
    FILE((byte)1);

    private byte type;

    public static UploadOptionType fromByte(byte item) {
        for (UploadOptionType value : UploadOptionType.values()) {
            if (value.type == item) {
                return value;
            }
        }

        return UploadOptionType.UNKNOWN;
    }

    UploadOptionType(byte type) {
        this.type = type;
    }

    public byte getValue() {
        return type;
    }
}
