package com.geekbrains.cloud_storage.Contract;

import java.time.Instant;

/*
* FIXME не нравится название и то что от него зависит
* */
public abstract class AbstractFileListCellView {
    protected String name;
    protected long size;
    protected Instant modifiedAt;
    protected Instant createdAt;

    public String getName() {
        return name;
    }

    public double getSize(SizeUnit unit) {
        return (double) size / unit.getValue();
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getModifiedAt() {
        return modifiedAt;
    }

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
}
