package com.geekbrains.cloud_storage.Action;

import com.geekbrains.cloud_storage.Contract.OptionType;

public enum AccountOptionType implements OptionType {
    UNKNOWN((byte) -1),
    AUTH((byte)1),
    CREATE((byte)2),
    CHANGE_PASS((byte)3),
    CHANGE_LOGIN((byte)4),
    DELETE_ACCOUNT((byte)5);

    private byte type;

    public static AccountOptionType fromByte(byte item) {
        for (AccountOptionType value : AccountOptionType.values()) {
            if (value.type == item) {
                return value;
            }
        }

        return AccountOptionType.UNKNOWN;
    }

    AccountOptionType(byte type) {
        this.type = type;
    }

    public byte getValue() {
        return type;
    }
}
