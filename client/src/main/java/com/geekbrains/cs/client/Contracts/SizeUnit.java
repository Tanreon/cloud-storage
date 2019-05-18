package com.geekbrains.cs.client.Contracts;

public enum SizeUnit {
    BYTES(1),
    KILOBYTES(1024),
    MEGABYTES(1024 * KILOBYTES.value),
    GIGABYTES(1024 * MEGABYTES.value),
    TERABYTES(1024 * GIGABYTES.value),
    PETABYTES(1024 * TERABYTES.value),
    EXABYTES(1024 * PETABYTES.value),
    ZETTABYTES(1024 * EXABYTES.value),
    YOTTABYTES(1024 * ZETTABYTES.value);

    private int value;

    SizeUnit(int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }
}