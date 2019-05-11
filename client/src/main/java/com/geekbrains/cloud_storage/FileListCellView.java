package com.geekbrains.cloud_storage;

import javafx.beans.property.StringProperty;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.time.Instant;

public class FileListCellView {
    enum SizeUnit {
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
    
    private String name;
    private long size;
    private FileTime modifiedAt;
    private FileTime createdAt;

    public String getName() {
        return name;
    }

    public double getSize(SizeUnit unit) {
        return (double) size / unit.getValue();
    }

    public FileTime getModifiedAt() {
        return modifiedAt;
    }

    public FileTime getCreatedAt() {
        return createdAt;
    }

    public FileListCellView(Path path) {
        this.name = path.toFile().getName();
        this.size = path.toFile().length();

        try {
            this.createdAt = Files.readAttributes(path, BasicFileAttributes.class).creationTime();
            this.modifiedAt = Files.readAttributes(path, BasicFileAttributes.class).lastModifiedTime();
        } catch (IOException e) {
            this.createdAt = FileTime.from(Instant.EPOCH);
            this.modifiedAt = FileTime.from(Instant.EPOCH);
        }
    }
}
