package com.geekbrains.cloud_storage;

import com.geekbrains.cloud_storage.Contract.AbstractFileRow;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;

public class ClientFileRow extends AbstractFileRow {
    public ClientFileRow(Path path) {
        this.name = path.toFile().getName();
        this.size = path.toFile().length();

        try {
            this.createdAt = Files.readAttributes(path, BasicFileAttributes.class).creationTime().toInstant();
            this.modifiedAt = Files.readAttributes(path, BasicFileAttributes.class).lastModifiedTime().toInstant();
        } catch (IOException e) {
            this.createdAt = Instant.EPOCH;
            this.modifiedAt = Instant.EPOCH;
        }
    }
}
